package arraydefs;

/* Soot - a J*va Optimization Framework
 * Copyright (C) 2003 Navindra Umanee <navindra@cs.mcgill.ca>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

import intervals.LocalsInterval;
import intervals.VarInterval;

import java.util.*;

import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.*;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class LocalArrayDefs {

	protected Map<Unit, List>	unitToLocalsBefore;
	protected Map<Unit, List>	unitToLocalsAfter;

	public LocalArrayDefs(UnitGraph graph, LocalsInterval intervalAnalysis) {
		LocalArrayDefsAnalysis analysis = new LocalArrayDefsAnalysis(graph,
				intervalAnalysis);

		// Build unitToLocals map
		unitToLocalsAfter = new HashMap<Unit, List>(graph.size() * 2 + 1, 0.7f);
		unitToLocalsBefore = new HashMap<Unit, List>(graph.size() * 2 + 1, 0.7f);

		Iterator unitIt = graph.iterator();

		while (unitIt.hasNext()) {
			Unit s = (Unit) unitIt.next();

			FlowSet set = (FlowSet) analysis.getFlowBefore(s);
			unitToLocalsBefore.put(s,
					Collections.unmodifiableList(set.toList()));

			set = (FlowSet) analysis.getFlowAfter(s);
			unitToLocalsAfter
					.put(s, Collections.unmodifiableList(set.toList()));
		}
	}

	public List getArrayDefsBefore(Unit s) {
		return unitToLocalsBefore.get(s);
	}

	public List getArrayDefsAfter(Unit s) {
		return unitToLocalsAfter.get(s);
	}

	public ArrayDef getArrayDefBefore(Unit s, String arrayName) {
		Iterator arrDefsIter = getArrayDefsBefore(s).iterator();
		ArrayDef ad = null;

		while (arrDefsIter.hasNext()) {
			ad = (ArrayDef) arrDefsIter.next();
			if (ad.getName().equals(arrayName)) { return ad; }
		}
		return null;
	}
}

@SuppressWarnings({ "unchecked", "rawtypes" })
class LocalArrayDefsAnalysis extends ForwardFlowAnalysis {

	private FlowSet				emptySet			= new ArraySparseSet();
	private Map<Unit, FlowSet>	unitToGenerateSet	= new HashMap<Unit, FlowSet>(
															graph.size() * 2 + 1,
															0.7f);
	private Map<Unit, FlowSet>	unitToKillSet		= new HashMap<Unit, FlowSet>(
															graph.size() * 2 + 1,
															0.7f);

	LocalArrayDefsAnalysis(UnitGraph graph, LocalsInterval analysis) {
		super(graph);
		Iterator unitIt = graph.iterator();

		// LocalsInterval analysis = new LocalsInterval(graph);

		/* Create gen and kill sets */
		while (unitIt.hasNext()) {
			Unit s = (Unit) unitIt.next();

			FlowSet genSet = emptySet.clone();
			FlowSet killSet = emptySet.clone();

			// Iterator defBoxIter = s.getDefBoxes().iterator();

			/* Create gen set for "int x[]" statements - not needed */
			// while (defBoxIter.hasNext()) {
			// Value defValue = ((ValueBox) defBoxIter.next()).getValue();
			//
			// if (defValue instanceof Local && s.getUseBoxes().size() == 0) {
			// String variableName = defValue.toString();
			// G.v().out.println("varName is is " + variableName);
			// if (defValue.getType() instanceof ArrayType) {
			// genSet.add(new ArrayDef(variableName, 0));
			// }
			// }
			// }

			if (s instanceof AssignStmt) {
				Value lhs = ((AssignStmt) s).getLeftOp();
				Value rhs = ((AssignStmt) s).getRightOp();

				if (rhs instanceof NewArrayExpr) {
					if (lhs instanceof Local) {
						String arrayName = lhs.toString();
						long arraySize = 0;
						Value size = ((NewArrayExpr) rhs).getSize();

						/* Create gen set for the array definition */
						if (size instanceof IntConstant) {
							arraySize = ((IntConstant) size).value;
							genSet.add(new ArrayDef(arrayName, arraySize));
						} else if (size instanceof Local) {
							String sizeVariableName = size.toString();
							arraySize = getLowerBound(
									analysis.getLocalsIntervalBefore(s),
									sizeVariableName);
							genSet.add(new ArrayDef(arrayName, arraySize));
						}
						/* Create kill set for the array definition */
						killSet.add(new ArrayDef(arrayName, arraySize));
					}
				}
			}

			else {
				/* nothing to do */
			}

			unitToGenerateSet.put(s, genSet);
			unitToKillSet.put(s, killSet);
		}
		doAnalysis();
	}

	private long getLowerBound(List localsIntervalBefore, String variableName) {
		Iterator fsIter = localsIntervalBefore.iterator();
		VarInterval vi = null;
		while (fsIter.hasNext()) {
			vi = (VarInterval) fsIter.next();
			if (vi.getVar().equals(variableName)) { return vi.getInterval()
					.getLowerBound(); }
		}
		return 0;
	}

	/**
	 * All INs are initialized to the empty set.
	 **/
	protected Object newInitialFlow() {
		return emptySet.clone();
	}

	/**
	 * IN(Start) is the empty set
	 **/
	protected Object entryInitialFlow() {
		return emptySet.clone();
	}

	/**
	 * OUT is the same as IN plus the genSet.
	 **/
	protected void flowThrough(Object inValue, Object unit, Object outValue) {

		FlowSet in = (FlowSet) inValue, out = (FlowSet) outValue;
		FlowSet genSet = emptySet.clone();
		FlowSet killSet = emptySet.clone();

		Unit s = (Unit) unit;

		if (s instanceof AssignStmt) {

			Value lhs = ((AssignStmt) s).getLeftOp();
			Value rhs = ((AssignStmt) s).getRightOp();

			/* a[] = b where b is an array reference */
			if (rhs.getType() instanceof ArrayType) {

				if (lhs.getType() instanceof ArrayType) {
					String localName = lhs.toString();
					String arrayName = rhs.toString();

					/* Kill previous array def */
					ArrayDef adToKill = flowSetContain(in, localName);
					if (adToKill != null) {
						killSet.add(adToKill);
						killSet.union(unitToKillSet.get(s));

						unitToKillSet.put(s, killSet);
					}

					ArrayDef ad = flowSetContain(in, arrayName);
					if (ad != null) {
						genSet.add(new ArrayDef(localName, ad.getSize()),
								genSet);
					}
				}
			}
		}

		if (!genSet.isEmpty()) {
			unitToGenerateSet.put(s, genSet);
		}

		if (!killSet.isEmpty()) {
			unitToKillSet.put(s, killSet);
		}

		/* Update output, subtract kill and add gen */
		in.difference(unitToKillSet.get(s));
		in.union(unitToGenerateSet.get(s), out);

		/* Debug prints */
		// G.v().out.println("in= " + in + " kill= " + unitToKillSet.get(unit)
		// + " gen= " + unitToGenerateSet.get(unit) + " out=" + out);
	}

	/**
	 * All paths == Union.
	 **/
	protected void merge(Object in1, Object in2, Object out) {
		FlowSet inSet1 = (FlowSet) in1, inSet2 = (FlowSet) in2, outSet = (FlowSet) out;

		FlowSet genArrayDefs = emptySet.clone();
		FlowSet killArrayDefs = emptySet.clone();

		Iterator set1Iter = inSet1.iterator();
		Iterator set2Iter = inSet2.iterator();

		/* Debug prints */

		// G.v().out.println("in1Set size is: " + inSet1.size()
		// + " , elements are:");
		// while (set1Iter.hasNext()) {
		// VarInterval v = (VarInterval) set1Iter.next();
		// G.v().out.println("varName: " + v.getVar() + " Interval: "
		// + v.getInterval().toString());
		// }
		//
		//
		// G.v().out.println("in2Set size is: " + inSet2.size()
		// + " , elements are:");
		// while (set2Iter.hasNext()) {
		// VarInterval v = (VarInterval) set2Iter.next();
		// G.v().out.println("varName: " + v.getVar() + " Interval: "
		// + v.getInterval().toString());
		// }
		//

		set1Iter = inSet1.iterator();
		set2Iter = inSet2.iterator();

		/* Combining array definitions - taking the minimum size */
		while (set1Iter.hasNext()) {
			ArrayDef ad1 = (ArrayDef) set1Iter.next();
			ArrayDef ad2 = flowSetContain(inSet2, ad1.getName());
			if (ad2 != null) {
				genArrayDefs.add(new ArrayDef(ad1.getName(), Math.min(
						ad1.getSize(), ad2.getSize())));
				killArrayDefs.add(ad1);
				killArrayDefs.add(ad2);
			}
		}

		// Iterator genIter = genIntervals.iterator();
		// G.v().out.println("genSet size is: " + genIntervals.size()
		// + " , elements are:");
		// while (genIter.hasNext()) {
		// VarInterval v = (VarInterval) genIter.next();
		// G.v().out.println("varName: " + v.getVar() + " Interval: "
		// + v.getInterval().toString());
		// }

		/* outSet = (inSet1 U inSet2) */
		inSet1.union(inSet2, outSet);
		/* remove combined array defs from outSet */
		outSet.difference(killArrayDefs);
		/* add to outSet the combined definitions of the common arrays */
		genArrayDefs.union(outSet, outSet);
	}

	protected void copy(Object source, Object dest) {
		FlowSet sourceSet = (FlowSet) source, destSet = (FlowSet) dest;

		sourceSet.copy(destSet);
	}

	private ArrayDef flowSetContain(FlowSet fs, String arrayName) {
		Iterator fsIter = fs.iterator();
		ArrayDef ad = null;
		while (fsIter.hasNext()) {
			ad = (ArrayDef) fsIter.next();
			if (ad.getName().equals(arrayName)) { return ad; }
		}
		return null;
	}
}
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

import intervals.BLocalsInterval;
import intervals.Interval;

import java.util.*;

import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.*;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class BLocalArrayDefs {

	protected Map<Unit, List>	unitToLocalsBefore;
	protected Map<Unit, List>	unitToLocalsAfter;

	public BLocalArrayDefs(UnitGraph graph, BLocalsInterval intervalAnalysis) {

		BranchLocalArrayDefsAnalysis analysis = new BranchLocalArrayDefsAnalysis(
				graph, intervalAnalysis);

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
class BranchLocalArrayDefsAnalysis extends ForwardFlowAnalysis {

	private FlowSet				emptySet			= new ArraySparseSet();
	private Map<Unit, FlowSet>	unitToGenerateSet	= new HashMap<Unit, FlowSet>(
															graph.size() * 2 + 1,
															0.7f);
	private Map<Unit, FlowSet>	unitToKillSet		= new HashMap<Unit, FlowSet>(
															graph.size() * 2 + 1,
															0.7f);

	BranchLocalArrayDefsAnalysis(UnitGraph graph, BLocalsInterval analysis) {
		super(graph);
		Iterator unitIt = graph.iterator();

		/* Create gen and kill sets */
		while (unitIt.hasNext()) {
			Unit s = (Unit) unitIt.next();

			FlowSet genSet = emptySet.clone();
			FlowSet killSet = emptySet.clone();

			if (s instanceof AssignStmt) {
				Value lhs = ((AssignStmt) s).getLeftOp();
				Value rhs = ((AssignStmt) s).getRightOp();

				if (rhs instanceof NewArrayExpr) {
					if (lhs instanceof Local) {
						String arrayName = lhs.toString();
						Value size = ((NewArrayExpr) rhs).getSize();
						Interval i = null;

						/* Create gen set for the array definition */
						if (size instanceof IntConstant) {
							long arraySize = ((IntConstant) size).value;
							i = new Interval(arraySize, arraySize);
							genSet.add(new ArrayDef(arrayName, i));
						} else if (size instanceof Local) {
							String sizeVariableName = size.toString();
							i = analysis.getVarIntervalBefore(s,
									sizeVariableName);

							/*
							 * if array size interval has negative edges, null
							 * them
							 */
							if (i.getLowerBound() < 0) i.setLowerBound(0);
							if (i.getUpperBound() < 0) i.setUpperBound(0);

							genSet.add(new ArrayDef(arrayName, i));
						}
						/* Create kill set for the array definition */
						killSet.add(new ArrayDef(arrayName, i));
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

			if (lhs.getType() instanceof ArrayType) {
				String leftArrayName = lhs.toString();

				/* Kill previous array definition */
				ArrayDef adToKill = flowSetContain(in, leftArrayName);
				if (adToKill != null) {

					killSet.add(adToKill);
					killSet.union(unitToKillSet.get(s));

					// unitToKillSet.put(s, killSet);
				}

				/* a[] = b where b is an array reference */
				if (rhs.getType() instanceof ArrayType) {

					String rightArrayName = rhs.toString();
					ArrayDef ad = flowSetContain(in, rightArrayName);

					if (ad != null) {
						genSet.add(new ArrayDef(leftArrayName, ad.getInterval()));
					}
				}

				/* a = foo() where a is an array reference */
				else if (rhs.getType() instanceof InvokeExpr) {

					genSet.add(new ArrayDef(leftArrayName, Interval.INF));
				}
			}
		}

		if (!killSet.isEmpty()) {
			unitToKillSet.put(s, killSet);
		}

		if (!genSet.isEmpty()) {
			unitToGenerateSet.put(s, genSet);
		}

		/* Update output, subtract kill and add gen */
		in.difference(unitToKillSet.get(s));
		in.union(unitToGenerateSet.get(s), out);

	}

	/**
	 * All paths == Union.
	 **/
	protected void merge(Object in1, Object in2, Object out) {
		FlowSet inSet1 = (FlowSet) in1, inSet2 = (FlowSet) in2, outSet = (FlowSet) out;

		FlowSet genArrayDefs = emptySet.clone();
		FlowSet killArrayDefs = emptySet.clone();

		Iterator set1Iter = inSet1.iterator();

		/* Combining array definitions - taking the minimum size */
		while (set1Iter.hasNext()) {
			ArrayDef ad1 = (ArrayDef) set1Iter.next();
			ArrayDef ad2 = flowSetContain(inSet2, ad1.getName());
			if (ad2 != null) {
				genArrayDefs.add(new ArrayDef(ad1.getName(), Interval.combine(
						ad1.getInterval(), ad2.getInterval())));
				killArrayDefs.add(ad1);
				killArrayDefs.add(ad2);
			}
		}

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

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

import java.util.*;

import soot.*;
import soot.jimple.IntConstant;
import soot.options.Options;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.*;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class LocalsIntervals {

	protected Map<Unit, List>	unitToLocalsBefore;
	protected Map<Unit, List>	unitToLocalsAfter;

	public LocalsIntervals(UnitGraph graph) {
		if (Options.v().verbose()) G.v().out.println("["
				+ graph.getBody().getMethod().getName()
				+ "]     Constructing GuaranteedDefs...");

		LocalIntervalsAnalysis analysis = new LocalIntervalsAnalysis(graph);

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

	public List getLiveLocalsAfter(Unit s) {
		return unitToLocalsAfter.get(s);
	}

	public List getLiveLocalsBefore(Unit s) {
		return unitToLocalsBefore.get(s);
	}
}

@SuppressWarnings({ "unchecked", "rawtypes" })
class LocalIntervalsAnalysis extends ForwardFlowAnalysis {

	FlowSet				emptySet			= new ArraySparseSet();
	Map<Unit, FlowSet>	unitToGenerateSet	= new HashMap<Unit, FlowSet>(
													graph.size() * 2 + 1, 0.7f);
	Map<Unit, FlowSet>	unitToKillSet		= new HashMap<Unit, FlowSet>(
													graph.size() * 2 + 1, 0.7f);

	LocalIntervalsAnalysis(UnitGraph graph) {
		super(graph);
		Iterator unitIt = graph.iterator();

		/* Create gen and kill sets */
		while (unitIt.hasNext()) {
			Unit s = (Unit) unitIt.next();

			FlowSet genSet = emptySet.clone();
			FlowSet killSet = emptySet.clone();

			ValueBox defBox = null;
			Iterator useBoxIter = s.getUseBoxes().iterator();
			Iterator defBoxIter = s.getDefBoxes().iterator();

			if (defBoxIter.hasNext()) {
				defBox = (ValueBox) defBoxIter.next();
			}

			/* Create gen and kill sets */
			if (defBox != null && defBox.getValue() instanceof Local) {
				String variableName = defBox.getValue().toString();

				/* Create gen set for "x = IntConstant" statements */
				if (s.getUseBoxes().size() == 1) {
					ValueBox useBox = (ValueBox) useBoxIter.next();
					if (useBox.getValue() instanceof IntConstant) {
						int variableValue = ((IntConstant) useBox.getValue()).value;
						genSet.add(new VarInterval(new Interval(variableValue,
								variableValue), variableName), genSet);
					}
				}
				/* Create kill set for the variable definition */
				killSet.add(variableName, killSet);
			}
			/* This is not a local variable definition */
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

		Unit s = (Unit) unit;
		Iterator useBoxIter = s.getUseBoxes().iterator();
		Iterator defBoxIter = s.getDefBoxes().iterator();
		ValueBox defBox = null;

		if (defBoxIter.hasNext()) {
			defBox = (ValueBox) defBoxIter.next();
		}

		if (defBox != null && defBox.getValue() instanceof Local) {
			String variableName = defBox.getValue().toString();

			if (s.getUseBoxes().size() == 1) {
				ValueBox useBox = (ValueBox) useBoxIter.next();
				/* Create gen set for "x = y" statements */
				if (useBox.getValue() instanceof Local) {
					VarInterval vi = flowSetContain(in, useBox.getValue()
							.toString());
					if (vi != null) {
						genSet.add(new VarInterval(vi.getInterval(),
								variableName), genSet);
						unitToGenerateSet.put(s, genSet);
					}
				}
			}
		}
		// perform generation (need to subtract killSet)
		in.union(unitToGenerateSet.get(unit), out);
	}

	/**
	 * All paths == Union.
	 **/
	protected void merge(Object in1, Object in2, Object out) {
		FlowSet inSet1 = (FlowSet) in1, inSet2 = (FlowSet) in2, outSet = (FlowSet) out;
		FlowSet genIntervals = emptySet.clone();

		Iterator set1Iter = inSet1.iterator();
		// Iterator set2Iter = inSet2.iterator();

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
		// set1Iter = inSet1.iterator();
		// set2Iter = inSet2.iterator();

		/* Combining intervals */
		while (set1Iter.hasNext()) {
			VarInterval vi1 = (VarInterval) set1Iter.next();
			VarInterval vi2 = flowSetContain(inSet2, vi1.getVar());
			if (vi2 != null) {
				genIntervals.add(new VarInterval(Interval.combine(
						vi1.getInterval(), vi2.getInterval()), vi1.getVar()));
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

		/* outSet = (inSet1 - inSet2) */
		inSet1.difference(inSet2, outSet);
		/* inSet2 = (inSet2 - inSet1) */
		inSet2.difference(inSet1);
		/* update outSet to contain all different elements from inSet1, inSet2 */
		outSet.union(inSet2, outSet);
		/* add to outSet the combined intervals of the common variables */
		genIntervals.union(outSet, outSet);
	}

	protected void copy(Object source, Object dest) {
		FlowSet sourceSet = (FlowSet) source, destSet = (FlowSet) dest;

		sourceSet.copy(destSet);
	}

	/*
	 * returns the corresponding varInterval from the flowSet (fs) according to
	 * viName, if the flowSet doesn't contain that varInterval returns null
	 */
	private VarInterval flowSetContain(FlowSet fs, String viName) {
		Iterator fsIter = fs.iterator();
		VarInterval vi = null;
		while (fsIter.hasNext()) {
			vi = (VarInterval) fsIter.next();
			if (vi.getVar().equals(viName)) { return vi; }
		}
		return vi;
	}
}
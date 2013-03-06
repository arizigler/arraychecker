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

	FlowSet				emptySet	= new ArraySparseSet();
	Map<Unit, FlowSet>	unitToGenerateSet;

	LocalIntervalsAnalysis(UnitGraph graph) {
		super(graph);
		unitToGenerateSet = new HashMap<Unit, FlowSet>(graph.size() * 2 + 1,
				0.7f);

		// Create generate sets
		unitToGenerateSet = new HashMap<Unit, FlowSet>(graph.size() * 2 + 1,
				0.7f);

		Iterator unitIt = graph.iterator();
		while (unitIt.hasNext()) {
			Unit s = (Unit) unitIt.next();

			FlowSet genSet = emptySet.clone();

			Iterator useBoxIt = s.getUseBoxes().iterator();
			Iterator defBoxIt = s.getDefBoxes().iterator();
			ValueBox defBox = null;
			if (defBoxIt.hasNext()) {
				defBox = (ValueBox) defBoxIt.next();
			}
			if (defBox != null && defBox.getValue() instanceof Local) {
				while (useBoxIt.hasNext()) {
					ValueBox useBox = (ValueBox) useBoxIt.next();
					if (useBox.getValue() instanceof IntConstant) {
						IntConstant c = (IntConstant) useBox.getValue();
						genSet.add(new VarInterval(c.value, c.value, defBox
								.getValue().toString()), genSet);
					}
				}
			}
			unitToGenerateSet.put(s, genSet);
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

		// perform generation (kill set is empty)
		in.union(unitToGenerateSet.get(unit), out);
	}

	/**
	 * All paths == Union.
	 **/
	protected void merge(Object in1, Object in2, Object out) {
		FlowSet inSet1 = (FlowSet) in1, inSet2 = (FlowSet) in2, outSet = (FlowSet) out;

		inSet1.union(inSet2, outSet);
		// inSet1.intersection(inSet2, outSet);
	}

	protected void copy(Object source, Object dest) {
		FlowSet sourceSet = (FlowSet) source, destSet = (FlowSet) dest;

		sourceSet.copy(destSet);
	}
}
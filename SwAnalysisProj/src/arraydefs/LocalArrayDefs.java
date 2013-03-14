package arraydefs;

import intervals.Interval;
import intervals.LocalsInterval;

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

	// private long getLowerBound(List localsIntervalBefore, String
	// variableName) {
	// Iterator fsIter = localsIntervalBefore.iterator();
	// VarInterval vi = null;
	// while (fsIter.hasNext()) {
	// vi = (VarInterval) fsIter.next();
	// if (vi.getVar().equals(variableName)) { return vi.getInterval()
	// .getLowerBound(); }
	// }
	// return 0;
	// }

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
						genSet.add(new ArrayDef(localName, ad.getInterval()),
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

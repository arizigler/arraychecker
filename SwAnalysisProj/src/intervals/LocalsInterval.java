package intervals;

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
import soot.jimple.*;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.*;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class LocalsInterval {

	protected Map<Unit, List>	unitToLocalsBefore;
	protected Map<Unit, List>	unitToLocalsAfter;

	public LocalsInterval(UnitGraph graph) {
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

	public List getLocalsIntervalBefore(Unit s) {
		return unitToLocalsBefore.get(s);
	}

	public List getLocalsIntervalAfter(Unit s) {
		return unitToLocalsAfter.get(s);
	}

	public Interval getVarIntervalBefore(Unit s, String var) {

		Iterator intervalsIter = getLocalsIntervalBefore(s).iterator();
		VarInterval vi = null;

		while (intervalsIter.hasNext()) {
			vi = (VarInterval) intervalsIter.next();
			if (vi.getVar().equals(var)) return vi.getInterval();
		}

		return null;
	}
}

@SuppressWarnings({ "unchecked", "rawtypes" })
class LocalIntervalsAnalysis extends ForwardFlowAnalysis {

	FlowSet				emptySet			= new ArraySparseSet();
	Map<Unit, FlowSet>	unitToGenerateSet	= new HashMap<Unit, FlowSet>(
													graph.size() * 2 + 1, 0.7f);
	Map<Unit, FlowSet>	unitToKillSet		= new HashMap<Unit, FlowSet>(
													graph.size() * 2 + 1, 0.7f);
	Map<Unit, Integer>	unitToVisitCount	= new HashMap<Unit, Integer>(
													graph.size() * 2 + 1, 0.7f);
	int					maxUnitVisit		= 3;

	LocalIntervalsAnalysis(UnitGraph graph) {
		super(graph);
		Iterator unitIt = graph.iterator();

		/* Create gen and kill sets */
		while (unitIt.hasNext()) {
			Unit s = (Unit) unitIt.next();
			unitToVisitCount.put(s, 0);
			FlowSet genSet = emptySet.clone();
			FlowSet killSet = emptySet.clone();

			// if (s instanceof AssignStmt) {
			// Value lhs = ((AssignStmt) s).getLeftOp();
			// Value rhs = ((AssignStmt) s).getRightOp();
			//
			// if (lhs instanceof Local && rhs instanceof IntConstant) genSet
			// .add(new VarInterval(lhs.toString(), new Interval(
			// ((IntConstant) rhs).value,
			// ((IntConstant) rhs).value)));
			//
			// /* Create kill set for the variable definition */
			// killSet.add(new VarInterval(lhs.toString(), Interval.EMPTY));
			// }
			// /* This is not a local variable definition */
			// else {
			// /* nothing to do */
			// }
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
		unitToVisitCount.put(s, unitToVisitCount.get(s) + 1);

		Stmt stmt = (Stmt) s;

		if (stmt.containsInvokeExpr()) {
			FlowSet objFieldsToKill = getAllObjField(in);
			killSet.union(objFieldsToKill);
			killSet.union(unitToKillSet.get(s));
			unitToKillSet.put(s, killSet);
			FlowSet objFieldsToGen = changeAllToINF(objFieldsToKill);
			genSet.union(objFieldsToGen);
		}

		/* Assign statements */
		if (s instanceof AssignStmt) {

			Value lhs = ((AssignStmt) s).getLeftOp();
			Value rhs = ((AssignStmt) s).getRightOp();

			if ((lhs instanceof Local || lhs instanceof FieldRef)
					&& lhs.getType() instanceof IntType) {
				String variableName = lhs.toString();

				/* Kill previous interval */
				VarInterval viToKill = flowSetContain(in, variableName);
				if (viToKill != null) {
					killSet.add(viToKill);
					killSet.union(unitToKillSet.get(s));

					unitToKillSet.put(s, killSet);
				}
				VarInterval vi = null;

				/* x = const */
				if (rhs instanceof IntConstant) {
					vi = new VarInterval(variableName, new Interval(
							((IntConstant) rhs).value,
							((IntConstant) rhs).value));
				}

				/* x = y or x = o.y or x = arr[y] */
				else if (rhs instanceof Local || rhs instanceof FieldRef
						|| rhs instanceof ArrayRef) {
					VarInterval rhsVi = flowSetContain(in, rhs.toString());
					// TODO : maybe assert in case it null (only for local)
					if (rhsVi != null) {
						vi = new VarInterval(variableName, rhsVi.getInterval());
					} else {
						vi = new VarInterval(variableName, new Interval(
								Interval.NEGATIVE_INF, Interval.POSITIVE_INF));
					}
				}

				/*
				 * x = foo() (we do not implement inter-procedural analysis,
				 * therefore we must assume [-INF,INF]). Also all object's field
				 * that we track we make them [-INF,INF] in case the method
				 * change the field value.
				 */
				else if (rhs instanceof InvokeExpr) {
					vi = new VarInterval(variableName, new Interval(
							Interval.NEGATIVE_INF, Interval.POSITIVE_INF));
				}

				/* Binary operations */
				else if (rhs instanceof BinopExpr) {
					/* x = a + b */
					if (rhs instanceof AddExpr) {
						Value op1 = ((AddExpr) rhs).getOp1();
						Value op2 = ((AddExpr) rhs).getOp2();
						vi = addExprInterval(variableName, op1, op2, in);
					}

					/* x = a - b */
					else if (rhs instanceof SubExpr) {
						Value op1 = ((SubExpr) rhs).getOp1();
						Value op2 = ((SubExpr) rhs).getOp2();
						vi = subExprInterval(variableName, op1, op2, in);
					}

					/* x = a * b */
					else if (rhs instanceof MulExpr) {
						Value op1 = ((MulExpr) rhs).getOp1();
						Value op2 = ((MulExpr) rhs).getOp2();
						vi = mulExprInterval(variableName, op1, op2, in);
					}

					/* x = a / b */
					else if (rhs instanceof DivExpr) {
						Value op1 = ((DivExpr) rhs).getOp1();
						Value op2 = ((DivExpr) rhs).getOp2();
						vi = divExprInterval(variableName, op1, op2, in);
					}
				}

				/* Unary operations */
				else if (rhs instanceof NegExpr) {
					Value op = ((NegExpr) rhs).getOp();
					vi = negExprInterval(variableName, op, in);
				}

				/*
				 * For all other creatures we do not their values, must assume
				 * [-INF,INF]
				 */
				else {
					vi = new VarInterval(variableName, new Interval(
							Interval.NEGATIVE_INF, Interval.POSITIVE_INF));
				}

				if (unitToVisitCount.get(s) > maxUnitVisit && vi != null
						&& viToKill != null) {
					Interval ci = Interval.convergentInterval(
							viToKill.getInterval(), vi.getInterval());
					genSet.add(new VarInterval(variableName, ci));
				} else if (vi != null) {
					genSet.add(vi);
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
		in.difference(unitToKillSet.get(unit));
		in.union(unitToGenerateSet.get(unit), out);

		/* Debug prints */
		// G.v().out.println("in= " + in + " kill= " + unitToKillSet.get(unit)
		// + " gen= " + unitToGenerateSet.get(unit) + " out=" + out);
	}

	/**
	 * All paths == Union.
	 **/
	protected void merge(Object in1, Object in2, Object out) {
		FlowSet inSet1 = (FlowSet) in1, inSet2 = (FlowSet) in2, outSet = (FlowSet) out;
		FlowSet genIntervals = emptySet.clone();
		FlowSet killIntervals = emptySet.clone();

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
				genIntervals.add(new VarInterval(vi1.getVar(), Interval
						.combine(vi1.getInterval(), vi2.getInterval())));
				killIntervals.add(vi1);
				killIntervals.add(vi2);
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
		/* remove combined intervals from outSet */
		outSet.difference(killIntervals);
		/* add to outSet the combined intervals of the common variables */
		genIntervals.union(outSet, outSet);
	}

	protected void copy(Object source, Object dest) {
		FlowSet sourceSet = (FlowSet) source, destSet = (FlowSet) dest;

		sourceSet.copy(destSet);
	}

	/*
	 * Returns the corresponding varInterval from the flowSet (fs) according to
	 * viName, if the flowSet doesn't contain that varInterval returns null
	 */
	private VarInterval flowSetContain(FlowSet fs, String viName) {
		Iterator fsIter = fs.iterator();
		VarInterval vi = null;
		while (fsIter.hasNext()) {
			vi = (VarInterval) fsIter.next();
			if (vi.getVar().equals(viName)) { return vi; }
		}
		return null;
	}

	private Interval getInterval(Value val, FlowSet fs) {

		/* Value is a constant */
		if (val instanceof IntConstant) {
			return new Interval(((IntConstant) val).value,
					((IntConstant) val).value);
		}
		/* Value is a local or FieldRef */
		else if (val instanceof Local || val instanceof FieldRef) {
			VarInterval vi = flowSetContain(fs, val.toString());
			if (vi != null) { return vi.getInterval(); }
		}
		return null;
	}

	private VarInterval addExprInterval(String defName, Value op1, Value op2,
			FlowSet in) {
		Interval i1 = getInterval(op1, in);
		Interval i2 = getInterval(op2, in);
		return new VarInterval(defName, Interval.addExpr(i1, i2));
	}

	private VarInterval subExprInterval(String defName, Value op1, Value op2,
			FlowSet in) {
		Interval i1 = getInterval(op1, in);
		Interval i2 = getInterval(op2, in);
		return new VarInterval(defName, Interval.subExpr(i1, i2));
	}

	private VarInterval negExprInterval(String defName, Value op1, FlowSet in) {
		Interval i1 = getInterval(op1, in);
		return new VarInterval(defName, Interval.negExpr(i1));
	}

	private VarInterval mulExprInterval(String defName, Value op1, Value op2,
			FlowSet in) {
		Interval i1 = getInterval(op1, in);
		Interval i2 = getInterval(op2, in);
		return new VarInterval(defName, Interval.mul(i1, i2));
	}

	private VarInterval divExprInterval(String defName, Value op1, Value op2,
			FlowSet in) {
		Interval i1 = getInterval(op1, in);
		Interval i2 = getInterval(op2, in);

		/* Special case: op1 == op2 */
		if (op1 == op2) { return new VarInterval(defName, new Interval(1, 1)); }

		return new VarInterval(defName, Interval.div(i1, i2));
	}

	private FlowSet getAllObjField(FlowSet fs) {
		FlowSet objFieldsSet = emptySet.clone();
		Iterator fsIter = fs.iterator();
		VarInterval vi = null;
		while (fsIter.hasNext()) {
			vi = (VarInterval) fsIter.next();
			if (vi.getVar().contains(".")) {
				objFieldsSet.add(vi);
			}
		}
		return objFieldsSet;
	}

	private FlowSet changeAllToINF(FlowSet fs) {
		FlowSet objFieldsINFSet = emptySet.clone();
		Iterator fsIter = fs.iterator();
		VarInterval vi = null;
		while (fsIter.hasNext()) {
			vi = (VarInterval) fsIter.next();
			objFieldsINFSet.add(new VarInterval(vi.getVar(), new Interval(
					Interval.NEGATIVE_INF, Interval.POSITIVE_INF)));
		}
		return objFieldsINFSet;
	}

}

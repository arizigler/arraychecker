package intervals;

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
		BLocalIntervalsAnalysis analysis = new BLocalIntervalsAnalysis(graph);

		// Build unitToLocals map
		unitToLocalsAfter = new HashMap<Unit, List>(graph.size() * 2 + 1, 0.7f);
		unitToLocalsBefore = new HashMap<Unit, List>(graph.size() * 2 + 1, 0.7f);

		Iterator unitIt = graph.iterator();

		while (unitIt.hasNext()) {
			Unit s = (Unit) unitIt.next();

			FlowSet set = (FlowSet) analysis.getFlowBefore(s);
			unitToLocalsBefore.put(s,
					Collections.unmodifiableList(set.toList()));

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

@SuppressWarnings({ "rawtypes" })
class BLocalIntervalsAnalysis extends ForwardBranchedFlowAnalysis {

	FlowSet				emptySet			= new ArraySparseSet();
	Map<Unit, FlowSet>	unitToGenerateSet	= new HashMap<Unit, FlowSet>(
													graph.size() * 2 + 1, 0.7f);
	Map<Unit, FlowSet>	unitToKillSet		= new HashMap<Unit, FlowSet>(
													graph.size() * 2 + 1, 0.7f);
	Map<Unit, Integer>	unitToVisitCount	= new HashMap<Unit, Integer>(
													graph.size() * 2 + 1, 0.7f);

	int					maxUnitVisit		= 50;

	BLocalIntervalsAnalysis(UnitGraph graph) {
		super(graph);
		Iterator unitIt = graph.iterator();

		/* Create gen and kill sets */
		while (unitIt.hasNext()) {
			Unit s = (Unit) unitIt.next();
			unitToVisitCount.put(s, 0);
			FlowSet genSet = emptySet.clone();
			FlowSet killSet = emptySet.clone();

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

	@Override
	protected void flowThrough(Object inValue, Unit u, List fallOut,
			List branchOuts) {

		FlowSet in = (FlowSet) inValue;
		FlowSet genSet = emptySet.clone();
		FlowSet killSet = emptySet.clone();

		unitToVisitCount.put(u, unitToVisitCount.get(u) + 1);

		/* If statement */
		if (u instanceof IfStmt) {

			IfStmt ifStmt = (IfStmt) u;
			Value condition = ifStmt.getCondition();

			/* Constructing branchOuts */
			for (Iterator it = branchOuts.iterator(); it.hasNext();) {

				FlowSet fs = (FlowSet) it.next();
				FlowSet newSet = cloneSet(in);

				analyzeCondition(true, condition, newSet);
				newSet.union(unitToGenerateSet.get(u), fs);

			}
			/* Constructing fallOuts */
			for (Iterator it = fallOut.iterator(); it.hasNext();) {

				FlowSet fs = (FlowSet) it.next();
				FlowSet newSet = cloneSet(in);

				analyzeCondition(false, condition, newSet);
				newSet.union(unitToGenerateSet.get(u), fs);
			}
		}

		/* Not an if statement */
		else {
			Stmt stmt = (Stmt) u;

			if (stmt.containsInvokeExpr()) {
				FlowSet objFieldsToKill = getAllObjField(in);
				killSet.union(objFieldsToKill);
				killSet.union(unitToKillSet.get(u));
				unitToKillSet.put(u, killSet);
				FlowSet objFieldsToGen = changeAllToINF(objFieldsToKill);
				genSet.union(objFieldsToGen);
			}

			/* Assign statements */
			if (u instanceof AssignStmt) {

				Value lhs = ((AssignStmt) u).getLeftOp();
				Value rhs = ((AssignStmt) u).getRightOp();

				if ((lhs instanceof Local || lhs instanceof FieldRef)
						&& lhs.getType() instanceof IntType) {
					String variableName = lhs.toString();

					/* Kill previous interval */
					VarInterval viToKill = flowSetContain(in, variableName);
					if (viToKill != null) {
						killSet.add(viToKill);
						killSet.union(unitToKillSet.get(u));

						unitToKillSet.put(u, killSet);
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
							vi = new VarInterval(variableName,
									rhsVi.getInterval());
						} else {
							vi = new VarInterval(variableName, new Interval(
									Interval.NEGATIVE_INF,
									Interval.POSITIVE_INF));
						}
					}

					/*
					 * x = foo() (we do not implement inter-procedural analysis,
					 * therefore we must assume [-INF,INF]). Also all object's
					 * field that we track we make them [-INF,INF] in case the
					 * method change the field value.
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
					 * For all other creatures we do not their values, must
					 * assume [-INF,INF]
					 */
					else {
						vi = new VarInterval(variableName, new Interval(
								Interval.NEGATIVE_INF, Interval.POSITIVE_INF));
					}

					if (unitToVisitCount.get(u) > maxUnitVisit && vi != null
							&& viToKill != null) {
						Interval ci = Interval.convergentInterval(
								viToKill.getInterval(), vi.getInterval());
						genSet.add(new VarInterval(variableName, ci));
					} else if (vi != null) {
						if ((viToKill != null)
								&& (viToKill.getInterval().isBottom())) vi
								.getInterval().setBottom(true);
						genSet.add(vi);
					}
				}

			}

			if (!genSet.isEmpty()) {
				unitToGenerateSet.put(u, genSet);
			}

			if (!killSet.isEmpty()) {
				unitToKillSet.put(u, killSet);
			}

			/* Subtract kill */
			in.difference(unitToKillSet.get(u));

			// in.union(unitToGenerateSet.get(u), out);

			/* Constructing branchOuts */
			for (Iterator it = branchOuts.iterator(); it.hasNext();) {

				FlowSet fs = (FlowSet) it.next();
				in.union(unitToGenerateSet.get(u), fs);

			}
			/* Constructing fallOuts */
			for (Iterator it = fallOut.iterator(); it.hasNext();) {

				FlowSet fs = (FlowSet) it.next();
				in.union(unitToGenerateSet.get(u), fs);
			}
		}
	}

	/**
	 * All paths == Union.
	 **/
	protected void merge(Object in1, Object in2, Object out) {

		FlowSet inSet1 = (FlowSet) in1, inSet2 = (FlowSet) in2, outSet = (FlowSet) out;
		FlowSet genIntervals = emptySet.clone();
		FlowSet killIntervals = emptySet.clone();

		Iterator set1Iter = inSet1.iterator();

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

	private void analyzeCondition(boolean holds, Value cond, FlowSet in) {

		if (cond instanceof BinopExpr) {

			BinopExpr expr = (BinopExpr) cond;
			Value leftOp = expr.getOp1();
			Value rightOp = expr.getOp2();

			/* Creating Intervals for left and right */
			VarInterval viLeft = valueToVarInterval("left", leftOp, in);
			VarInterval viRight = valueToVarInterval("right", rightOp, in);

			if (viLeft != null && viRight != null) {

				/* a < b */
				if (cond instanceof LtExpr) {

					if (holds) {
						pairLT(viLeft, viRight);
					} else {
						pairLE(viRight, viLeft);
					}
				}

				/* a <= b */
				else if (cond instanceof LeExpr) {

					if (holds) {
						pairLE(viLeft, viRight);
					} else {
						pairLT(viRight, viLeft);
					}
				}

				/* a > b */
				else if (cond instanceof GtExpr) {

					if (holds) {
						pairLT(viRight, viLeft);
					} else {
						pairLE(viLeft, viRight);
					}
				}
				/* a >= b */
				else if (cond instanceof GtExpr) {

					if (holds) {
						pairLE(viRight, viLeft);
					} else {
						pairLT(viLeft, viRight);
					}
				}
				/* a == b */
				else if (cond instanceof EqExpr) {

					if (holds) {
						pairEq(viRight, viLeft);
					}
					/* else nothing should be done */
				}

				/* a != b */
				else if (cond instanceof NeExpr) {

					if (!holds) {
						pairEq(viRight, viLeft);
					}
					/* else nothing should be done */
				}
			}
		}
	}

	private VarInterval valueToVarInterval(String varName, Value val, FlowSet in) {

		if (val instanceof IntConstant) {
			int value = ((IntConstant) val).value;
			return new VarInterval(varName, new Interval(value, value));
		} else if (val instanceof Local) { return flowSetContain(in,
				val.toString()); }

		return null;
	}

	/* Pair< */
	private void pairLT(VarInterval viLeft, VarInterval viRight) {

		Interval li = viLeft.getInterval();
		Interval ri = viRight.getInterval();

		/* In case of bottom */
		if (li.isBottom() || ri.isBottom()) {
			viLeft.setInterval(Interval.BOTTOM);
			viRight.setInterval(Interval.BOTTOM);
		} else {
			/* Compute new intervals */
			Interval newLeftInterval = new Interval(li.getLowerBound(),
					Math.min(li.getUpperBound(), ri.getUpperBound() - 1));

			Interval newRightInterval = new Interval(Math.max(
					li.getLowerBound(), ri.getLowerBound()), ri.getUpperBound());

			/* Invoke meet on the result */
			viLeft.setInterval(Interval.meet(newLeftInterval));
			viRight.setInterval(Interval.meet(newRightInterval));
		}

	}

	/* Pair<= */
	private void pairLE(VarInterval viLeft, VarInterval viRight) {

		Interval li = viLeft.getInterval();
		Interval ri = viRight.getInterval();

		/* In case of bottom */
		if (li.isBottom() || ri.isBottom()) {
			viLeft.setInterval(Interval.BOTTOM);
			viRight.setInterval(Interval.BOTTOM);
		} else {
			/* Compute new intervals */
			Interval newLeftInterval = new Interval(li.getLowerBound(),
					Math.min(li.getUpperBound(), ri.getUpperBound()));

			Interval newRightInterval = new Interval(Math.max(
					li.getLowerBound(), ri.getLowerBound()), ri.getUpperBound());

			/* Invoke meet on the result */
			viLeft.setInterval(Interval.meet(newLeftInterval));
			viRight.setInterval(Interval.meet(newRightInterval));
		}

	}

	/* Pair== */
	private void pairEq(VarInterval viLeft, VarInterval viRight) {

		Interval li = viLeft.getInterval();
		Interval ri = viRight.getInterval();

		/* Bottoms are handled by Interval intersection */
		Interval intersection = Interval.getIntersection(li, ri);

		viLeft.setInterval(intersection);
		viRight.setInterval(intersection);
	}

	private FlowSet cloneSet(FlowSet in) {

		FlowSet newSet = emptySet.clone();

		Iterator iter = in.iterator();
		while (iter.hasNext()) {
			VarInterval vi = (VarInterval) iter.next();
			newSet.add(vi.clone());
		}
		return newSet;
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

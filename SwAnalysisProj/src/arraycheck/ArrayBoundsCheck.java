package arraycheck;

import intervals.Interval;
import intervals.LocalsInterval;

import java.util.*;

import soot.*;
import soot.jimple.*;
import soot.toolkits.graph.UnitGraph;
import arraydefs.ArrayDef;
import arraydefs.LocalArrayDefs;

@SuppressWarnings({ "rawtypes" })
public class ArrayBoundsCheck {

	protected Map<Unit, String>	unitToIllegalAccess;
	private static String		unsafeLower			= "Unsafe Array Access: Illegal Lower Bound";
	private static String		unsafeUpper			= "Unsafe Array Access: Illegal Upper Bound";
	private static String		pUnsafeLowerUpper	= "Unsafe Array Access: Potentially Illegal Lower and Upper Bounds";
	private static String		pUnsafeLower		= "Unsafe Array Access: Potentially Illegal Lower Bound";
	private static String		pUnsafeUpper		= "Unsafe Array Access: Potentially Illegal Upper Bound";

	public ArrayBoundsCheck(UnitGraph graph) {

		LocalsInterval intAnalysis = new LocalsInterval(graph);
		LocalArrayDefs arrDefAnalysis = new LocalArrayDefs(graph, intAnalysis);

		unitToIllegalAccess = new HashMap<Unit, String>(graph.size() * 2 + 1,
				0.7f);

		Iterator unitIter = graph.iterator();

		/* Iterating over CFG for illegal array access */
		while (unitIter.hasNext()) {
			Unit s = (Unit) unitIter.next();
			Stmt stmt = (Stmt) s;

			/* Statement contains an array reference */
			if (stmt.containsArrayRef()) {

				ArrayRef aref = stmt.getArrayRef();

				String arrayName = ((Local) aref.getBase()).toString();
				ArrayDef arrayDef = arrDefAnalysis.getArrayDefBefore(s,
						arrayName);

				ValueBox indexBox = aref.getIndexBox();
				Value index = indexBox.getValue();

				/* array[i] */
				if (index instanceof Local) {
					String indexVarName = ((Local) index).toString();

					Interval indexInterval = intAnalysis.getVarIntervalBefore(
							s, indexVarName);

					if (arrayDef != null && indexInterval != null) {

						Interval arraySizeInterval = arrayDef.getInterval();

						/* array size interval [a,b] => [0,a-1] */
						Interval arraySizeMinInterval = Interval
								.toMinArrayIndex(arraySizeInterval);

						/* array size interval [a,b] => [0,b-1] */
						Interval arraySizeMaxInterval = Interval
								.toMaxArrayIndex(arraySizeInterval);

						/* Special case: access using INF interval */
						if (indexInterval.equals(Interval.INF)) {
							unitToIllegalAccess.put(s, pUnsafeLowerUpper);
						}

						/* Definite illegal access */
						else if (!Interval.intersect(indexInterval,
								arraySizeMaxInterval)) {

							/* Definite illegal access by lower bound */
							if (Interval.isBefore(indexInterval,
									arraySizeMaxInterval)) {
								unitToIllegalAccess.put(s, unsafeLower);
							}
							/* Definite illegal access by upper bound */
							else {
								unitToIllegalAccess.put(s, unsafeUpper);
							}
						}

						/* Special case: array[0] where array is of size 0 */
						else if (arraySizeMinInterval.equals(Interval.ZERO)
								&& indexInterval.equals(Interval.ZERO)) {
							unitToIllegalAccess.put(s, unsafeUpper);
						}

						/* Potential illegal access by lower bound */
						else if (indexInterval.getLowerBound() < arraySizeMinInterval
								.getLowerBound()) {
							if (indexInterval.getUpperBound() == Interval.POSITIVE_INF) {
								unitToIllegalAccess.put(s, pUnsafeLowerUpper);
							} else unitToIllegalAccess.put(s, pUnsafeLower);
						}
						/* Potential illegal access by upper bound */
						else if (indexInterval.getUpperBound() > arraySizeMinInterval
								.getUpperBound()) {
							if (indexInterval.getLowerBound() == Interval.NEGATIVE_INF) {
								unitToIllegalAccess.put(s, pUnsafeLowerUpper);
							} else unitToIllegalAccess.put(s, pUnsafeUpper);
						}
					}
				}

				/* array[5] */
				else if (indexBox instanceof IntConstant) {
					if (arrayDef != null) {
						/* TODO: check if this case is possible */
					}
				}
			}
		}
	}

	public String getAccessNotification(Unit s) {
		return unitToIllegalAccess.get(s);
	}
}

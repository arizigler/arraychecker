package arraycheck;

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
import arraydefs.ArrayDef;
import arraydefs.BLocalArrayDefs;

@SuppressWarnings({ "rawtypes" })
public class BArrayBoundsCheck {

	protected Map<Unit, String>	unitToIllegalAccess;
	private static String		unsafeLower			= "Unsafe Array Access: Illegal Lower Bound";
	private static String		unsafeUpper			= "Unsafe Array Access: Illegal Upper Bound";
	private static String		pUnsafeLowerUpper	= "Unsafe Array Access: Potentially Illegal Lower and Upper Bounds";
	private static String		pUnsafeLower		= "Unsafe Array Access: Potentially Illegal Lower Bound";
	private static String		pUnsafeUpper		= "Unsafe Array Access: Potentially Illegal Upper Bound";

	public BArrayBoundsCheck(UnitGraph graph) {

		BLocalsInterval intAnalysis = new BLocalsInterval(graph);
		BLocalArrayDefs arrDefAnalysis = new BLocalArrayDefs(graph, intAnalysis);

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

						/*
						 * If array size is BOTTOM , or array index is BOTTOM,
						 * no need to declare overflow.
						 */
						if (!(arraySizeInterval.isBottom() || indexInterval
								.isBottom())) {

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
									unitToIllegalAccess.put(s,
											pUnsafeLowerUpper);
								} else unitToIllegalAccess.put(s, pUnsafeLower);
							}
							/* Potential illegal access by upper bound */
							else if (indexInterval.getUpperBound() > arraySizeMinInterval
									.getUpperBound()) {
								if (indexInterval.getLowerBound() == Interval.NEGATIVE_INF) {
									unitToIllegalAccess.put(s,
											pUnsafeLowerUpper);
								} else unitToIllegalAccess.put(s, pUnsafeUpper);
							}

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

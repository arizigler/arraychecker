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

	protected Map<Unit, List>	unitToLocalsBefore;
	protected Map<Unit, List>	unitToLocalsAfter;

	public ArrayBoundsCheck(UnitGraph graph) {

		LocalsInterval intAnalysis = new LocalsInterval(graph);
		LocalArrayDefs arrDefAnalysis = new LocalArrayDefs(graph, intAnalysis);

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

				Value index = aref.getIndex();

				/* array[i] */
				if (index instanceof Local) {
					String indexVarName = ((Local) index).toString();

					Interval indexInterval = intAnalysis.getVarIntervalBefore(
							s, indexVarName);

					if (arrayDef != null && indexInterval != null) {
						G.v().out.println("WARNING: array size is: "
								+ arrayDef.getSize() + ", however " + arrayName
								+ " index is in the interval: "
								+ indexInterval.toString());
					}
				}

				/* array[5] */
				else if (index instanceof IntConstant) {

					if (arrayDef != null) {
						G.v().out.println("WARNING: array size is: "
								+ arrayDef.getSize() + ", however " + arrayName
								+ " index is: " + ((IntConstant) index).value);
					}
				}
			}
		}
	}

}

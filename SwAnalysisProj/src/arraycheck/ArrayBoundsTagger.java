package arraycheck;

import java.util.Iterator;
import java.util.Map;

import soot.*;
import soot.tagkit.ColorTag;
import soot.tagkit.StringTag;
import soot.toolkits.graph.ExceptionalUnitGraph;

@SuppressWarnings({ "rawtypes" })
public class ArrayBoundsTagger extends BodyTransformer {

	public static final String			PHASE_NAME	= "abctagger";
	public static final String			TAG_TYPE	= "Array Bounds Check";

	private static ArrayBoundsTagger	instance	= new ArrayBoundsTagger();

	private ArrayBoundsTagger() {}

	public static ArrayBoundsTagger v() {
		return instance;
	}

	protected void internalTransform(Body b, String phaseName, Map options) {

		ArrayBoundsCheck abc = new ArrayBoundsCheck(new ExceptionalUnitGraph(b));
		Iterator unitIt = b.getUnits().iterator();

		// LocalsInterval intAnalysis = new LocalsInterval(
		// new ExceptionalUnitGraph(b));
		// LocalArrayDefs arrDefAnalysis = new LocalArrayDefs(
		// new ExceptionalUnitGraph(b), intAnalysis);
		//
		// while (unitIt.hasNext()) {
		// Unit s = (Unit) unitIt.next();
		// Stmt stmt = (Stmt) s;
		//
		// /* Statement contains an array reference */
		// if (stmt.containsArrayRef()) {
		//
		// ArrayRef aref = stmt.getArrayRef();
		//
		// String arrayName = ((Local) aref.getBase()).toString();
		// ArrayDef arrayDef = arrDefAnalysis.getArrayDefBefore(s,
		// arrayName);
		//
		// ValueBox indexBox = aref.getIndexBox();
		// Value index = indexBox.getValue();
		//
		// /* array[i] */
		// if (index instanceof Local) {
		// String indexVarName = ((Local) index).toString();
		//
		// Interval indexInterval = intAnalysis.getVarIntervalBefore(
		// s, indexVarName);
		//
		// if (arrayDef != null && indexInterval != null) {
		//
		// if (arrayDef.getInterval().getLowerBound() < indexInterval
		// .getUpperBound()) {
		//
		// indexBox.addTag(new ColorTag(ColorTag.RED, TAG_TYPE));
		// indexBox.addTag(new StringTag("Array bounds error"));
		//
		// // s.addTag(new ColorTag(ColorTag.BLUE, TAG_TYPE));
		// // s.addTag(new StringTag("Array bounds error"));
		//
		// G.v().out.println("index box is: "
		// + indexBox.toString());
		//
		// // G.v().out.println("WARNING: array size interval: "
		// // + arrayDef.getInterval().toString()
		// // + ", however " + arrayName
		// // + " index is in the interval: "
		// // + indexInterval.toString());
		// }
		//
		// }
		// }
		// }

		while (unitIt.hasNext()) {
			Unit s = (Unit) unitIt.next();

			String notification = abc.getAccessNotification(s);

			if (notification != null) {
				// vb.addTag(new ColorTag(ColorTag.BLUE, false, TAG_TYPE));
				s.addTag(new StringTag(notification, TAG_TYPE));
				s.addTag(new ColorTag(ColorTag.RED, false, TAG_TYPE));
			}

			// Iterator usesIt = s.getUseBoxes().iterator();
			// while (usesIt.hasNext()) {
			// ValueBox use = (ValueBox) usesIt.next();
			// if (true) {
			// // use.addTag(new ColorTag(ColorTag.RED, TAG_TYPE));
			// G.v().out.println("I am doing something");
			// }
			// }
		}
	}
}

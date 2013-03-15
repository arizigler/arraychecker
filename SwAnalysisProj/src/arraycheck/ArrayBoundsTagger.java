package arraycheck;

import java.util.Iterator;
import java.util.Map;

import soot.*;
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

		ArrayBoundsCheck abc = new ArrayBoundsCheck(new ExceptionalUnitGraph(
				b));
		Iterator unitIt = b.getUnits().iterator();

		while (unitIt.hasNext()) {
			Unit s = (Unit) unitIt.next();

			String notification = abc.getAccessNotification(s);

			if (notification != null) {
				s.addTag(new StringTag(notification, TAG_TYPE));
			}
		}
	}
}

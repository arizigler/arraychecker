package arraycheck;

import java.util.Iterator;
import java.util.Map;

import soot.*;
import soot.tagkit.StringTag;
import soot.toolkits.graph.ExceptionalUnitGraph;

@SuppressWarnings({ "rawtypes" })
public class BArrayBoundsTagger extends BodyTransformer {

	public static final String			PHASE_NAME	= "abctagger";
	public static final String			TAG_TYPE	= "Array Bounds Check";

	private static BArrayBoundsTagger	instance	= new BArrayBoundsTagger();

	private BArrayBoundsTagger() {}

	public static BArrayBoundsTagger v() {
		return instance;
	}

	protected void internalTransform(Body b, String phaseName, Map options) {

		BArrayBoundsCheck abc = new BArrayBoundsCheck(new ExceptionalUnitGraph(
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

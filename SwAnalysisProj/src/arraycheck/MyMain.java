package arraycheck;

import soot.PackManager;
import soot.Transform;

public class MyMain {

	public static void main(String[] args) {
		PackManager
				.v()
				.getPack("jtp")
				.add(new Transform("jtp.myTransform"
						+ ArrayBoundsTagger.PHASE_NAME, ArrayBoundsTagger.v()));

		soot.Main.main(args);
	}

}
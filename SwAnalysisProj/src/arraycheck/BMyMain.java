package arraycheck;

import soot.PackManager;
import soot.Transform;

public class BMyMain {

	public static void main(String[] args) {
		PackManager
				.v()
				.getPack("jtp")
				.add(new Transform("jtp.myTransform"
						+ BArrayBoundsTagger.PHASE_NAME, BArrayBoundsTagger.v()));

		soot.Main.main(args);
	}

}
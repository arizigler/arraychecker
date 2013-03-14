package arraydefs;

import intervals.LocalsInterval;

import java.util.Map;

import soot.*;
import soot.toolkits.graph.ExceptionalUnitGraph;

public class MyMain {

	public static void main(String[] args) {
		PackManager.v().getPack("jtp")
				.add(new Transform("jtp.myTransform", new BodyTransformer() {

					protected void internalTransform(Body body, String phase,
							Map options) {
						new LocalArrayDefs(new ExceptionalUnitGraph(body),
								new LocalsInterval(new ExceptionalUnitGraph(
										body)));
						// use G.v().out instead of System.out so that Soot can
						// redirect this output to the Eclipse console
						G.v().out.println(body.getMethod());
					}

				}));

		soot.Main.main(args);
	}

}
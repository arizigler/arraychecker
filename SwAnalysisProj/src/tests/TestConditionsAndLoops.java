package tests;

@SuppressWarnings("unused")
public class TestConditionsAndLoops {

	public static void main(String[] args) {

		// arrayX => array of size X
		int array0[] = new int[0];
		int array5[] = new int[5];
		int array7[] = new int[7];
		int array16[] = new int[16];

		boolean b0 = args.length == 0;
		boolean b1 = args.length == 1;

		for (int i = 0; i < 0; i++) {
			array0[i] = 5; // Good (because we won't get here
		}

		for (int i = 0; i < 7; i++) {
			array0[i] = 5; // Potentially illegal upper bound
			array5[i] = 5; // Potentially illegal upper bound
			array7[i] = 5;
		}

		int a = 1; // a is [1,1]
		int b = -3; // b is [-3,-3]

		array5[b] = 5; // Illegal lower bound

		if (b < a) {
			b = b + 4;
		}

		// now b is [1,1]
		array5[b] = 5; // Good

		if (b < a) b = b + 20;

		// b still [1,1]
		array5[b] = 5; // Good

		while (a < 15)
			a++;

		// a is [15,15]
		array16[a] = 5; // Good

		if (b0) a = a + 5;

		// now a is [15,20]
		array16[a] = 5; // Potentially illegal upper bound

		int c = -8; // c is [-8,-8]

		array16[c] = 5; // Illegal lower bound

		if ((b < a) && (c < 0)) {
			c = c + 8;
		}
	}

}

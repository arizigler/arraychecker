package tests;

@SuppressWarnings("unused")
public class TestMult {

	public static void main(String[] args) {

		// arrayX => array of size X
		int array0[] = new int[0];
		int array3[] = new int[3];
		int array5[] = new int[5];
		int array16[] = new int[16];
		int array17[] = new int[17];
		int array19[] = new int[19];
		int array200[] = new int[200];
		int array1000[] = new int[1000];

		// iX => integer with value X
		int i0 = 0;
		int i20 = 20;
		int i1 = 1;
		int i2 = 2;
		int i3 = 3;
		int i4 = 4;
		int i_5 = -5;
		int p = i2 * i_5; // p is [-10,-10]

		array0[0] = 5; // Illegal upper bound

		array0[1] = 5; // Illegal upper bound

		array5[p] = 5; // Illegal lower bound

		p = 2 * i2; // p is [4,4]

		array5[p] = 5; // Good

		p *= p; // p is [16,16]

		array16[p] = 5; // Illegal upper bound (legal index range is 0-15)

		array17[p] = 5; // Good

		p = i2 * i3; // p is [6,6]

		boolean b = args.length == 0;

		if (b) {
			p = p * i3; // p is [18,18]
		}

		// now p is [6,18]

		array17[p] = 5; // Potentially illegal upper bound
		array19[p] = 5; // Good

		p = 2; // p is [2,2]

		for (int i = 0; i < 5; i++) {
			p *= 2;
		}

		// now p is [2,INF]

		array19[p] = 5; // Potentially illegal upper bound

		p = p - 4; // p is [-2,INF]

		array1000[p] = 5; // Potentially illegal lower and upper bounds

		p = 2; // p is [2,2]

		for (int i = 0; i < 5; i++) {
			array16[i] = 5;
			p *= -2;
		}

		// p is [-INF,INF]

		array1000[p] = 5; // Potentially illegal lower and upper bounds

	}
}

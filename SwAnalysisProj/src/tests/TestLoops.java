package tests;

@SuppressWarnings("unused")
public class TestLoops {

	private final int	x	= 5;

	public static void main(String[] args) {

		// arrayX => array of size X
		int array0[] = new int[0];
		int array5[] = new int[5];
		int array7[] = new int[7];
		int array16[] = new int[16];
		int array17[] = new int[17];
		int array19[] = new int[19];
		int array1000[] = new int[1000];

		// Iv => Integer with value v
		final int I0 = 0;
		final int I1 = 1;
		final int I2 = 2;
		final int I3 = 3;
		final int I4 = 4;
		final int I20 = 20;
		final int I48 = 48;
		final int I_5 = -5;

		int p = 10;

		int x = I2; // x is [2,2]
		int y = I3; // y is [3,3]
		int z = I48; // z is [48,48]

		for (int i = 0; i < 100; i++) {
			x++;
			y--;
			z = x;
			p = 5;
		}

		// Now: x is [2,INF], y is [-INF,3], z is [3,INF], p is [5,5]

		// Proof:

		array1000[x] = 5; // Potentially illegal upper bound
		array1000[y] = 5; // Potentially illegal lower bound
		array1000[z] = 5; // Potentially illegal upper bound
		array1000[p] = 5; // Good

		boolean b1 = args.length == 0;
		boolean b2 = args.length == 1;

		while (b1) {
			p++;
			while (b2) {
				p--;
			}
		}

		// now p is [-INF,INF]

		array1000[p] = 5; // Potentially illegal lower and upper bounds

		x = 0; // x is [0,0]
		p = 0; // p is [0,0]
		for (int i = 0; i < 10; i++) {
			x++;
			p = 5;
			for (int j = 0; i < 10; i++) {
				x--;
				p++;
			}
		}

	}
}

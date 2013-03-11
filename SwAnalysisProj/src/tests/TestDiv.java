package tests;

@SuppressWarnings("unused")
public class TestDiv {

	public static void main(String[] args) {

		// arrayX => array of size X
		int array0[] = new int[0];
		int array5[] = new int[5];
		int array7[] = new int[7];
		int array16[] = new int[16];
		int array17[] = new int[17];
		int array19[] = new int[19];
		int array1000[] = new int[1000];

		// iX => integer with value X
		int i0 = 0;
		int i20 = 20;
		int i2 = 2;
		int i3 = 3;
		int i4 = 4;
		int i48 = 48;
		int i_5 = -5;

		int p = 10;
		array16[p] = 5; // Good

		p = i20 / i4; // p is [5,5]

		array5[p] = 5; // Illegal upper bound (legal index is 0-4)

		p = 48 / 3; // p is [16,16]

		array16[p] = 5; // Illegal upper bound (legal index range is 0-15)

		p /= p; // p is [1,1]

		array16[p] = 5; // Good

		p = i48 / i3; // p is [16,16]

		array16[p] = 5; // Illegal upper bound (legal index range is 0-15)

		array17[p] = 5; // Good

		boolean b = args.length == 0;

		p = 6; // p is [6,6]
		if (b) {
			p = p / 4; // p is [4,4]
		}

		// now p is [4,6]

		array5[p] = 5; // Potentially illegal upper bound (legal range is 0-4)

		array7[p] = 5; // Good

		for (int i = 0; i < 5; i++) {
			p /= 2;
		}

		// now p is [0,0]

		array0[p] = 5; // Illegal upper bound

		p = 2; // p is [2,2]
		for (int i = 0; i < 5; i++) {
			p *= 2;
		}

	}
}

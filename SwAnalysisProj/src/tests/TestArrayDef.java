package tests;

@SuppressWarnings("unused")
public class TestArrayDef {

	public static void main(String[] args) {

		int array5[] = new int[5]; // array5 => array of size 5

		int array2[] = new int[-10];

		int i = 0;
		int t = 20;
		int g = 2;
		int x = -5;

		int p = x * g; // p is [-10,-10]

		array5[p] = 5; // Illegal lower bound

		array5[10] = 5; // Illegal upper bound

		array5[1] = 4; // Good

		array5[t] = 6; // Illegal upper bound

		array5[g] = 3; // Good

		array5[-2] = 7; // Illegal lower bound

		array5[g] = 2; // Good

		array5[x] = 8; // Illegal lower bound

		x = x + 7; // x = 2

		array5[x] = 1; // Good

		boolean b = args.length == 0;
		if (b) {
			g = 7;
			i = 1;
		} else {
			g = 3;
			i = -1;
		}

		/* now g is in [3,7] , i is in [-1,1] */

		array5[g] = 9; // Potential illegal upper bound
		array5[i] = 10; // Potential illegal lower bound

	}
}

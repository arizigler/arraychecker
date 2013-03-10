package tests;

public class TestArrayDef {

	public static void main(String[] args) {

		int array[] = new int[5];

		int i = 0;
		int g = 2;
		int x = -5;

		array[10] = 5; // illegal upper bound

		array[1] = 4; // good

		array[i] = 6; // illegal upper bound

		array[g] = 3; // good

		array[-2] = 7; // illegal lower bound

		g++;

		array[g] = 2; // good

		array[x] = 8; // illegal lower bound

		x = x + 7; // x = 2

		array[x] = 1; // good

		boolean b = args.length == 0;
		if (b) {
			g = 7;
			i = 1;
		} else {
			g = 3;
			i = -1;
		}

		/* now g is in [3,7] , i is in [-1,1] */

		array[g] = 9; // potential upper bound
		array[i] = 10; // potential lower bound

	}
}

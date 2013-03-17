package tests;

public class PresentationTest2 {

	public static void main(String[] args) {

		// arrayX => array of size X
		int array0[] = new int[0];
		int array2[] = new int[2];
		int array3[] = new int[3];
		int array5[] = new int[5];
		int array7[] = new int[7];
		int array14[] = new int[14];
		int array15[] = new int[15];
		int array16[] = new int[16];
		int array18[] = new int[18];
		int array20[] = new int[20];

		boolean b0 = args.length == 0;
		boolean b1 = args.length == 1;

		/* Loops and conditions */

		for (int i = 0; i < 0; i++)
			array0[i] = 5; // OK (condition doesn't hold --> i == BOTTOM)

		for (int i = 0; i < 7; i++) {
			array0[i] = 5; // Illegal upper bound
			array5[i] = 5; // Potentially illegal upper bound (i is [0,6])
			array7[i] = 5; // OK
		}

		int a = 1; // a is [1,1]
		int b = -3; // b is [-3,-3]

		array5[b] = 5; // Illegal lower bound

		if (b < a) b = b + 4; // Condition holds

		// now b is [1,1]
		array5[b] = 5; // OK

		if (b < a) b = b + 20; // Condition does not hold

		// b still [1,1]
		array5[b] = 5; // OK

		while (a < 15)
			a++;

		// a is [15,15]
		array16[a] = 5; // OK

		a = a - 2; // a is [13,13]
		array16[a] = 5; // OK

		a = a + 2; // a is [15,15]

		if (b0) a = a + 5;

		// now a is [15,20]
		array16[a] = 5; // Potentially illegal upper bound

		int c = -8; // c is [-8,-8]

		array16[c] = 5; // Illegal lower bound

		// a is [15,20], b is [1,1], c is [-8,-8]
		if (!((b < a) || (c < 0))) c = c - 8; // Condition does not hold
		else c = c + 8;

		// now c is [0,0]
		array16[c] = 5; // OK

		c = 1; // c is [1,1]

		array5[c] = 5; // OK
		array16[c] = 5; // OK

		for (int i = 0; i < 15; i++) {
			c = i;
		}

		// now c is [0,14]
		array5[c] = 5; // Potentially illegal upper bound
		array14[c] = 5; // Potentially illegal upper bound (legal range is 0-13)
		array15[c] = 5; // OK

		a = 12; // a is [12,12]
		b = 2; // b is [2,2]
		c = a; // c is [12,12]

		array5[a] = 5; // Illegal upper bound
		array16[a] = 5; // OK
		array5[c] = 5; // Illegal upper bound
		array16[c] = 5; // OK

		while (c > 1) {
			a = a / b;
			c = a;
		}

		array5[c] = 5; // OK
		array16[c] = 5; // OK

		a = 1; // a is [1,1]

		array5[a] = 5; // OK

		while (a < 0) { // Condition does not hold
			a = -1;
			array0[a] = 5; // OK (a is BOTTOM)
		}

		// a is [1,1]
		a = a - 1;
		// a is [0,0]
		array5[a] = 5; // OK

		int i, j = 0; // i, j are [0,0]

		for (i = 0; i < 10; i++) {
			for (j = 0; j < 10; j++) {
				array18[i + j] = 5; // Potentially illegal upper bound (max i+j
									// is 18)
				array20[i + j] = 5; // OK
			}
		}

		/* Boolean operators */

		boolean b2 = args.length <= 2;

		/* operator <= */

		int d = -3;

		if (b2) d = 8;

		// d is [-3,8]

		int e = 2;
		if (b2) e = 4;

		// e is [2,4]

		int f = 0; // f is [0,0]

		array5[f] = 5; // OK
		array7[f] = 5; // OK

		if (d <= e) f = d;

		// f is [-3,4] ( and not [-3,8] )
		array3[f] = 5; // Potentially illegal lower and upper bounds
		array7[f] = 5; // Potentially illegal lower bound

		/* operator >= */

		f = 0; // f is [0,0]

		array5[f] = 5; // OK
		array7[f] = 5; // OK

		// Reminder: d is [-3,8] , e is [2,4]
		if (e >= d) f = d; // Condition holds if d is [-3,4]

		// f is [-3,4] ( and not [-3,8] )
		array3[f] = 5; // Potentially illegal lower and upper bounds
		array7[f] = 5; // Potentially illegal lower bound

		/* operator == */

		d = -3;

		if (b2) d = 2;

		// Reminder: d is [-3,2] , e is [2,4]
		array2[d] = 5; // Potentially illegal lower and upper bounds
		array3[d] = 5; // Potentially illegal lower bound
		array3[e] = 5; // Potentially illegal upper bound

		if (d == e) { // Possible only if d == e == [2,2]

			array2[d] = 5; // Illegal upper bound (legal range is 0-1)
			array3[d] = 5; // OK
			array3[e] = 5; // OK

		} else {
			array3[d] = 5; // Potentially illegal lower bound
			array3[e] = 5; // Potentially illegal upper bound
		}

		/* operator != */

		if (d != e) {

			array2[d] = 5; // Potentially illegal lower and upper bounds
			array3[d] = 5; // Potentially illegal lower bound
			array3[e] = 5; // Potentially illegal upper bound

		} else { // Possible only if d == e == [2,2]

			array2[d] = 5; // Illegal upper bound (legal range is 0-1)
			array3[d] = 5; // OK
			array3[e] = 5; // OK
		}

		f = 2; // f is [2,2]

		array5[f] = 5; // OK
		array7[f] = 5; // OK

		// Reminder: d is [-3,2] ,e is [2,4]
		if (!(d != e)) f = d; // Possible only if d == e == [2,2]

		// f is [2,2] ( and not [-3,2] )
		array2[f] = 5; // Illegal upper bound (legal range is 0-1)
		array5[f] = 5; // OK
		array7[f] = 5; // OK

		/* operator < */

		// e is [2,4]
		if (e < 3)
		// can be only if e == [2,2]
		array3[e] = 5; // OK
		else array3[e] = 5; // Illegal upper bound

		/* operator > */

		// e is [2,4]
		if (3 > e)
		// can be only if e == [2,2]
		array3[e] = 5; // OK
		else array3[e] = 5; // Illegal upper bound

		/* Widening */

		a = 0; // a is [0,0]
		while (b1)
			a++;

		// a is [0,INF]
		array5[a] = 5; // Potentially illegal upper bound
		array20[a] = 5; // Potentially illegal upper bound

		while (b1)
			a--;

		// a is [-INF,INF]
		array5[a] = 5; // Potentially illegal lower and upper bounds
		array20[a] = 5; // Potentially illegal lower and upper bounds
	}
}

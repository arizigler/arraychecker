package tests;

@SuppressWarnings("unused")
public class TestObjFields {

	int	f_unknown, f0, f1, f2, f20, f_5, f;

	public TestObjFields(TestObjFields tof) {

		// arrayX => array of size X
		int array0[] = new int[0];
		int array3[] = new int[3];
		int array5[] = new int[5];
		int array11[] = new int[11];
		int array17[] = new int[17];
		int array19[] = new int[19];
		int array200[] = new int[200];
		int array1000[] = new int[1000];

		// fX => integer field with value X
		tof.f0 = 0;
		tof.f20 = 20;
		tof.f1 = 1;
		tof.f2 = 2;
		tof.f_5 = -5;
		tof.f = 4;
		int p_ref = tof.f2 * tof.f_5; // p_ref is [-10,-10]

		f0 = 0;
		f20 = 20;
		f1 = 1;
		f2 = 2;
		f_5 = -5;
		f = 4;
		int p_this = f1 + f20; // p_this is [21,21]

		int p = tof.f20 - f2 * f2 + tof.f_5; // p is [11,11]

		array3[f_unknown] = 5; // Potentially illegal lower and upper bounds
								// (field uninitialized)
		array3[tof.f_unknown] = 5; // Potentially illegal lower and upper bounds

		array0[f0] = 5; // Illegal upper bound

		array0[tof.f1] = 5; // Illegal upper bound

		array5[p_ref] = 5; // Illegal lower bound

		array5[p_this] = 5; // Illegal upper bound

		array17[p] = 5; // Good

		array11[p] = 5; // Illegal upper bound (legal index range is 0-10)

		array5[f] = 5; // Good

		array5[tof.f] = 5; // Good

		array3[f0] = 5; // Good

		array3[tof.f0] = f1 + 4; // Good

		array3[f2] = tof.f1 * 8; // Good

		array3[tof.f2] = 7 - (tof.f1 + f2); // Good

		int a = foo(7);

		array3[f2] = 5; // Potentially illegal lower and upper bounds

		array3[tof.f2] = 5; // Potentially illegal lower and upper bounds

		array5[f] = 5; // Potentially illegal lower and upper bounds

		array5[tof.f] = 5; // Potentially illegal lower and upper bounds

		tof.f = 4;

		array5[tof.f] = 5; // Good

		array5[f] = 5; // Potentially illegal lower and upper bounds

		a = 10 - foo(7);

		array5[tof.f] = 5; // Potentially illegal lower and upper bounds

		f = 4;

		foo(7);

		array5[tof.f] = 5; // Potentially illegal lower and upper bounds

		array5[f] = 5; // Potentially illegal lower and upper bounds

		boolean b = tof == null;

		if (b) {
			p = p / 11; // p is [1,1]
			array5[p] = 5; // Good
			tof.f = 1; // tof.f is [1,1]
			array5[tof.f] = 5; // Good
		}

		// now p is [1,11]
		// now tof.f is [-INF,INF]

		array5[p] = 5; // Potentially illegal upper bound
		array17[p] = 5; // Good

		array5[tof.f] = 5; // Potentially illegal lower and upper bounds
		array17[tof.f] = 5; // Potentially illegal lower and upper bounds

		tof.f = 1; // tof.f is [1,1]

		if (b) {
			array5[tof.f] = 5; // Good
			foo(7);
			array5[tof.f] = 5; // Potentially illegal lower and upper bounds
		}

		array5[tof.f] = 5; // Potentially illegal lower and upper bounds

		f = 2;
		tof.f = 4 - 2; // tof.f is [2,2]
		array17[tof.f] = 5; // Good

		for (int i = 0; i < 5; i++) {
			tof.f *= 2;
		}

		// now f is [2,INF]

		array17[tof.f] = 5; // Potentially illegal upper bound

		f = foo(3);

		tof.f = tof.f - f; // tof.f is [-INF,INF]

		array1000[tof.f] = 5; // Potentially illegal lower and upper bounds

		tof.f = 2; // tof.f is [2,2]

		for (int i = 0; i < 5; i++) {
			tof.f *= -2;
		}

		// tof.f is [-INF,INF]

		array1000[tof.f] = 5; // Potentially illegal lower and upper bounds

	}

	private int foo(int x) {
		return x + 1;
	}
}

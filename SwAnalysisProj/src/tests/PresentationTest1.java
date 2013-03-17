package tests;

@SuppressWarnings("unused")
public class PresentationTest1 {

	private int	iField5;
	private int	iField15;

	public void method(String[] args) {

		// arrayX => array of size X
		int array0[] = new int[0];
		int array1[] = new int[1];
		int array5[] = new int[5];
		int array10[] = new int[10];

		final int i_10 = -10; // i_5 is [-10,-10]
		final int i_5 = -5; // i_5 is [-5,-5]
		final int i0 = 0; // i0 is [0,0]
		final int i1 = 1; // i1 is [1,1]
		final int i2 = 2; // i2 is [2,2]
		final int i3 = 3; // i3 is [3,3]
		final int i4 = 4; // i4 is [4,4]
		final int i5 = 5; // i5 is [5,5]
		final int i10 = 10; // i10 is [10,10]
		final int i20 = 20; // i20 is [20,20]
		final int i50 = 50; // i50 is [50,50]
		final int i100 = 100; // i100 is [100,100]
		final int i500 = 500; // i500 is [500,500]

		/* Trivial cases */

		array10[5] = 5; // OK

		array10[-5] = 5; // Illegal lower bound

		array10[20] = 5; // Illegal upper bound

		array10[i5] = 5; // Good

		array10[i_5] = 5; // Illegal lower bound

		array10[i20] = 5; // Illegal upper bound

		/* Interval Arithmetic */

		int p = 1; // p is [1,1]

		array5[p] = 5; // OK ( p is [1,1] )

		p = i5 + i10; // p is [15,15]

		array5[p] = 5; // Illegal upper bound

		p = i_10 + i5; // p is [-5,-5]

		array5[p] = 5; // Illegal lower bound

		p = i10 - i5; // p is [5,5]

		array10[p] = 5; // OK

		array5[p] = 5; // Illegal upper bound ( legal index range is [0,4] )

		p *= i10; // p is [10,10]

		array5[p] = 5; // Illegal upper bound

		array5[i3 * i1] = 5; // OK ( index is [3,3] )

		array5[i3 * i2] = 5; // Illegal upper bound ( index is [6,6] )

		p = i10 / i2; // p is [5,5]

		array5[p] = 5; // Illegal upper bound ( legal index range is [0,4] )

		p = 0 / i5; // p is [0,0]

		array1[0] = 5; // OK

		array0[p] = 5; // Illegal upper bound ( no legal index range )

		p = -i5; // p is [-5,-5]

		array10[p] = 5; // Illegal lower bound

		p = -p; // p is [5,5]

		array10[p] = 5; // OK

		/* Changing array size */
		int arrayA[] = new int[i50];
		int arrayB[] = new int[i100];

		arrayA[i100] = 5; // Illegal upper bound

		arrayA = new int[1000];

		arrayA[i500] = 5; // OK

		arrayA = arrayB; // arrayA new range is 0-99

		arrayA[i20] = 5; // OK

		arrayA[i500] = 5; // Illegal upper bound ( legal range is 0-99)

		arrayA = foo(); // arrayA new range is [0,INF]

		arrayA[i20] = 5; // Potentially illegal upper bound

		/* Tracking class fields */

		this.iField5 = 5; // iField5 is [5,5]

		this.iField15 = 15; // iField15 is [15,15]

		array10[iField5] = 5; // OK

		array10[iField15] = 5; // Illegal upper bound

		bar(); // Now all class fields are [-INF,INF]

		array10[iField5] = 5; // Potentially illegal lower and upper bounds

	}

	private int[] foo() {

		return new int[10];
	}

	private int bar() {

		return 10;
	}
}

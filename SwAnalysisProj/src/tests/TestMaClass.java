package tests;

import java.awt.List;
import java.util.ArrayList;
import java.util.Date;

public class TestMaClass {

		public static void main(String[] args) {

			int array5[] = new int[5];
//			int array16[] = new int[16];
//			int array19[] = new int[19];
//			int array20[] = new int[20];
//			int a = 12; // a is [12,12]
//			int b = 2; // b is [2,2]
//			int c = a; // c is [12,12]
//			
//			array5[a] = 5; // illegal upper bound
//			array16[a] = 5; // Good
//			array5[c] = 5; // illegal upper bound
//			array16[c] = 5; // Good		
//
//			while (c > 1) {
//				a = a / b;
//				c = a;
//			}
//			
//			array5[c] = 5; // Good
//			array16[c] = 5; // Good
//			
//			
//			int i , j = 0; // i, j are [0,0]
//			for (i=0; i<10; i++)
//				for (j=0; i<10; j++) {
//					array19[i+j] = 5; // Good
//					array20[i+j] = 5; // illegal upper bound
//				}
			
			int a = 1;
			
			array5[a] = 5; // Good
			
			while (a < 0) {
				a = -1;
				//array5[a] = 5; // Good (we won't get here)
			}
			
			array5[a] = 5; // Good
	}
	
}


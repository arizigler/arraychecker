package tests;

import java.awt.List;
import java.util.ArrayList;
import java.util.Date;

public class TestMaClass {

		public static void main(String[] args) {
			
			int array5[] = new int[5];
			int array7[] = new int[7];			
			
			boolean b2 = args.length > 2;
			
			int d = -3; 
			if (b2)
				d = 8;
			
			// d is [-3,8]
			
			int e = 2;
			if (b2)
				e = 4;
			
			// e is [2,4]
			
			int f = 0; // f is [0,0]
			
			array5[f] = 5; // Good
			array7[f] = 5; // Good
			
			if (d <= e)
				f = d;
				
			// f is [-3,4] and not [-3,8] 
			array5[f] = 5; // Potentially illegal lower bound
			array7[f] = 5; // Potentially illegal lower bound			


	}
	
}


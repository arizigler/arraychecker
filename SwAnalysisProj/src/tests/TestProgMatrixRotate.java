package tests;
@SuppressWarnings("unused")

public class TestProgMatrixRotate {
	public static void rotate() {
		int a,b,c,d,e,f;
		int[] arr = new int[10];
		f = 0;
		while (f < 0){
			f++;
			f--;
		}
		// f 0,0
		arr[f] = 2;  // good
		
		a = 3; //a,3,3
		b= 2*a; //b,6,6
		c=b/3; //c, 2,2
		d = c + b; //d 8,8
		e = d - a; // e 5,5
		f = e + 4; //f 9,9
		
		
		
		if (a > b){
			arr[a] = b;
		}
		else
			b = -a;
		// b -3,3
		
		arr[b] = a; // potentially lower bound OF
		
		b = -b;
		
		arr[b] = a; // potentially lower bound OF
		
	
		
	
		
		b = -1; // -1,-1
		while (a > 0)
			b--;
		// b -INF,-1
		arr[b] = 4; //lower bound OF
		
		b = -b; // 1,INF
		
		arr[b] = 2; // potentially upper bound OF	
		
		arr[b*2*e] = 3; // 10,INF  upper bound OF 
		
		b = b-1;
		
		arr[b*2*e] = 3; // 9,INF  potentially upper bound OF
		
		if (a > 0)
			 e = 9;
		
		// e 5,9
		
		arr[e] = 0; // Good
		
		f = 2*e; //f 10,18
		if (a < 0)
			f = 12; //f 10,18
		arr[f]  = 2; // upper bound OF
		if (a < 0)
			f = 0;
		//f 0,18
		
		arr[f/2] = 3; //Good
		
		while (f < 0){
			f++;
			f--;
		}
		
		arr[f] = 2;  // 0,18 potentially upper bound OF
		
		while (f  < 0) {
			f++;
		}
		
		while (f < 0) {
			f--;
		}
		
		arr[f] = 2; //-INF,INF potentially upper and lower OF
		
		a = 0;
		
		while (a == 0)
			a++;
		
		arr[f/a] = 3;
	
		
		
		
		
		
		
	}
}

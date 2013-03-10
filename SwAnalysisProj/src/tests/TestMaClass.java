package tests;
public class TestMaClass {

	public static void main(String[] args) {
		// loop
//		
//		int a = -1;
//		int b = 0; 
		boolean bool = args.length == 0;
//		while (a<0) {
//			if (bool)
//				a=a+2;
//			else
//				a=a-2;
//		}
//		b = b/a;
		
		int a = 1;
		int b = foo(a);
		while (bool) { b=b*2;}
			
		int c = a + b;
		

			
		
		
	}
	
	public static int foo(int x) {return x+1;}
	
}

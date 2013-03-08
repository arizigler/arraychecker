package test;
public class TestMaClass {

	public static void main(String[] args) {
		int a = 1 + 2;
		int b = 2;
		b += 1;
		b++;
		if (a > 3){
			b = a + 7;
			a = 19 + a;
		}
		else
			b = a + b;
		int c = b + a;
		int d = -b;
		/* check SubExpr */
		boolean bool = false;
		int a1,b1;
		if (bool){ 
			a1 = -3;
			b1 = -7;
		}
		else{
			a1 = 2;
			b1 = 8;
		}
		int c1 = a1 - b1;
		c1 = c1 - b1;
	}
}

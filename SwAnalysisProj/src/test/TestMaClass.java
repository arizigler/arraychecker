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
	}
}

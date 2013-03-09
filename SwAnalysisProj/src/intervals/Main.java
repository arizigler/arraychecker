package intervals;

public class Main {

	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		Main f = new Main();
		int[] array = new int[20];
		array[2] = 3;
		
		int a = 7;
		array[a] = 2;
		int b = 14;
		while (b > a)
			array[b-a+1] = b--;
		int x = (f.bar(21) + a) * b;
		}
		public int bar(int n) { return n + 42; }

}

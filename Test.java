public class Test implements java.io.Serializable {

	@Deprecated
	final private int value = -2;

	public static int sum(int a, int b) {
		int result = a + b;
		return result;
	}

	public static void main(String[] args) {
		int a = 1;
		int result = sum(1, 2);
		int c = result * 6;
	}

}
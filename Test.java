public class Test implements java.io.Serializable {

	@Deprecated
	final private int value = -2;

	public static int sum(int a, float b) {
		int result = (int) (a + b);
		return result;
	}

	public static void main(String[] args) {
		int a = 1;
		float b = 2;
		int result = sum(1, 2);
	}

}
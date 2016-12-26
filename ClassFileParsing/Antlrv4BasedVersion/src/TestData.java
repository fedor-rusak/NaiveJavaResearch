@interface Copyright {
	String value();
	String[] data() default {"that is awkward", "asd"};
}

@Copyright(value="Santa Claus", data={"From", "North Pole"})
public class TestData implements java.io.Serializable {

	@Deprecated
	final private long value = -2;

	public static void parsingTrick(Object[][] data) {
		return;
	}

	@Deprecated
	public static void main(String[] args) {
		for (int i = 0; i < 1; i++)
			System.out.println("Hello World!");
	}

}
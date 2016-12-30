import java.util.*;
import java.lang.invoke.*;
import static java.lang.invoke.MethodType.*;
import static java.lang.invoke.MethodHandles.*;

public class TestData {

	public static Integer adder(Integer x, Integer y) {
		return x + y;
	}

	public static Integer funnyAdder(Integer x, Integer y) {
		return -1*(x + y);
	}

	public static CallSite mybsm(MethodHandles.Lookup callerClass, String dynMethodName, MethodType dynMethodType) throws Throwable {
		MethodHandle mh =
			callerClass.findStatic(
				TestData.class,
				"funnyAdder",
				MethodType.methodType(Integer.class, Integer.class, Integer.class)
			);

		if (!dynMethodType.equals(mh.type())) {
			mh = mh.asType(dynMethodType);
		}

		return new ConstantCallSite(mh);
	}

	public static void main(String[] args) {
		System.out.println("Adder tests!!!");
		adderTest();
		funnyAdderTest();
	}

	public static void adderTest() {
		System.out.println(adder(40,2));
	}

	public static void funnyAdderTest() {
		System.out.println(adder(40,2));
	}

}
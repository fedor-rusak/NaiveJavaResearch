public class NaiveInterpreter {

	static String pushTwoIntsInLocalVariables =
		"10" + "16"		// push byte value as integer to operand stack
		+ "3b"			// pop int value from stack into variable 0
		+ "10" + "16"	// push byte value as integer to operand stack
		+ "3c";			// pop int value from stack into variable 1

	static String sumTwoIntsMethod =
		"1a"			// push int value from variable 0 to operand stack
		+ "1b"			// push int value from variable 1 to operand stack
		+ "60"			// pop and sum two int values from stack and push result to operand stack
		+ "3d"			// pop int value from stack into variable 2
		+ "1c"			// push int value from variable 2 to operand stack
		+ "ac";			// signal that execution completed successfully and int value is on top of operand stack of current frame

	static int hexToInt(String hex) {
		return Integer.parseInt(hex, 16);
	}

	static String getByteHex(String input, int pointer) {
		return input.substring(pointer*2, pointer*2+2);
	}

	static String hexToString(String hex) {
		int length = hex.length();

		byte[] data = new byte[length / 2];
		for (int i = 0; i < length; i += 2) {
			data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
		                         + Character.digit(hex.charAt(i+1), 16));
		}

		return new String(data);
	}

	static void println(Object object) {
		System.out.println(object);
	}

	public static void main(String[] args) {
		int pointer = 0;

		String program = pushTwoIntsInLocalVariables + sumTwoIntsMethod;

		while (pointer < program.length()/2) {

			String opcodeString = getByteHex(program, pointer);

			System.out.print(pointer + ":  " + opcodeString + " ");

			switch(hexToInt(opcodeString)) {
				case 0x10:
					// bipush
					println("push byte value " + hexToInt(getByteHex(program, pointer+1)) + " on operand stack as integer");
					pointer++;
					break;
				case 0x1a:
					// iload_0
					println("push int value from variable 0 to operand stack");
					break;
				case 0x1b:
					// iload_1
					println("push int value from variable 1 to operand stack");
					break;
				case 0x1c:
					// iload_2
					println("push int value from variable 2 to operand stack");
					break;
				case 0x3b:
					// istore_0
					println("pop int value from stack into variable 0");
					break;
				case 0x3c:
					// istore_1
					println("pop int value from stack into variable 1");
					break;
				case 0x3d:
					// istore_2
					println("pop int value from stack into variable 2");
					break;
				case 0x60:
					// iadd
					println("pop and sum two int values from stack and push result to operand stack");
					break;
				case 0xac:
					// ireturn
					println("signal that execution completed successfully and int value is on top of operand stack of current frame");
					break;
				default:
					println("WTF?");
					break;
			}

			pointer++;
		}
	}

}
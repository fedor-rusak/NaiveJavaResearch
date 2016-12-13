public class NaiveInterpreter {

	static String bytecodeCommands = 
		"10" + "16"		// push byte value and as integer
		+ "3c"			// store int value into int variable 1
		+ "1b"			// push on stack value from int variable 1
		+ "10" + "15"	// push byte value as integer 
		+ "60"			// add two ints from stack and push value into stack
		+ "3d";			// store int value into int variable 2

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

		while (pointer < bytecodeCommands.length()/2) {
			String opcodeString = getByteHex(bytecodeCommands, pointer);

			switch(hexToInt(opcodeString)) {
				case 0x10:
					println("push value: " + hexToInt(getByteHex(bytecodeCommands, pointer+1)) + " to frame");
					pointer++;
					break;
				case 0x1b:
					println("load value from int variable 1 into stack");
					pointer++;
					break;
				case 0x3c:
					println("get value from stack and save it to int variable 1");
					pointer++;
					break;
				case 0x3c:
					println("get value from stack and save it to int variable 2");
					pointer++;
					break;
				case 0x60:
					println("sum two int values from stack and push it back to stack");
					pointer++;
					break;
			}

			pointer++;
		}
	}

}
public class Helper {

	public static final int UTF8 = 1;
	public static final int INTEGER = 3;
	public static final int FLOAT = 4;
	public static final int LONG = 5;
	public static final int DOUBLE = 6;
	public static final int CLASS = 7;
	public static final int STRING = 8;
	public static final int FIELD_REF = 9;
	public static final int METHOD_REF = 10;
	public static final int INTERFACE_METHOD_REF = 11;
	public static final int NAME_AND_TYPE = 12;
	public static final int METHOD_HANDLE = 15;
	public static final int METHOD_TYPE = 16;
	public static final int INVOKE_DYNAMIC = 18;


	public static String intToHex(int value) {
		return String.format("%08X", value);
	}

	public static String intToShortHex(int value) {
		return String.format("%04X", value);
	}

	public static String intToByteHex(int value) {
		return String.format("%02X", value);
	}

	public static String stringToHex(String input) {
		String result = "";

		for (int i = 0; i < input.length(); i++)
			result += intToByteHex((int) input.charAt(i));

		return result;
	}


	public static String toConstant(int constantType, String data) {
		if (1 == constantType)
			return intToByteHex(constantType) +intToShortHex(data.length()) + stringToHex(data);

		return intToByteHex(constantType) + data;
	}

	public static String toConstant(int constantType, int twoBytes) {
		return toConstant(constantType, intToShortHex(twoBytes));
	}

	public static String toConstant(int constantType, int twoBytes, int twoBytesMore) {
		return toConstant(constantType, twoBytes) + intToShortHex(twoBytesMore);
	}

	public static String getConstants(Object[][] input) {
		//this will work only before introducing double or long constants!!!
		String result = intToShortHex(input.length+1);	//number of constants +1 because indexing start from 1

		for(int i = 0; i < input.length; i++) {
			Object[] args = input[i];
			int constantType = (int) args[0];

			if (args.length == 2 && UTF8 == constantType)
				result += toConstant(constantType, (String) args[1]);
			else if (args.length == 2)
				result += toConstant(constantType, (int) args[1]);
			else if (args.length == 3)
				result += toConstant(constantType, (int) args[1], (int) args[2]);
		}

		return result;
	}


	public static int findConstantIndex(Object[][] input, String searchValue) {
		int result = -1;

		for(int i = 0; i < input.length; i++) {
			int constantType = (int) input[i][0];

			if (UTF8 == constantType && searchValue.equals(input[i][1])) {
				result = i+1; //because constants are indexed from 1
				break;
			}
		}

		return result;
	}

	public static String getMethods(Object[][] input, Object[][] constantsInput) {
		String result = intToShortHex(input.length);

		for(int i = 0; i < input.length; i++) {
			Object[] method = input[i];

			String accessFlags = (String) method[0];
			int nameIndex = findConstantIndex(constantsInput, (String) method[1]);
			int descriptorIndex = findConstantIndex(constantsInput, (String) method[2]);
			Object[][] attributes =(Object[][]) method[3];
			int attributeCount = attributes.length;

			result += accessFlags + intToShortHex(nameIndex);
			result += intToShortHex(descriptorIndex) + intToShortHex(attributeCount);

			for (int j = 0; j < attributes.length; j++) {
				Object[] attribute = attributes[j];
				String constantUtf8 = (String) attribute[0];
				int constantIndex = findConstantIndex(constantsInput, constantUtf8);

				String tempResult = "";

				if ("Code".equals(constantUtf8)) {
					int maxStack = (int) attribute[1];
					int maxLocals = (int) attribute[2];
					String code = (String) attribute[3];
					int codeLength = code.length()/2;//get numberof bytes
					Object[][] exceptionTable = (Object[][]) attribute[4];
					Object[][] innerAttributes = (Object[][]) attribute[5];


					tempResult = intToShortHex(maxStack);
					tempResult += intToShortHex(maxLocals);
					tempResult += intToHex(codeLength);
					tempResult += code;
					tempResult += intToShortHex(exceptionTable.length);

					for (int k = 0; k < exceptionTable.length; k++)
						tempResult += "NOT_IMPLEMENTED";

					tempResult += intToShortHex(innerAttributes.length);

					for (int k = 0; k < innerAttributes.length; k++) {
						Object[] innerAttribute = innerAttributes[k];
						String attributeName = (String) innerAttribute[0];

						if ("LineNumberTable".equals(attributeName)) {
							int attributeNameIndex = findConstantIndex(constantsInput, attributeName);
							Object[][] lineNumberTable = (Object[][]) innerAttribute[1];
							int lineNumberCount = lineNumberTable.length;

							tempResult += intToShortHex(attributeNameIndex);
							tempResult += intToHex(2 + lineNumberCount*4); //byte length of data
							tempResult += intToShortHex(lineNumberCount);

							for (int l = 0; l < lineNumberTable.length; l++) {
								tempResult += intToShortHex((int) lineNumberTable[l][0]);
								tempResult += intToShortHex((int) lineNumberTable[l][1]);
							}
						}
					}
				}

				result += intToShortHex(constantIndex);
				result += intToHex(tempResult.length()/2);//length in bytes
				result += tempResult;
			}
		}

		return result;
	}


	public static String getAttributes(Object[][] input, Object[][] constantsInput) {
		String result = intToShortHex(input.length);

		for(int i = 0; i < input.length; i++) {
			Object[] attribute = input[i];
			String attributeName = (String) attribute[0];
			int attributeNameIndex = findConstantIndex(constantsInput, attributeName);

			if ("SourceFile".equals(attributeName)){
				String sourceFileName = (String) attribute[1];
				int sourceFileNameIndex = findConstantIndex(constantsInput, sourceFileName);

				result += intToShortHex(attributeNameIndex);
				result += intToHex(2);	//constant
				result += intToShortHex(sourceFileNameIndex);
			}
			if ("BootstrapMethods".equals(attributeName)){
				result += intToShortHex(attributeNameIndex);
				result += intToHex(2);	//constant
				result += intToShortHex(0);
			}
		}

		return result;
	}


	static {
		System.out.println("Simple tests!");
		System.out.println("1 as byte hex: " + "01".equals(intToByteHex(1)));
		System.out.println("1 as short hex: " + "0001".equals(intToShortHex(1)));
		System.out.println("<init> as hex: " + "3C696E69743E".equals(stringToHex("<init>")));
		System.out.println("OK!");
	}


	private static String magicStartConstant = "cafebabe";

	private static String minorMajorJavaCompilerVersion = intToShortHex(0) + intToShortHex(51);

	private static Object[][] constantsData = {
		{METHOD_REF, 6, 15},
		{FIELD_REF, 16, 17},
		{STRING, 18},
		{METHOD_REF, 19, 20},
		{CLASS, 21},
		{CLASS, 22},
		{UTF8, "<init>"},
		{UTF8, "()V"},
		{UTF8, "Code"},
		{UTF8, "LineNumberTable"},
		{UTF8, "main"},
		{UTF8, "([Ljava/lang/String;)V"},
		{UTF8, "SourceFile"},
		{UTF8, "Test.java"},
		{NAME_AND_TYPE, 7, 8},
		{CLASS, 23},
		{NAME_AND_TYPE, 24, 25},
		{UTF8, "Hello world haha!"},
		{CLASS, 26},
		{NAME_AND_TYPE, 27, 28},
		{UTF8, "Test"},
		{UTF8, "java/lang/Object"},
		{UTF8, "java/lang/System"},
		{UTF8, "out"},
		{UTF8, "Ljava/io/PrintStream;"},
		{UTF8, "java/io/PrintStream"},
		{UTF8, "println"},
		{UTF8, "(Ljava/lang/String;)V"},
		{UTF8, "somethingWithInvokeDynamic"},
		{METHOD_REF, 5, 31},
		{NAME_AND_TYPE, 29, 8},
		{UTF8, "BootstrapMethods"},
	};

	private static String accessFlags = "0021";

	private static String thisClass = intToShortHex(5);

	private static String superClass = intToShortHex(6);

	private static String interfaces = intToShortHex(0);

	private static String fields = intToShortHex(0);

	private static Object[][] methodsData = {
		{
			"0001",//accessflags
			"<init>",
			"()V",
			new Object[][]{
				{
					"Code", 1, 1, "2ab70001b1",
					new Object[][] {},//exceptions
					new Object[][] {
						{"LineNumberTable", new Object[][]{{0,1}}}
					}
				}
			}
		},
		{
			"0009",//accessflags
			"main",
			"([Ljava/lang/String;)V",
			new Object[][]{
				{
					"Code", 2, 1, "b200021203b60004" + "b8001e" +"b1",
					new Object[][] {},//exceptions
					new Object[][] {
						{"LineNumberTable", new Object[][]{{0,3},{8,4}}}
					}
				}
			}
		},
		{
			"0009",//accessflags
			"somethingWithInvokeDynamic",
			"()V",
			new Object[][]{
				{
					"Code", 2, 0, "b200021203b60004b1",
					new Object[][] {},//exceptions
					new Object[][] {} //attributes
				}
			}
		}

	};

	private static Object[][] attributesData = {
		{"SourceFile", "Test.java"},
		{"BootstrapMethods", new Object[][]{{}}}
	};


	private static String testClassFile =
		magicStartConstant	+
		minorMajorJavaCompilerVersion	+
		getConstants(constantsData)	+
		accessFlags	+
		thisClass	+
		superClass	+
		interfaces	+
		fields	+
		getMethods(methodsData, constantsData) +
		getAttributes(attributesData, constantsData);

	public static Object[][] testDataConstantsData = {
		{METHOD_REF, 23, 46},
		{METHOD_REF, 6, 47},
		{METHOD_REF, 6, 48},
		{CLASS, 49},
		{STRING, 50},
		{CLASS, 51},
		{CLASS, 52},
		{METHOD_REF, 53, 54},
		{METHOD_REF, 32, 55},
		{METHOD_REF, 56, 57},
		{METHOD_REF, 53, 58},
		{METHOD_REF, 56, 59},
		{CLASS, 60},
		{METHOD_REF, 13, 61},
		{FIELD_REF, 62, 63},
		{STRING, 64},
		{METHOD_REF, 65, 66},
		{METHOD_REF, 22, 67},
		{METHOD_REF, 22, 68},
		{METHOD_REF, 22, 69},
		{METHOD_REF, 65, 70},
		{CLASS, 71},
		{CLASS, 72},
		{UTF8, "<init>"},
		{UTF8, "()V"},
		{UTF8, "Code"},
		{UTF8, "LineNumberTable"},
		{UTF8, "adder"},
		{UTF8, "(Ljava/lang/Integer;Ljava/lang/Integer;)Ljava/lang/Integer;"},
		{UTF8, "funnyAdder"},
		{UTF8, "mybsm"},
		{CLASS, 74},
		{UTF8, "Lookup"},
		{UTF8, "InnerClasses"},
		{UTF8, "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"},
		{UTF8, "StackMapTable"},
		{CLASS, 75},
		{UTF8, "Exceptions"},
		{CLASS, 76},
		{UTF8, "main"},
		{UTF8, "([Ljava/lang/String;)V"},
		{UTF8, "adderTest"},
		{UTF8, "funnyAdderTest"},
		{UTF8, "SourceFile"},
		{UTF8, "TestData.java"},
		{NAME_AND_TYPE, 24, 25},
		{NAME_AND_TYPE, 77, 78},
		{NAME_AND_TYPE, 79, 80},
		{UTF8, "Test"},
		{UTF8, "TestData.funnyAdder"},
		{UTF8, "java/lang/Integer"},
		{UTF8, "java/lang/Class"},
		{CLASS, 81},
		{NAME_AND_TYPE, 82, 83},
		{NAME_AND_TYPE, 84, 85},
		{CLASS, 75},
		{NAME_AND_TYPE, 86, 87},
		{NAME_AND_TYPE, 88, 89},
		{NAME_AND_TYPE, 90, 91},
		{UTF8, "java/lang/invoke/ConstantCallSite"},
		{NAME_AND_TYPE, 24, 92},
		{CLASS, 93},
		{NAME_AND_TYPE, 94, 95},
		{UTF8, "Adder tests!!!"},
		{CLASS, 96},
		{NAME_AND_TYPE, 97, 98},
		{NAME_AND_TYPE, 42, 25},
		{NAME_AND_TYPE, 43, 25},
		{NAME_AND_TYPE, 28, 29},
		{NAME_AND_TYPE, 97, 99},
		{UTF8, "TestData"},
		{UTF8, "java/lang/Object"},
		{CLASS, 100},
		{UTF8, "java/lang/invoke/MethodHandles$Lookup"},
		{UTF8, "java/lang/invoke/MethodHandle"},
		{UTF8, "java/lang/Throwable"},
		{UTF8, "intValue"},
		{UTF8, "()I"},
		{UTF8, "valueOf"},
		{UTF8, "(I)Ljava/lang/Integer;"},
		{UTF8, "java/lang/invoke/MethodType"},
		{UTF8, "methodType"},
		{UTF8, "(Ljava/lang/Class;Ljava/lang/Class;[Ljava/lang/Class;)Ljava/lang/invoke/MethodType;"},
		{UTF8, "findStatic"},
		{UTF8, "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;"},
		{UTF8, "type"},
		{UTF8, "()Ljava/lang/invoke/MethodType;"},
		{UTF8, "equals"},
		{UTF8, "(Ljava/lang/Object;)Z"},
		{UTF8, "asType"},
		{UTF8, "(Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;"},
		{UTF8, "(Ljava/lang/invoke/MethodHandle;)V"},
		{UTF8, "java/lang/System"},
		{UTF8, "out"},
		{UTF8, "Ljava/io/PrintStream;"},
		{UTF8, "java/io/PrintStream"},
		{UTF8, "println"},
		{UTF8, "(Ljava/lang/String;)V"},
		{UTF8, "(Ljava/lang/Object;)V"},
		{UTF8, "java/lang/invoke/MethodHandles"}
	};

	private static String testDataClassFile =
		magicStartConstant	+
		minorMajorJavaCompilerVersion	+
		getConstants(testDataConstantsData)	+
		accessFlags	+
		intToShortHex(22)	+
		intToShortHex(23)	+
		interfaces	+
		fields	+
		"0007"	+
		"0001001800190001001A0000001D00010001000000052AB70001B100000001001B000000060001000000060009001C001D0001001A00000025000200020000000D2AB600022BB6000260B80003B000000001001B000000060001000000090009001E001D0001001A00000025000200020000000D2AB600022BB6000260B80003B000000001001B0000000600010000000D0009001F00230002001A0000006900090004000000372A130004120513000613000604BD0007590313000653B80008B600094E2C2DB6000AB6000B9A00092D2CB6000C4EBB000D592DB7000EB000000002001B00000012000400000011001D001800280019002E001C0024000000080001FC002E070025002600000004000100270009002800290001001A00000033000200010000000FB2000F1210B60011B80012B80013B100000001001B0000001200040000002000080021000B0022000E00230009002A00190001001A0000002F0003000000000013B2000F1028B8000305B80003B80014B60015B100000001001B0000000A000200000026001200270009002B00190001001A0000002F0003000000000013B2000F1028B8000305B80003B80014B60015B100000001001B0000000A00020000002A0012002B" +
		"0002"+
		"002C00000002002D00220000000A00010020004900210019";


	public static byte[] hexStringToByteArray(String input) {
		int len = input.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			byte value = (byte) (Character.digit(input.charAt(i), 16) << 4);
				value += (byte) Character.digit(input.charAt(i+1), 16);
			data[i / 2] = value;
		}
		return data;
	}


	public static void main(String[] args) throws Exception {
		// writeClassFileToFS(testClassFile, "Test.class");
		writeClassFileToFS(testDataClassFile, "TestData.class");
	}

	public static void writeClassFileToFS(String classFileData, String classFileName) throws Exception {
		java.io.FileOutputStream out = new java.io.FileOutputStream(classFileName);
		out.write(hexStringToByteArray(classFileData));
		out.close();
	}

}
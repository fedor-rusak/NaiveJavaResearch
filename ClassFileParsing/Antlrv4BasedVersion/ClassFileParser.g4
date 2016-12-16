parser grammar ClassFileParser;

tokens { BYTE }

@members {
	static void println(Object object) {
		System.out.println(object);
	}

	static int hexToInt(String hex) {
		return Integer.parseInt(hex, 16);
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
}


startPoint:
	{println("Class file start");}
	magic
	minorVersion
	majorVersion
	constantPoolStuff
	BYTE*
	{println("Class file end");};


magic:
	fourBytes
	{if ("CAFEBABE".equals($fourBytes.text) == false) throw new RuntimeException("Magic value is wrong!");}
	{println("Magic: " + $fourBytes.text);};

minorVersion: twoBytes {println("Minor version: " + hexToInt($twoBytes.text));};

majorVersion: twoBytes {println("Major version: " + hexToInt($twoBytes.text));};

constantPoolStuff
  locals[int i = 1, int poolCount]:
  constantPoolCount
  {$poolCount = hexToInt($constantPoolCount.text);} 
  (
  	{$i<$poolCount}? 
  		{println("  Constant index: " + $i);}
  		constantElement
  		{
  			$i++;
  			String tagByte = $constantElement.text.substring(0,2);
  		 	if (tagByte.equals("05") || tagByte.equals("06"))
  		 		// 8 byte constans are considered to use two indices... official specification
  		 		// https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.4.5
  		 		$i++;
  		 }
  )*;


constantPoolCount: twoBytes {println("Constant pool count: " + hexToInt($twoBytes.text));};

constantElement: BYTE constantContent[hexToInt($BYTE.text)];


constantContent[int tag]:
	{$tag == 1}? {println("    Utf8");} utf8Data
	| {$tag == 3}? {println("    Integer");} integerData
	| {$tag == 4}? {println("    Float");} floatData
	| {$tag == 5}? {println("    Long");} longData
	| {$tag == 6}? {println("    Double");} doubleData
	| {$tag == 7}? {println("    Class");} classData
	| {$tag == 8}? {println("    String");} stringData
 	| {$tag == 9}? {println("    FieldRef");} fieldRefData
	| {$tag == 10}? {println("    MethodRef");} methodRefData
	| {$tag == 11}? {println("    InterfaceMethodRef");} interfaceMethodRefData
	| {$tag == 12}? {println("    NameAndType");} nameAndTypeData
	| {$tag == 15}? {println("    MethodHandle");} methodHandleData
	| {$tag == 16}? {println("    MethodType");} methodTypeData
	| {$tag == 18}? {println("    InvokeDynamic");} invokeDynamicData;


utf8Data:
	twoBytesWithLog["Length"]
	utf8Value[hexToInt($twoBytesWithLog.text)] {println("    Value: " + hexToString($utf8Value.text));};

utf8Value[int length]: someBytes[$length];

integerData: fourBytes;

floatData: fourBytes;

longData: fourBytes fourBytes;

doubleData: fourBytes fourBytes;

classData: twoBytesWithLog["Class index"];

stringData: twoBytesWithLog["String index"];

fieldRefData: twoForTwo["Class", "Name and type index"];
methodRefData: twoForTwo["Class", "Name and type index"];
interfaceMethodRefData: twoForTwo["Class", "Name and type index"];

nameAndTypeData: twoForTwo["Name", "Descriptor"];

methodHandleData:
	BYTE {println("    Reference kind: " + hexToInt($BYTE.text));}
	twoBytesWithLog["Reference index"];

methodTypeData: twoBytesWithLog["Descriptor index"];

invokeDynamicData: twoForTwo["Bootstrap attribute method", "Name and type"];

twoForTwo[String first, String second]:
	twoBytesWithLog[$first+" index"]
	twoBytesWithLog[$second+" index"];

twoBytesWithLog[String logMessage]:
	twoBytes {println("    "+ $logMessage + ": " + hexToInt($twoBytes.text));};


twoBytes: BYTE BYTE;

fourBytes: BYTE BYTE BYTE BYTE;

someBytes[int length] locals[int i = 0]: ( {$i<$length}? BYTE {$i++;} )*;
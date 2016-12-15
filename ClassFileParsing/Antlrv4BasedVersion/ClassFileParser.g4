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
  ( {$i<$poolCount}? {println("  Constant index: " + $i);} constantElement {$i++;} )*;


constantPoolCount: twoBytes {println("Constant pool count: " + hexToInt($twoBytes.text));};

constantElement: BYTE constantContent[hexToInt($BYTE.text)];


constantContent[int tag]:
	{$tag == 1}? {println("    Type: Utf8");} utf8Data
	| {$tag == 3}? {println("    Type: Integer");} integerData
	| {$tag == 4}? {println("    Type: Float");} floatData
	| {$tag == 7}? {println("    Type: Class");} classData
	| {$tag == 8}? {println("    Type: String");} stringData
 	| {$tag == 9}? {println("    Type: FieldRef");} fieldRefData
	| {$tag == 10}? {println("    Type: MethodRef");} methodRefData
	| {$tag == 11}? {println("    Type: InterfaceMethodRef");} interfaceMethodRefData
	| {$tag == 12}? {println("    Type: NameAndType");} nameAndTypeData;


utf8Data:
	twoBytes {println("    Length: " + hexToInt($twoBytes.text));}
	utf8Value[hexToInt($twoBytes.text)] {println("    Value: " + hexToString($utf8Value.text));};

utf8Value[int length]: someBytes[$length];

integerData: fourBytes;

floatData: fourBytes;

classData: twoBytes {println("    Class index: " + hexToInt($twoBytes.text));};

stringData: twoBytes {println("    String index: " + hexToInt($twoBytes.text));};

fieldRefData: twoForTwo["Class", "Name and type index"];
methodRefData: twoForTwo["Class", "Name and type index"];
interfaceMethodRefData: twoForTwo["Class", "Name and type index"];

nameAndTypeData: twoForTwo["Name", "Descriptor"];

twoForTwo[String first, String second]:
	twoBytes {println("    "+$first+" index: " + hexToInt($twoBytes.text));}
	twoBytes {println("    "+$second+" index: " + hexToInt($twoBytes.text));};


twoBytes: BYTE BYTE;

fourBytes: BYTE BYTE BYTE BYTE;

someBytes[int length] locals[int i = 0]: ( {$i<$length}? BYTE {$i++;} )*;
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


startPoint
	locals[String[] utf8ConstantArray]:
	{println("Class file start");}
	magic
	minorVersion
	majorVersion
	constantPoolStorage {$utf8ConstantArray = $constantPoolStorage.utf8ConstantArray;}
	accessFlags
	thisClass
	superClass
	interfaceStuff
	fieldStorage[$utf8ConstantArray]
	BYTE*
	{println("Class file end");};


magic:
	fourBytes
	{if ("CAFEBABE".equals($fourBytes.text) == false) throw new RuntimeException("Magic value is wrong!");}
	{println("Magic: " + $fourBytes.text);};

minorVersion: twoBytes {println("Minor version: " + hexToInt($twoBytes.text));};

majorVersion: twoBytes {println("Major version: " + hexToInt($twoBytes.text));};

constantPoolStorage
	returns [String[] utf8ConstantArray]
	locals[int i = 1, int poolCount]:
	twoBytesWithLog["Constant pool count"]
	{$poolCount = hexToInt($twoBytesWithLog.text); $utf8ConstantArray = new String[$poolCount];} 
	(
		{$i<$poolCount}? 
			{println("  Constant index: " + $i);}
			constantElement
			{
				String tagByte = $constantElement.ctx.BYTE().getText();

				if ("01".equals(tagByte)) {
					String utf8ValueHex = $constantElement.ctx.utf8Data().utf8Value().getText();
					$utf8ConstantArray[$i] = hexToString(utf8ValueHex);
				}
			 	else if ("05".equals(tagByte) || "06".equals(tagByte))
			 		// 8 byte constants are considered to use two indices... official specification
			 		// https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.4.5
			 		$i++;

			 	$i++;
			}
	)*;


constantElement
	locals[int tag]:
	BYTE
	{$tag = hexToInt($BYTE.text);}
	(
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
		| {$tag == 18}? {println("    InvokeDynamic");} invokeDynamicData
	);


utf8Data:
	twoBytesWithLog["    Length"]
	utf8Value[hexToInt($twoBytesWithLog.text)] {println("    Value: " + hexToString($utf8Value.text));};

utf8Value[int length]: someBytes[$length];

integerData: fourBytes;

floatData: fourBytes;

longData: fourBytes fourBytes;

doubleData: fourBytes fourBytes;

classData: twoBytesWithLog["    Class index"];

stringData: twoBytesWithLog["    String index"];

fieldRefData: twoForTwo["    Class", "    Name and type"];
methodRefData: twoForTwo["    Class", "    Name and type"];
interfaceMethodRefData: twoForTwo["    Class", "    Name and type"];

nameAndTypeData: twoForTwo["    Name", "    Descriptor"];

methodHandleData:
	BYTE {println("    Reference kind: " + hexToInt($BYTE.text));}
	twoBytesWithLog["Reference index"];

methodTypeData: twoBytesWithLog["    Descriptor index"];

invokeDynamicData: twoForTwo["    Bootstrap attribute method", "    Name and type"];

twoForTwo[String first, String second]:
	twoBytesWithLog[$first+" index"]
	twoBytesWithLog[$second+" index"];




accessFlags:
	twoBytes {println("Access flags: 0x" + $twoBytes.text);};


thisClass:
	twoBytesWithLog["This class index"];


superClass:
	twoBytesWithLog["Super class index"];


interfaceStuff
	locals[int i = 0, int interfaceCount]:
	twoBytesWithLog["Interface count"]
	{$interfaceCount = hexToInt($twoBytesWithLog.text);} 
	(
		{$i<$interfaceCount}? 

			twoBytesWithLog["  Interface index"]

			{$i++;}
	)*;


fieldStorage[String[] utf8ConstantArray]
	locals[int i = 0, int fieldCount]:
	twoBytesWithLog["Field count"]
	{$fieldCount = hexToInt($twoBytesWithLog.text);} 
	(
		{$i<$fieldCount}? 
			{println("  Field index: " + $i);}
			fieldElement[$utf8ConstantArray]
			{$i++;}
	)*;

fieldElement[String[] utf8ConstantArray]:
	twoBytes {println("    Access flags: 0x" + $twoBytes.text);}
	twoBytesWithLog["    Name index"]
	{println("    Name: " + $utf8ConstantArray[hexToInt($twoBytesWithLog.text)]);}
	twoBytesWithLog["    Descriptor index"]
	{println("    Descriptor: " + $utf8ConstantArray[hexToInt($twoBytesWithLog.text)]);}
	fieldAttributeStorage[$utf8ConstantArray];

fieldAttributeStorage[String[] utf8ConstantArray]
	locals[int i = 0, int fieldAttributeCount, String attributeName]:
	twoBytesWithLog["    Attribute count"]
	{$fieldAttributeCount = hexToInt($twoBytesWithLog.text);} 
	(
		{$i<$fieldAttributeCount}? 
			{println("      Attribute index: " + $i);}

			fieldAttribute[$utf8ConstantArray]

			{$i++;}
	)*;


fieldAttribute[String[] utf8ConstantArray]
	locals[String attributeName]:
	twoBytesWithLog["        Attribute name index"]
	{
		$attributeName = $utf8ConstantArray[hexToInt($twoBytesWithLog.text)];
		println("        Attribute name: " + $attributeName);
	}
	(
		{"ConstantValue".equals($attributeName)}? constantValueData
		| {"Synthetic".equals($attributeName)}? syntheticData
		| {"Signature".equals($attributeName)}? signatureData
		| {"Deprecated".equals($attributeName)}? deprecatedData
		| {"RuntimeVisibleAnnotations".equals($attributeName)}? fieldAnnotationsStorage[$utf8ConstantArray]
		| {"RuntimeInvisibleAnnotations".equals($attributeName)}? fieldAnnotationsStorage[$utf8ConstantArray]
	);


constantValueData:
	fourBytesWithLog["        Attribute length"]
	twoBytesWithLog["        Constant value"];

syntheticData:
	fourBytesWithLog["        Attribute length"];

signatureData:
	fourBytesWithLog["        Attribute length"]
	twoBytesWithLog["        Signature value"];

deprecatedData:
	fourBytesWithLog["        Attribute length"];

fieldAnnotationsStorage[String[] utf8ConstantArray]
	locals[int i = 0, int annotationCount]:
	fourBytesWithLog["        Attribute length"]
	twoBytesWithLog["        Annotation count"]
	{$annotationCount = hexToInt($twoBytesWithLog.text);} 
	(
		{$i<$annotationCount}? 
			{println("          Annotation index: " + $i);}
			fieldAnnotation[$utf8ConstantArray]
			{$i++;}
	)*;

fieldAnnotation[String[] utf8ConstantArray]
	locals[int i, int elementValuePairCount]:
	twoBytesWithLog["          Type index"]
	{println("          Type: " + $utf8ConstantArray[hexToInt($twoBytesWithLog.text)]);}
	twoBytesWithLog["          Number of element-value pairs"]
	{$elementValuePairCount = hexToInt($twoBytesWithLog.text);} 
	(
		{$i<$elementValuePairCount}? 
			{println("            Element-value pair index: " + $i);}
			{if ($elementValuePairCount > 0) throw new RuntimeException("Not implemented!");}
			twoBytesWithLog["              Index"]
			{$i++;}
	)*;


twoBytesWithLog[String logMessage]:
	twoBytes {println($logMessage + ": " + hexToInt($twoBytes.text));};

fourBytesWithLog[String logMessage]:
	fourBytes {println($logMessage + ": " + hexToInt($fourBytes.text));};


twoBytes: BYTE BYTE;

fourBytes: BYTE BYTE BYTE BYTE;

someBytes[int length] locals[int i = 0]: ( {$i<$length}? BYTE {$i++;} )*;
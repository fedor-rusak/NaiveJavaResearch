parser grammar ClassFileParser;

tokens { BYTE }

@header {
package ru.fedor_rusak.microjvm.antlr_class_file_parsing;
}

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
	locals[String[] utf8ConstantArray, int[] classArray]:
	magic
	minorVersion
	majorVersion
	constantPoolStorage
	{
		$utf8ConstantArray = $constantPoolStorage.utf8ConstantArray;
		$classArray = $constantPoolStorage.classArray;
	}
	accessFlags
	thisClass
	superClass
	interfaceStorage[$utf8ConstantArray, $classArray]
	fieldStorage[$utf8ConstantArray]
	methodStorage[$utf8ConstantArray]
	attributeStorage[$utf8ConstantArray]
	EOF;


magic:
	fourBytes
	{if ("CAFEBABE".equals($fourBytes.text) == false) throw new RuntimeException("Magic value is wrong!");}
	{println("Magic: " + $fourBytes.text);};

minorVersion: twoBytes {println("Minor version: " + hexToInt($twoBytes.text));};

majorVersion: twoBytes {println("Major version: " + hexToInt($twoBytes.text));};

constantPoolStorage
	returns [String[] utf8ConstantArray, int[] classArray]
	locals[int i = 1, int poolCount]:
	twoBytesWithLog["Constant pool count"]
	{
		$poolCount = hexToInt($twoBytesWithLog.text);
		$utf8ConstantArray = new String[$poolCount];
		$classArray = new int[$poolCount];
	}
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
				else if ("07".equals(tagByte)) {
					String classIndexHex = $constantElement.ctx.classData().twoBytesWithLog().getText();
					$classArray[$i] = hexToInt(classIndexHex);
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
	utf8Value[hexToInt($twoBytesWithLog.text)]
	{println("    Value: " + hexToString($utf8Value.text));};

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
	BYTE
	{println("    Reference kind: " + hexToInt($BYTE.text));}
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


interfaceStorage[String[] utf8ConstantArray, int[] classArray]
	locals[int i = 0, int interfaceCount, int index]:
	twoBytesWithLog["Interface count"]
	{$interfaceCount = hexToInt($twoBytesWithLog.text);} 
	(
		{$i<$interfaceCount}? 
			twoBytes
			{
				println("  Interface index: " + $i);
				$index = hexToInt($twoBytes.text);
				println("    Class index: " + $index);
				println("    Class: " + $utf8ConstantArray[$classArray[$index]]);
			}
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
	twoBytes
	{println("    Access flags: 0x" + $twoBytes.text);}
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
	twoBytesWithLog["        Name index"]
	{
		$attributeName = $utf8ConstantArray[hexToInt($twoBytesWithLog.text)];
		println("        Name: " + $attributeName);
	}
	(
		{"ConstantValue".equals($attributeName)}? constantValueData
		| {"Synthetic".equals($attributeName)}? syntheticData
		| {"Signature".equals($attributeName)}? signatureData
		| {"Deprecated".equals($attributeName)}? deprecatedData["        "]
		| {"RuntimeVisibleAnnotations".equals($attributeName)}? fieldAnnotationsStorage[$utf8ConstantArray]
		| {"RuntimeInvisibleAnnotations".equals($attributeName)}? fieldAnnotationsStorage[$utf8ConstantArray]
	);


constantValueData:
	fourBytesWithLog["        Length"]
	twoBytesWithLog["        Constant value"];

syntheticData:
	fourBytesWithLog["        Length"];

signatureData:
	fourBytesWithLog["        Length"]
	twoBytesWithLog["        Signature value"];

deprecatedData[String prefix]:
	fourBytesWithLog[prefix+"Length"];

fieldAnnotationsStorage[String[] utf8ConstantArray]
	locals[int i = 0, int annotationCount]:
	fourBytesWithLog["        Length"]
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
	twoBytesWithLog["            Type index"]
	{println("            Type: " + $utf8ConstantArray[hexToInt($twoBytesWithLog.text)]);}
	twoBytesWithLog["            Element-value pairs count"]
	{$elementValuePairCount = hexToInt($twoBytesWithLog.text);} 
	(
		{$i<$elementValuePairCount}? 
			{println("              Element-value pair index: " + $i);}
			elementValuePair[$utf8ConstantArray]
			{$i++;}
	)*;

elementValuePair[String[] utf8ConstantArray]
	locals[String tag]:
	twoBytesWithLog["                Name index"]
	{println("                Name: " + $utf8ConstantArray[hexToInt($twoBytesWithLog.text)]);}
	BYTE
	{$tag = hexToString($BYTE.text);}
	(
		{"s".equals($tag)}? stringElementValuePairData[$utf8ConstantArray]
	);

stringElementValuePairData[String[] utf8ConstantArray]:
	twoBytesWithLog["                Constant value index"]
	{println("                Constant value: " + $utf8ConstantArray[hexToInt($twoBytesWithLog.text)]);};

methodStorage[String[] utf8ConstantArray]
	locals[int i, int methodCount]:
	twoBytesWithLog["Method count"]
	{$methodCount = hexToInt($twoBytesWithLog.text);} 
	(
		{$i<$methodCount}? 
			{println("  Method index: " + $i);}
			methodElement[$utf8ConstantArray]
			{$i++;}
	)*;


methodElement[String[] utf8ConstantArray]:
	twoBytes
	{println("    Access flags: 0x" + $twoBytes.text);}
	twoBytesWithLog["    Name index"]
	{println("    Name: " + $utf8ConstantArray[hexToInt($twoBytesWithLog.text)]);}
	twoBytesWithLog["    Descriptor index"]
	{println("    Descriptor: " + $utf8ConstantArray[hexToInt($twoBytesWithLog.text)]);}
	methodAttributeStorage[$utf8ConstantArray];


methodAttributeStorage[String[] utf8ConstantArray]
	locals[int i = 0, int fieldAttributeCount, String attributeName]:
	twoBytesWithLog["    Attribute count"]
	{$fieldAttributeCount = hexToInt($twoBytesWithLog.text);} 
	(
		{$i<$fieldAttributeCount}? 
			{println("      Attribute index: " + $i);}
			methodAttribute[$utf8ConstantArray]
			{$i++;}
	)*;

methodAttribute[String[] utf8ConstantArray]
	locals[String attributeName]:
	twoBytesWithLog["        Name index"]
	{
		$attributeName = $utf8ConstantArray[hexToInt($twoBytesWithLog.text)];
		println("        Name: " + $attributeName);
	}
	(
		{"Code".equals($attributeName)}? codeData[$utf8ConstantArray]
		| {"Synthetic".equals($attributeName)}? syntheticData
		| {"Signature".equals($attributeName)}? signatureData
		| {"Deprecated".equals($attributeName)}? deprecatedData["        "]
		| {"RuntimeVisibleAnnotations".equals($attributeName)}? fieldAnnotationsStorage[$utf8ConstantArray]
		| {"RuntimeInvisibleAnnotations".equals($attributeName)}? fieldAnnotationsStorage[$utf8ConstantArray]
	);

codeData[String[] utf8ConstantArray]
	locals[int codeLength]:
	fourBytesWithLog["        Length"]
	twoBytesWithLog["        Maximum stack size"]
	twoBytesWithLog["        Maximum local variable count"]
	fourBytesWithLog["        Code length"]
	{$codeLength = hexToInt($fourBytesWithLog.text);}
	someBytes[$codeLength]
	exceptionStorage
	codeAttributeStorage[$utf8ConstantArray]
	;

exceptionStorage
	locals[int i, int exceptionCount]:
	twoBytesWithLog["        Exception table length"]
	{$exceptionCount = hexToInt($twoBytesWithLog.text);} 
	(
		{$i<$exceptionCount}? 
			{println("          Exception index: " + $i);}
			twoBytes
			twoBytes
			twoBytes
			twoBytes
			{$i++;}
	)*;


codeAttributeStorage[String[] utf8ConstantArray]
	locals[int i, int attributeCount]:
	twoBytesWithLog["        Attribute count"]
	{$attributeCount = hexToInt($twoBytesWithLog.text);} 
	(
		{$i<$attributeCount}? 
			{println("          Attribute index: " + $i);}
			codeAttribute[$utf8ConstantArray]
			{$i++;}
	)*;


codeAttribute[String[] utf8ConstantArray]
	locals[String attributeName]:
	twoBytesWithLog["            Name index"]
	{
		$attributeName = $utf8ConstantArray[hexToInt($twoBytesWithLog.text)];
		println("            Name: " + $attributeName);
	}
	(
		{"LineNumberTable".equals($attributeName)}? lineNumberTableData
		| {"LocalVariableTable".equals($attributeName)}? notImplementedData
		| {"LocalVariableTypeTable".equals($attributeName)}? notImplementedData
		| {"StackMapTable".equals($attributeName)}? stackMapTableData
	);


lineNumberTableData:
	fourBytesWithLog["            Length"]
	lineNumberStorage;

lineNumberStorage
	locals[int i, int lineNumberCount]:
	twoBytesWithLog["              Line number count"]
	{$lineNumberCount = hexToInt($twoBytesWithLog.text);} 
	(
		{$i<$lineNumberCount}? 
			{println("                Line number index: " + $i);}
			fourBytes
			{$i++;}
	)*;

notImplementedData:
	twoBytes
	{if ($twoBytes.text != null) throw new RuntimeException("Not implemented!");};

stackMapTableData:
	fourBytesWithLog["            Length"]
	stackMapEntryStorage;

stackMapEntryStorage
	locals[int i, int entryCount]:
	twoBytesWithLog["            Entry count"]
	{$entryCount = hexToInt($twoBytesWithLog.text);} 
	(
		{$i<$entryCount}? 
			{println("              Entry index: " + $i);}
			stackMapEntryElement
			{$i++;}
	)*;

stackMapEntryElement
	locals[int frameType, String prefix = "                Type"]:
	BYTE
	{
		$frameType = hexToInt($BYTE.text);
		println($prefix+" number: "+$frameType);
	}
	{if ($frameType >= 0 && $frameType <= 63) println("                Type: SAME");}
	(
		{$frameType >= 64 && $frameType <= 127}? {println($prefix+": SAME_LOCALS_1_STACK_ITEM");} twoBytes
		| {$frameType == 247}? {println($prefix+": SAME_LOCALS_1_STACK_ITEM_EXTENDED");} twoBytes
		| {$frameType >= 248 && $frameType <= 250}? {println($prefix+": CHOP");} frameChopData
		| {$frameType == 251}? {println($prefix+": SAME_FRAME_EXTENDED");} twoBytes
		| {$frameType >= 252 && $frameType <= 254}? {println($prefix+": APPEND");} frameAppendData[$frameType-251]
		| {$frameType >= 255}? {println($prefix+": FULL_FRAME");} twoBytes
	);

frameChopData:
	twoBytesWithLog["                Offset delta"];

frameAppendData[int typeInfoCount]
	locals[int i]:
	twoBytes
	{println("                Type info count: " + $typeInfoCount);}
	(
		{$i<$typeInfoCount}? 
			{println("                  Index: " + $i);}
			typeInfo
			{$i++;}
	)*;


typeInfo
	locals[int typeInfoTag]:
	BYTE
	{
		$typeInfoTag = hexToInt($BYTE.text);
		println("                    Type info tag: " + $typeInfoTag);
	}
	({$typeInfoTag == 7 || $typeInfoTag == 8}? twoBytes)*;


attributeStorage[String[] utf8ConstantArray]
	locals[int i, int attributeCount]:
	twoBytesWithLog["Attribute count"]
	{
		$attributeCount = hexToInt($twoBytesWithLog.text);
	} 
	(
		{$i<$attributeCount}? 
			{println("  Attribute index: " + $i);}
			attributeElement[$utf8ConstantArray]
			{$i++;}
	)*;


attributeElement[String[] utf8ConstantArray]
	locals[String attributeName]:
	twoBytesWithLog["    Name index"]
	{
		$attributeName = $utf8ConstantArray[hexToInt($twoBytesWithLog.text)];
		println("    Name: " + $attributeName);
	}
	(
		{"InnerClasses".equals($attributeName)}? innerClassesData[$utf8ConstantArray]
		| {"EnclosingMethod".equals($attributeName)}? enclosingMethodData[$utf8ConstantArray]
		| {"Synthetic".equals($attributeName)}? syntheticData
		| {"Signature".equals($attributeName)}? signatureData
		| {"SourceFile".equals($attributeName)}? sourceFileData[$utf8ConstantArray]
		| {"Deprecated".equals($attributeName)}? deprecatedData["    "]
		| {"RuntimeVisibleAnnotations".equals($attributeName)}? fieldAnnotationsStorage[$utf8ConstantArray]
		| {"RuntimeInvisibleAnnotations".equals($attributeName)}? fieldAnnotationsStorage[$utf8ConstantArray]
	);


innerClassesData[String[] utf8ConstantArray]
	locals [int sourceFileIndex]:
	fourBytesWithLog["    Length"]
	innerClassesStorage[$utf8ConstantArray];

innerClassesStorage[String[] utf8ConstantArray]
	locals[int i, int innerClassCount]:
	twoBytesWithLog["    Inner class count"]
	{
		$innerClassCount = hexToInt($twoBytesWithLog.text);
	} 
	(
		{$i<$innerClassCount}? 
			{println("      Index: " + $i);}
			innerClassElement[$utf8ConstantArray]
			{$i++;}
	)*;

innerClassElement[String[] utf8ConstantArray]:
	twoBytesWithLog["        Inner class info index"]
	twoBytesWithLog["        Outer class info index"]
	twoBytesWithLog["        Inner name index"]
	twoBytes
	{println("        Access flags: 0x" + $twoBytes.text);};


enclosingMethodData[String[] utf8ConstantArray]:
	fourBytesWithLog["    Length"]
	twoBytesWithLog["    Class index"]
	twoBytesWithLog["    Method index"];


sourceFileData[String[] utf8ConstantArray]
	locals [int sourceFileIndex]:
	fourBytesWithLog["    Length"]
	twoBytesWithLog["    Source file index"]
	{$sourceFileIndex = hexToInt($twoBytesWithLog.text);}
	{println("    Source file: " + $utf8ConstantArray[$sourceFileIndex]);};

twoBytesWithLog[String logMessage]:
	twoBytes {println($logMessage + ": " + hexToInt($twoBytes.text));};

fourBytesWithLog[String logMessage]:
	fourBytes {println($logMessage + ": " + hexToInt($fourBytes.text));};


twoBytes: BYTE BYTE;

fourBytes: BYTE BYTE BYTE BYTE;

someBytes[int length] locals[int i = 0]: ( {$i<$length}? BYTE {$i++;} )*;
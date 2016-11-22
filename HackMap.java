import java.io.File;
import java.io.FileInputStream;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

public class HackMap {


	private Map<String, Object> storage = new HashMap<String, Object>();

	public boolean contains(String key) {
		return storage.containsKey(key);
	}

	public void put(String key, Object value) {
		storage.put(key, value);
	}

	public Object getObject(String key) {
		return storage.get(key);
	}

	public int getInt(String key) {
		return (Integer) storage.get(key);
	}

	public boolean getBoolean(String key) {
		return (Boolean) storage.get(key);
	}

	public String getString(String key) {
		return (String) storage.get(key);
	}

	public HackMap getHackMap(String key) {
		return (HackMap) storage.get(key);
	}


	private static byte[] readContentIntoByteArray(File file) {
		FileInputStream fileInputStream = null;
		byte[] bFile = new byte[(int) file.length()];

		try	{
			//convert file into array of bytes
			fileInputStream = new FileInputStream(file);
			fileInputStream.read(bFile);
			fileInputStream.close();
		}
		catch (Exception e) {
			try {
				if (fileInputStream != null) fileInputStream.close();
			}
			catch (Exception eFromClose) {
				eFromClose.printStackTrace();
			}

			e.printStackTrace();

			bFile = new byte[0];
		}

		return bFile;
	}


	public static int signBitAsValue(byte value) {
		return value & 0xFF;
	}

	public static boolean unsignedByteCompare(byte one, int another) {
		return signBitAsValue(one) == another;
	}

	public static int byteArrayToInt(byte[] b) {
		int result = 0;

		for (int i = 0; i < b.length; i++) {
			// println(String.format("%02x", b[i]));
			result = (result << 8) + (b[i] & 0xff);
		}

		return result;
	}

	public static int byteArrayRangeToInt(byte[] input, int from, int length) {
		return byteArrayToInt(Arrays.copyOfRange(input, from, from+length));
	}

	public static short byteArrayPartToShort(byte[] input, int from) {
		return (short) ( ((input[from]&0xFF)<<8) | (input[from+1]&0xFF) );
	}

	private static void println(String string) {
		System.out.println(string);
	}

	private static void println(int value) {
		println(String.valueOf(value));
	}

	private static void printlnError(String string) {
		System.err.println(string);
	}



	public static HackMap checkByteArrayNonZeroLength(byte[] input) {
		HackMap result = new HackMap();

		if (input.length > 0) {
			result.put("Succeded", true);
			result.put("Message", "File length is " + input.length + " bytes");
		}
		else {
			result.put("Succeded", false);
			result.put("ErrorText", "Failure reading file or it is empty");
		}

		return result;
	}

	public static HackMap checkMagic(byte[] input) {
		HackMap result = new HackMap();

		final int JAVA_CLASS_MAGIC = 0xCAFEBABE;

		if (input.length >= 4 && byteArrayRangeToInt(input, 0, 4) == JAVA_CLASS_MAGIC) {
			result.put("Succeded", true);
			result.put("Message", "Magic is right");
		}
		else {
			result.put("Succeded", false);
			result.put("ErrorText", "First bytes must contain \"magic\" values");
		}

		return result;
	}

	public static HackMap checkVersionNumber(byte[] input) {
		HackMap result = new HackMap();

		if (input.length >= 8) {
			final String VERSION_NOT_FOUND = "not found";
			String majorVersionJava = VERSION_NOT_FOUND;

			int minorVersionNumber = byteArrayRangeToInt(input, 4, 2);
			//not sure what to do with it

			int majorVersionNumber = byteArrayRangeToInt(input, 6, 2);

			switch(majorVersionNumber) {
				case 0x35: majorVersionJava = "Java SE 9"; break;
				case 0x34: majorVersionJava = "Java SE 8"; break;
				case 0x33: majorVersionJava = "Java SE 7"; break;
				case 0x32: majorVersionJava = "Java SE 6.0"; break;
				case 0x31: majorVersionJava = "Java SE 5.0"; break;
				case 0x30: majorVersionJava = "JDK 1.4"; break;
				case 0x2F: majorVersionJava = "JDK 1.3"; break;
				case 0x2E: majorVersionJava = "JDK 1.2"; break;
				case 0x2D: majorVersionJava = "JDK 1.1"; break;
			}

			if (majorVersionJava.equals(VERSION_NOT_FOUND)) {
				result.put("Succeded", false);
				result.put("ErrorText", "Can not identify version of java compiler");
			}
			else {
				result.put("Succeded", true);
				result.put("MajorVersionNumber", majorVersionNumber);
				result.put("Message", "Major version is " + majorVersionNumber);
			}
		}
		else {
			result.put("Succeded", false);
			result.put("ErrorText", "Not enough data to determine java compiler version");
		}

		return result;
	}

	public static HackMap parseConstants(byte[] input) {
		HackMap result = new HackMap();

		String message;
		int numberOfConstants = -1;

		if (input.length >= 10) {
			numberOfConstants = byteArrayRangeToInt(input, 8, 2);

			result.put("NumberOfConstants", numberOfConstants);

			message = "Number of constants is " + numberOfConstants + "\n";


			int index = 10;

			String[] arrayOfConstants = new String[numberOfConstants+1];
			int[] arrayOfInts = new int[numberOfConstants+1];
			int[] arrayOfIntsSecond = new int[numberOfConstants+1];

			if (numberOfConstants > 0) {
				// CONSTANT_Utf8				1
				// CONSTANT_Integer				3
				// CONSTANT_Float				4
				// CONSTANT_Long				5
				// CONSTANT_Double				6
				// CONSTANT_Class				7
				// CONSTANT_String				8
				// CONSTANT_Fieldref			9
				// CONSTANT_Methodref			10
				// CONSTANT_InterfaceMethodref	11
				// CONSTANT_NameAndType			12
				// CONSTANT_MethodHandle 	    15
				// CONSTANT_MethodType 	        16
				// CONSTANT_InvokeDynamic 	    18

				int i = 1;

				while(i < numberOfConstants && input.length >= index+1) {
					int constantTag = signBitAsValue(input[index]);
					index++;

					String type = "";

					if (1 == constantTag) {
						type = "Utf8";
						// CONSTANT_Utf8_info {
						// 	u1 tag;
						// 	u2 length;
						// 	u1 bytes[length];
						// }

						int length = byteArrayRangeToInt(input, index, 2);
						index += 2;
						message += length + "\n";

						byte[] utf8Data = Arrays.copyOfRange(input, index, index+length);
						message += new String(utf8Data) + "\n";

						arrayOfConstants[i] = new String(utf8Data);

						index += length;
					}
					else if (3 == constantTag) {
						type = "Integer";
						// CONSTANT_Integer_info {
						// 	u1 tag;
						// 	u4 bytes;
						// }
						message += byteArrayRangeToInt(input, index, 4) + "\n";
						index += 4;
					}
					else if (4 == constantTag) {
						type = "Float";
						// CONSTANT_Float_info {
						// 	u1 tag;
						// 	u4 bytes;
						// }

						index += 4;
					}
					else if (5 == constantTag) {
						type = "Long";
						// CONSTANT_Long_info {
						// 	u1 tag;
						// 	u4 high_bytes;
						// 	u4 low_bytes;
						// }

						index += 8;
					}
					else if (6 == constantTag) {
						type = "Double";
						// CONSTANT_Double_info {
						// 	u1 tag;
						// 	u4 high_bytes;
						// 	u4 low_bytes;
						// }

						index += 8;
					}
					else if (7 == constantTag) {
						type = "Class";
						// CONSTANT_Class_info {
						// 	u1 tag;
						// 	u2 name_index;
						// }
						arrayOfInts[i] = byteArrayRangeToInt(input, index, 2);
						message += arrayOfInts[i] + "\n";

						index += 2;
					}
					else if (8 == constantTag) {
						type = "String";
						// CONSTANT_String_info {
						// 	u1 tag;
						// 	u2 string_index;
						// }

						arrayOfInts[i] = byteArrayRangeToInt(input, index, 2);
						message += arrayOfInts[i] + "\n";

						index += 2;
					}
					else if (9 == constantTag) {
						type = "Field Reference";
						// CONSTANT_Fieldref_info {
						// 	u1 tag;
						// 	u2 class_index;
						// 	u2 name_and_type_index;
						// }
						arrayOfInts[i] = byteArrayRangeToInt(input, index, 2);
						message += arrayOfInts[i] + "\n";

						arrayOfIntsSecond[i] = byteArrayRangeToInt(input, index+2, 2);
						message += arrayOfIntsSecond[i] + "\n";

						index += 4;
					}
					else if (10 == constantTag) {
						type = "Method Reference";
						// CONSTANT_Methodref_info {
						// 	u1 tag;
						// 	u2 class_index;
						// 	u2 name_and_type_index;
						// }

						message += byteArrayRangeToInt(input, index, 2) + "\n";
						message += byteArrayRangeToInt(input, index+2, 2) + "\n";

						index+=4;
					}
					else if (11 == constantTag) {
						type = "Interface Method Reference";
						// CONSTANT_InterfaceMethodref_info {
						// 	u1 tag;
						// 	u2 class_index;
						// 	u2 name_and_type_index;
						// }

						message += byteArrayRangeToInt(input, index, 2) + "\n";
						message += byteArrayRangeToInt(input, index+2, 2) + "\n";

						index+=4;
					}
					else if (12 == constantTag) {
						type = "Name and Type";
						// CONSTANT_NameAndType_info {
						// 	u1 tag;
						// 	u2 name_index;
						// 	u2 descriptor_index;
						// }

						arrayOfInts[i] = byteArrayRangeToInt(input, index, 2);
						message += arrayOfInts[i] + "\n";

						arrayOfIntsSecond[i] = byteArrayRangeToInt(input, index+2, 2);
						message += arrayOfIntsSecond[i] + "\n";

						index+=4;
					}
					else if (15 == constantTag){
						type = "Method Handle";
						// CONSTANT_MethodHandle_info {
						//     u1 tag;
						//     u1 reference_kind;
						//     u2 reference_index;
						// }

						index += 3;
					}
					else if (16 == constantTag){
						type = "Method Type";
						// CONSTANT_MethodType_info {
						//     u1 tag;
						//     u2 descriptor_index;
						// }

						index += 2;
					}
					else if (18 == constantTag) {
						type = "Invoke Dynamic";
						// CONSTANT_InvokeDynamic_info {
						//     u1 tag;
						//     u2 bootstrap_method_attr_index;
						//     u2 name_and_type_index;
						// }

						index += 4;
					}


					if ("".equals(type)) {
						result.put("Succeded", false);
						result.put("ErrorText", "Failed to get tag for constant["+i+"]");
						break;
					}


					message += "  Constant["+i+"] is " + type + "\n";


					i++;
				}

				if (result.contains("Succeded") == false) {
					//if error was not triggered during constant analysis
					result.put("Succeded", true);
					result.put("Message", message);
					result.put("NextIndex", index);
					result.put("ArrayOfUTF8Constants", arrayOfConstants);
					result.put("ArrayOfInts", arrayOfInts);
					result.put("ArrayOfIntsSecond", arrayOfIntsSecond);
				}
			}
			else {
				result.put("Succeded", false);
				result.put("ErrorText", "Wrong number of constants");
			}
		}
		else {
			result.put("Succeded", false);
			result.put("ErrorText", "Not enough data to get number of constants");
		}


		return result;
	}

	public static HackMap checkIndexReachedArrayLength(byte[] input, int currentIndex) {
		HackMap result = new HackMap();

		if (input.length == currentIndex) {
			result.put("Succeded", true);
			result.put("Message", "Whole file parsing was completed");
		}
		else {
			result.put("Succeded", false);
			result.put("ErrorText", "Parts of class file were not analyzed");
		}

		return result;		
	}


	public static void evaluateResult(HackMap result, int exitCode) {
		if (result.getBoolean("Succeded"))
			println(result.getString("Message")+"\n");
		else {
			printlnError(result.getString("ErrorText"));
			System.exit(exitCode);
		}
	}


	/*
	 *  If any questions appear. Please read official specification of java class file structure
	 *  https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html
	 */
	public static void main(String[] args) {
		if (args.length == 0)
			println("Please specify class-file for analysis");
		else {
			println("File \""+args[0] + "\" will be analyzed\n");

			byte[] data = readContentIntoByteArray(new File(args[0]));


			HackMap lengthCheckResult = checkByteArrayNonZeroLength(data);

			evaluateResult(lengthCheckResult, 1);


			HackMap magicCheckResult = checkMagic(data);

			evaluateResult(magicCheckResult, 2);


			HackMap versionNumberCheckResult = checkVersionNumber(data);

			evaluateResult(versionNumberCheckResult, 3);


			HackMap constantParseResult = parseConstants(data);

			evaluateResult(constantParseResult, 4);

			int index = constantParseResult.getInt("NextIndex");
			String[] arrayOfConstants = (String[]) constantParseResult.getObject("ArrayOfUTF8Constants");
			int[] arrayOfInts = (int[]) constantParseResult.getObject("ArrayOfInts");
			int[] arrayOfIntsSecond = (int[]) constantParseResult.getObject("ArrayOfIntsSecond");


			if (data.length >= index+1) {
				int value = byteArrayRangeToInt(data, index, 2);
				println("Access flags bitmask: " + String.format("%02x", data[index]) + String.format("%02x", data[index+1]) + "\n");
				// ACC_PUBLIC 	0x0001 	Declared public; may be accessed from outside its package.
				// ACC_FINAL 	0x0010 	Declared final; no subclasses allowed.
				// ACC_SUPER 	0x0020 	Treat superclass methods specially when invoked by the invokespecial instruction.
				// ACC_INTERFACE 	0x0200 	Is an interface, not a class.
				// ACC_ABSTRACT 	0x0400 	Declared abstract; must not be instantiated.
				// ACC_SYNTHETIC 	0x1000 	Declared synthetic; not present in the source code.
				// ACC_ANNOTATION 	0x2000 	Declared as an annotation type.
				// ACC_ENUM 	0x4000 	Declared as an enum type. 

				index +=2;
			}
			else {
				printlnError("No data for access flags!");
				System.exit(1);
			}


			if (data.length >= index+1) {
				int value = byteArrayRangeToInt(data, index, 2);
				println("This class index: " + value + "\n");

				index +=2;
			}
			else {
				printlnError("No this class index!");
				System.exit(1);
			}

			if (data.length >= index+1) {
				int value = byteArrayRangeToInt(data, index, 2);
				println("Super class index: " + value + "\n");

				index +=2;
			}
			else {
				printlnError("No super class index!");
				System.exit(1);
			}

			int numberOfInterfaces = 0;
			if (data.length >= index+1) {
				numberOfInterfaces = byteArrayRangeToInt(data, index, 2);
				println("Number of interfaces: " + numberOfInterfaces + "\n");

				index +=2;
			}
			else {
				printlnError("No number of interfaces!");
				System.exit(1);
			}


			for (int i = 0; i < numberOfInterfaces; i++) {
				int interfaceIndex = byteArrayRangeToInt(data, index, 2);

				println("  interface[" + (i+1) + "] is " + interfaceIndex + "\n");

				index += 2;
			}


			int numberOfFields = 0;

			if (data.length >= index+1) {
				numberOfFields = byteArrayRangeToInt(data, index, 2);
				println("Number of fields: " + numberOfFields + "\n");

				index +=2;
			}
			else {
				printlnError("No number of fields");
				System.exit(1);
			}


			for (int i = 0; i < numberOfFields; i++) {
				// field_info {
				// 	u2 access_flags;
				// 	u2 name_index;
				// 	u2 descriptor_index;
				// 	u2 attributes_count;
				// 	attribute_info attributes[attributes_count];
				// }

				println("  access flags bitmask: " + String.format("%02x", data[index]) + String.format("%02x", data[index+1]) + "\n");
				index +=2;
				// ACC_PUBLIC 	0x0001 	Declared public; may be accessed from outside its package.
				// ACC_PRIVATE 	0x0002 	Declared private; usable only within the defining class.
				// ACC_PROTECTED 	0x0004 	Declared protected; may be accessed within subclasses.
				// ACC_STATIC 	0x0008 	Declared static.
				// ACC_FINAL 	0x0010 	Declared final; never directly assigned to after object construction (JLS §17.5).
				// ACC_VOLATILE 	0x0040 	Declared volatile; cannot be cached.
				// ACC_TRANSIENT 	0x0080 	Declared transient; not written or read by a persistent object manager.
				// ACC_SYNTHETIC 	0x1000 	Declared synthetic; not present in the source code.
				// ACC_ENUM 	0x4000 	Declared as an element of an enum. 


				int nameIndex = byteArrayRangeToInt(data, index, 2);
				index +=2;
				println("  name index: " + nameIndex + "");
				println("  name value: " + arrayOfConstants[nameIndex] + "\n");

				int descriptorIndex = byteArrayRangeToInt(data, index, 2);
				index +=2;
				println("  descriptor index: " + descriptorIndex + "\n");

				int numberOfAttributes = byteArrayRangeToInt(data, index, 2);
				index +=2;
				println("  number of attributes: " + numberOfAttributes + "\n");

				for (int j = 0; j < numberOfAttributes; j++) {
					int attributeNameIndex = byteArrayRangeToInt(data, index, 2);
					index += 2;
					println("    attribute name index: " + attributeNameIndex + "");
					String attributeName = arrayOfConstants[attributeNameIndex];
					println("    attribute name value: " + attributeName + "\n");

					if ("ConstantValue".equals(attributeName)) {
						// ConstantValue_attribute {
						// 	u2 attribute_name_index;
						// 	u4 attribute_length;
						// 	u2 constantvalue_index;
						// }

						//attribute_length    The value of the attribute_length item of a ConstantValue_attribute structure must be 2.
						//it should be unsigned int to be precise
						println("    attribute length: " + byteArrayRangeToInt(data, index, 4) + "\n");
						index+=4;

						int constantValueIndex = byteArrayRangeToInt(data, index, 2);
						index += 2;
						println("    constant value index: " + constantValueIndex + "\n");
					}
					else if ("Synthetic".equals(attributeName)) {
						// Synthetic_attribute {
						// 	u2 attribute_name_index;
						// 	u4 attribute_length;
						// }

						//attribute_length    The value of the attribute_length item is zero.
						//it should be unsigned int to be precise
						println("    attribute length: " + byteArrayRangeToInt(data, index, 4) + "\n");
						index += 4;
					}
					else if ("Signature".equals(attributeName)) {
						// Signature_attribute {
						//     u2 attribute_name_index;
						//     u4 attribute_length;
						//     u2 signature_index;
						// }

						println("    attribute length: " + byteArrayRangeToInt(data, index, 4) + "\n");
						index += 4;

						int signatureIndex = byteArrayRangeToInt(data, index, 2);
						index += 2;
						println("    signature index: " + signatureIndex + "\n");
					}
					else if ("Deprecated".equals(attributeName)) {
						// Synthetic_attribute {
						// 	u2 attribute_name_index;
						// 	u4 attribute_length;
						// }

						//attribute_length    The value of the attribute_length item is zero.
						//it should be unsigned int to be precise
						println("    attribute length: " + byteArrayRangeToInt(data, index, 4) + "\n");
						index += 4;
					}
					else if ("RuntimeVisibleAnnotations".equals(attributeName) || "RuntimeInvisibleAnnotations".equals(attributeName)) {
						// RuntimeVisibleAnnotations_attribute {
						//     u2         attribute_name_index;
						//     u4         attribute_length;
						//     u2         num_annotations;
						//     annotation annotations[num_annotations];
						// }

						// RuntimeInvisibleAnnotations_attribute {
						//     u2         attribute_name_index;
						//     u4         attribute_length;
						//     u2         num_annotations;
						//     annotation annotations[num_annotations];
						// }

						println("    attribute length: " + byteArrayRangeToInt(data, index, 4) + "\n");
						index += 4;

						int numberOfAnnotations = byteArrayRangeToInt(data, index, 2);
						index += 2;
						println("    number of annotations: " + numberOfAnnotations + "\n");

						for (int k = 0; k < numberOfAnnotations; k++) {
							// annotation {
							//     u2 type_index;
							//     u2 num_element_value_pairs;
							//     {   u2            element_name_index;
							//         element_value value;
							//     } element_value_pairs[num_element_value_pairs];
							// }

							int typeIndex = byteArrayRangeToInt(data, index, 2);
							index += 2;
							println("      type index: " + typeIndex + "");
							String typeName = arrayOfConstants[typeIndex];
							println("      type name: " + typeName + "\n");

							int numberOfValuePairs = byteArrayRangeToInt(data, index, 2);
							index += 2;
							println("      number of value pairs: " + numberOfValuePairs + "\n");

							//WORK IN PROGRESS
							for (int l = 0; l < numberOfValuePairs; l++) {
								// element_value {
								//     u1 tag;
								//     union {
								//         u2 const_value_index;

								//         {   u2 type_name_index;
								//             u2 const_name_index;
								//         } enum_const_value;

								//         u2 class_info_index;

								//         annotation annotation_value;

								//         {   u2            num_values;
								//             element_value values[num_values];
								//         } array_value;
								//     } value;
								// }
								int elementNameIndex = byteArrayRangeToInt(data, index, 2);
								index += 2;
								println("        element name index: " + elementNameIndex + "\n");
								char elementTag = (char) signBitAsValue(data[index]);
								index++;
								//The const_value_index item is used if the tag item is one of B, C, D, F, I, J, S, Z, or s. 
								String tagString = "BCDFIJSZs";
								boolean useConst = false;
								for(int m = 0; m < tagString.length(); m++) {
									if (elementTag == tagString.charAt(m)) {
										useConst = true;
										break;
									}
								}

								if (useConst) {
									int unionConstNameIndex = byteArrayRangeToInt(data, index, 2);
									println("        union constant index: " + unionConstNameIndex);
									println("        union constant value: " + arrayOfConstants[unionConstNameIndex] + "\n");
									index+=2;
								}
								else {
									//WORK IN PROGRESS
									printlnError("WAAAT " + elementTag);
									System.exit(1);
								}
							}
						}
					}

					println("    attribute[" + (j+1) + "]\n");
				}


				println("  field[" + (i+1) + "]\n");
			}


			int numberOfMethods = 0;

			if (data.length >= index+1) {
				numberOfMethods = byteArrayRangeToInt(data, index, 2);
				println("Number of methods: " + numberOfMethods + "\n");

				index +=2;
			}
			else {
				printlnError("No number of methods");
				System.exit(1);
			}

			for (int i = 0; i < numberOfMethods; i++) {
				// method_info {
				//     u2             access_flags;
				//     u2             name_index;
				//     u2             descriptor_index;
				//     u2             attributes_count;
				//     attribute_info attributes[attributes_count];
				// }

				println("  access flags bitmask: " + String.format("%02x", data[index]) + String.format("%02x", data[index+1]) + "\n");
				index +=2;
				// ACC_PUBLIC 	0x0001 	Declared public; may be accessed from outside its package.
				// ACC_PRIVATE 	0x0002 	Declared private; accessible only within the defining class.
				// ACC_PROTECTED 	0x0004 	Declared protected; may be accessed within subclasses.
				// ACC_STATIC 	0x0008 	Declared static.
				// ACC_FINAL 	0x0010 	Declared final; must not be overridden (§5.4.5).
				// ACC_SYNCHRONIZED 	0x0020 	Declared synchronized; invocation is wrapped by a monitor use.
				// ACC_BRIDGE 	0x0040 	A bridge method, generated by the compiler.
				// ACC_VARARGS 	0x0080 	Declared with variable number of arguments.
				// ACC_NATIVE 	0x0100 	Declared native; implemented in a language other than Java.
				// ACC_ABSTRACT 	0x0400 	Declared abstract; no implementation is provided.
				// ACC_STRICT 	0x0800 	Declared strictfp; floating-point mode is FP-strict.
				// ACC_SYNTHETIC 	0x1000 	Declared synthetic; not present in the source code. 


				int nameIndex = byteArrayRangeToInt(data, index, 2);
				index +=2;
				println("  name index: " + nameIndex + "");
				println("  name value: " + arrayOfConstants[nameIndex] + "\n");

				int descriptorIndex = byteArrayRangeToInt(data, index, 2);
				index +=2;
				println("  descriptor index: " + descriptorIndex + "\n");

				int numberOfAttributes = byteArrayRangeToInt(data, index, 2);
				index +=2;
				println("  number of attributes: " + numberOfAttributes + "\n");

				for (int j = 0; j < numberOfAttributes; j++) {
					int attributeNameIndex = byteArrayRangeToInt(data, index, 2);
					index += 2;
					println("    attribute name index: " + attributeNameIndex + "");
					String attributeName = arrayOfConstants[attributeNameIndex];
					println("    attribute name value: " + attributeName + "\n");

					if ("Code".equals(attributeName)) {
						// Code_attribute {
						//     u2 attribute_name_index;
						//     u4 attribute_length;
						//     u2 max_stack;
						//     u2 max_locals;
						//     u4 code_length;
						//     u1 code[code_length];
						//     u2 exception_table_length;
						//     {   u2 start_pc;
						//         u2 end_pc;
						//         u2 handler_pc;
						//         u2 catch_type;
						//     } exception_table[exception_table_length];
						//     u2 attributes_count;
						//     attribute_info attributes[attributes_count];
						// }

						//attribute_length    The value of the attribute_length item of a ConstantValue_attribute structure must be 2.
						//it should be unsigned int to be precise
						println("    attribute length: " + byteArrayRangeToInt(data, index, 4) + "\n");
						index+=4;

						int maxStack = byteArrayRangeToInt(data, index, 2);
						index += 2;
						println("    max stack: " + maxStack + "\n");

						int maxLocals = byteArrayRangeToInt(data, index, 2);
						index += 2;
						println("    max locals: " + maxLocals + "\n");

						int lengthOfCode = byteArrayRangeToInt(data, index, 4);
						println("    code length: " + lengthOfCode + "\n");
						index += 4;

						println("    No idea how to show bytecode now\n");
						int tempIndex = 0;
						while (tempIndex < lengthOfCode){
							int byteCodeValue = signBitAsValue(data[index + tempIndex]);

							String commandName = String.format("%03d", tempIndex) + ": " + String.format("%02x", byteCodeValue) + " ";

							switch(byteCodeValue) {
								case 0x03:
									commandName += "iconst_0"; //load the int value 0 onto the stack
									break;
								case 0x04:
									commandName += "iconst_1"; //load the int value 1 onto the stack
									break;
								case 0x10:
									commandName += "bipush"; //push a byte onto the stack as an integer value
									commandName += " " + data[index+tempIndex+1];
									tempIndex ++;
									break;
								case 0x12:
									commandName += "ldc"; //push a constant #index from a constant pool (String, int or float) onto the stack
									commandName += " #" + data[index+tempIndex+1];
									commandName += "\t\t\t\t//" + arrayOfConstants[arrayOfInts[data[index+tempIndex+1]]];
									tempIndex ++;
									break;
								case 0x1b:
									commandName += "iload_1"; //load an int value from local variable 1
									break;
								case 0x2a:
									commandName += "aload_0"; //load a reference onto the stack from local variable 0
									break;
								case 0x3c:
									commandName += "istore_1"; //store int value into variable 1
									break;
								case 0xa2:
									commandName += "if_icmpge"; //if value1 is greater than or equal to value2, branch to instruction at branchoffset (signed short constructed from unsigned bytes branchbyte1 << 8 + branchbyte2)
									commandName += " " + (byteArrayPartToShort(data, index+tempIndex+1) + tempIndex);
									tempIndex += 2;
									break;
								case 0xb1:
									commandName += "return"; //return void from method
									break;
								case 0xb2:
									//current implementation works ONLY for fields
									int getstaticArg = byteArrayRangeToInt(data, index+tempIndex+1, 2);
									commandName += "getstatic"; //get a static field value of a class, where the field is identified by field reference in the constant pool index (indexbyte1 << 8 + indexbyte2)
									commandName += " #" + getstaticArg;
									commandName += "\t//" + arrayOfConstants[arrayOfInts[arrayOfInts[getstaticArg]]];
									commandName += "." + arrayOfConstants[arrayOfInts[arrayOfIntsSecond[getstaticArg]]];
									String typeOrSignature = arrayOfConstants[arrayOfIntsSecond[arrayOfIntsSecond[getstaticArg]]];
									if (typeOrSignature.charAt(0) == '(') {
										//nothing to add
									}
									else {
										//field
										commandName += ":";
									}
									commandName += typeOrSignature;
									tempIndex += 2;
									break;
								case 0xb5:
									commandName += "putfield"; //set field to value in an object objectref, where the field is identified by a field reference index in constant pool (indexbyte1 << 8 + indexbyte2)
									commandName += " #" + byteArrayRangeToInt(data, index+tempIndex+1, 2);
									tempIndex += 2;
									break;
								case 0xb7:
									commandName += "invokespecial"; //invoke instance method on object objectref and puts the result on the stack (might be void); the method is identified by method reference index in constant pool (indexbyte1 << 8 + indexbyte2)
									commandName += " " + byteArrayRangeToInt(data, index+tempIndex+1, 2);
									tempIndex += 2;
									break;
								default:
									commandName += "wtf?";
									break;
							}


							println("    " + commandName +"\n");
							tempIndex++;
						} 

						index += lengthOfCode;

						int lengthOfExceptionTable = byteArrayRangeToInt(data, index, 2);
						index += 2;
						println("    length of exception table: " + lengthOfExceptionTable + "\n");

						for (int k = 0; k < lengthOfExceptionTable; k++) {
							// {   u2 start_pc;
							//     u2 end_pc;
							//     u2 handler_pc;
							//     u2 catch_type;
							// }
							println("      8 boring bytes about exception handlers\n");
							index += 8;
						}

						int numberOfCodeAttributes = byteArrayRangeToInt(data, index, 2);
						index += 2;
						println("    number of attributes: " + numberOfCodeAttributes + "\n");

						for (int k = 0; k < numberOfCodeAttributes; k++) {
							int codeAttributeNameIndex = byteArrayRangeToInt(data, index, 2);
							index += 2;
							println("      attribute name index: " + codeAttributeNameIndex + "");
							String codeAttributeName = arrayOfConstants[codeAttributeNameIndex];
							println("      attribute name value: " + codeAttributeName + "\n");

							if ("LineNumberTable".equals(codeAttributeName)) {
								// LineNumberTable_attribute {
								//     u2 attribute_name_index;
								//     u4 attribute_length;
								//     u2 line_number_table_length;
								//     {   u2 start_pc;
								//         u2 line_number;	
								//     } line_number_table[line_number_table_length];
								// }

								println("      attribute length: " + byteArrayRangeToInt(data, index, 4) + "\n");
								index += 4;

								int lengthOfLineNumberTable = byteArrayRangeToInt(data, index, 2);
								index += 2;
								println("      length of line number table: " + lengthOfLineNumberTable + "\n");

								println("      no idea how to show line number table\n");
								index += lengthOfLineNumberTable*4;
							}
							else if ("LocalVariableTable".equals(codeAttributeName)) {
								// LocalVariableTable_attribute {
								//     u2 attribute_name_index;
								//     u4 attribute_length;
								//     u2 local_variable_table_length;
								//     {   u2 start_pc;
								//         u2 length;
								//         u2 name_index;
								//         u2 descriptor_index;
								//         u2 index;
								//     } local_variable_table[local_variable_table_length];
								// }

								println("      attribute length: " + byteArrayRangeToInt(data, index, 4) + "\n");
								index += 4;

								int lengthOfLocalVariableTable = byteArrayRangeToInt(data, index, 2);
								index += 2;
								println("      length of local variable table: " + lengthOfLocalVariableTable + "\n");

								println("      no idea how to show local variable table\n");
								index += lengthOfLocalVariableTable*10;
							}
							else if ("LocalVariableTypeTable".equals(codeAttributeName)) {
								// LocalVariableTypeTable_attribute {
								//     u2 attribute_name_index;
								//     u4 attribute_length;
								//     u2 local_variable_type_table_length;
								//     {   u2 start_pc;
								//         u2 length;
								//         u2 name_index;
								//         u2 signature_index;
								//         u2 index;
								//     } local_variable_type_table[local_variable_type_table_length];
								// }

								println("      attribute length: " + byteArrayRangeToInt(data, index, 4) + "\n");
								index += 4;

								int lengthOfLocalVariableTypeTable = byteArrayRangeToInt(data, index, 2);
								index += 2;
								println("      length of local variable type table: " + lengthOfLocalVariableTypeTable + "\n");

								println("      no idea how to show local variable type table\n");
								index += lengthOfLocalVariableTypeTable*10;
							}
							else if ("StackMapTable".equals(codeAttributeName)) {
								// StackMapTable_attribute {
								//     u2              attribute_name_index;
								//     u4              attribute_length;
								//     u2              number_of_entries;
								//     stack_map_frame entries[number_of_entries];
								// }

								println("      attribute length: " + byteArrayRangeToInt(data, index, 4) + "\n");
								index += 4;

								int numberOfEntries = byteArrayRangeToInt(data, index, 2);
								index += 2;
								println("      number of entries: " + numberOfEntries + "\n");

								for (int l = 0; l < numberOfEntries; l++) {
									int frameTag = signBitAsValue(data[index]);
									index++;
									println("        frame tag: " + frameTag + "\n");

									if (frameTag <= 63) {
										// same_frame {
										//     u1 frame_type = SAME; /* 0-63 */
										// }
									}
									else if (frameTag <= 127) {
										// same_locals_1_stack_item_frame {
										//     u1 frame_type = SAME_LOCALS_1_STACK_ITEM; /* 64-127 */
										//     verification_type_info stack[1];
										// }

										int verificationTypeInfoTag = signBitAsValue(data[index]);
										index++;
										println("          verification type info tag: " + verificationTypeInfoTag + "\n");

										if (verificationTypeInfoTag == 7 || verificationTypeInfoTag == 8) {
											// Object_variable_info {
											//    u1 tag = ITEM_Object; /* 7 */
											//    u2 cpool_index;
											// }

											// Uninitialized_variable_info {
											//     u1 tag = ITEM_Uninitialized /* 8 */
											//     u2 offset;
											// }

											index +=2;
										}
									}
									else if (frameTag == 247) {
										// same_locals_1_stack_item_frame_extended {
										//     u1 frame_type = SAME_LOCALS_1_STACK_ITEM_EXTENDED; /* 247 */
										//     u2 offset_delta;
										//     verification_type_info stack[1];
										// }

										int offsetDelta = byteArrayRangeToInt(data, index, 2);
										index += 2;
										println("        offset delta: " + offsetDelta + "\n");

										int verificationTypeInfoTag = signBitAsValue(data[index]);
										index++;
										println("          verification type info tag: " + verificationTypeInfoTag + "\n");

										if (verificationTypeInfoTag == 7 || verificationTypeInfoTag == 8) {
											// Object_variable_info {
											//    u1 tag = ITEM_Object; /* 7 */
											//    u2 cpool_index;
											// }

											// Uninitialized_variable_info {
											//     u1 tag = ITEM_Uninitialized /* 8 */
											//     u2 offset;
											// }

											index +=2;
										}
									}
									else if (frameTag > 247 && frameTag <= 250) {
										// chop_frame {
										//     u1 frame_type = CHOP; /* 248-250 */
										//     u2 offset_delta;
										// }

										int offsetDelta = byteArrayRangeToInt(data, index, 2);
										index += 2;
										println("        offset delta: " + offsetDelta + "\n");
									}
									else if (frameTag == 251) {
										// same_frame_extended {
										//     u1 frame_type = SAME_FRAME_EXTENDED; /* 251 */
										//     u2 offset_delta;
										// }

										int offsetDelta = byteArrayRangeToInt(data, index, 2);
										index += 2;
										println("        offset delta: " + offsetDelta + "\n");
									}
									else if (frameTag > 251 && frameTag <= 254) {
										// append_frame {
										//     u1 frame_type = APPEND; /* 252-254 */
										//     u2 offset_delta;
										//     verification_type_info locals[frame_type - 251];
										// }

										int offsetDelta = byteArrayRangeToInt(data, index, 2);
										index += 2;
										println("        offset delta: " + offsetDelta + "\n");

										for (int m = 0; m < frameTag - 251; m++) {
											int verificationTypeInfoTag = signBitAsValue(data[index]);
											index++;
											println("          verification type info tag: " + verificationTypeInfoTag + "\n");

											if (verificationTypeInfoTag == 7 || verificationTypeInfoTag == 8) {
												// Object_variable_info {
												//    u1 tag = ITEM_Object; /* 7 */
												//    u2 cpool_index;
												// }

												// Uninitialized_variable_info {
												//     u1 tag = ITEM_Uninitialized /* 8 */
												//     u2 offset;
												// }
    
												index +=2;
											}

											println("          verification type info tag with index " + (m+1) + "\n");
										}
									}
									else if (frameTag == 255) {
										// full_frame {
										//     u1 frame_type = FULL_FRAME; /* 255 */
										//     u2 offset_delta;
										//     u2 number_of_locals;
										//     verification_type_info locals[number_of_locals];
										//     u2 number_of_stack_items;
										//     verification_type_info stack[number_of_stack_items];
										// }

										int offsetDelta = byteArrayRangeToInt(data, index, 2);
										index += 2;
										println("        offset delta: " + offsetDelta + "\n");

										int numberOfLocals = byteArrayRangeToInt(data, index, 2);
										index += 2;
										println("        number of locals: " + numberOfLocals + "\n");

										int numberOfStackFrames = byteArrayRangeToInt(data, index, 2);
										index += 2;
										println("        number of stack frames: " + numberOfStackFrames + "\n");
									}

									println("        frame[" + (l+1) + "]\n");
								}
							}

							println("      attribute[" + (k+1) + "]\n");
						}
					}
					else if ("Exceptions".equals(attributeName)) {
						// Exceptions_attribute {
						//     u2 attribute_name_index;
						//     u4 attribute_length;
						//     u2 number_of_exceptions;
						//     u2 exception_index_table[number_of_exceptions];
						// }

						println("    attribute length: " + byteArrayRangeToInt(data, index, 4) + "\n");
						index += 4;

						int numberOfExceptions = byteArrayRangeToInt(data, index, 2);
						index += 2;
						println("    number of exceptions: " + numberOfExceptions + "\n");


						for (int k = 0; k < numberOfExceptions; k++) {
							int exceptionIndex = byteArrayRangeToInt(data, index, 2);
							index += 2;
							println("      exception class index: " + exceptionIndex + "");
							String exceptionClass = arrayOfConstants[arrayOfInts[exceptionIndex]];
							println("      exception class value: " + exceptionClass + "\n");
						}
					}
					else if ("Synthetic".equals(attributeName)) {
						// Synthetic_attribute {
						//     u2 attribute_name_index;
						//     u4 attribute_length;
						// }

						println("    attribute length: " + byteArrayRangeToInt(data, index, 4) + "\n");
						index += 4;
					}
					else if ("Signature".equals(attributeName)) {
						// Signature_attribute {
						//     u2 attribute_name_index;
						//     u4 attribute_length;
						//     u2 signature_index;
						// }

						println("    attribute length: " + byteArrayRangeToInt(data, index, 4) + "\n");
						index += 4;

						int signatureIndex = byteArrayRangeToInt(data, index, 2);
						index += 2;
						println("    signature index: " + signatureIndex + "\n");
					}
					else if ("Deprecated".equals(attributeName)) {
						// Synthetic_attribute {
						// 	u2 attribute_name_index;
						// 	u4 attribute_length;
						// }

						//attribute_length    The value of the attribute_length item is zero.
						//it should be unsigned int to be precise
						println("    attribute length: " + byteArrayRangeToInt(data, index, 4) + "\n");
						index += 4;
					}
					else if ("RuntimeVisibleAnnotations".equals(attributeName) || "RuntimeInvisibleAnnotations".equals(attributeName)) {
						// RuntimeVisibleAnnotations_attribute {
						//     u2         attribute_name_index;
						//     u4         attribute_length;
						//     u2         num_annotations;
						//     annotation annotations[num_annotations];
						// }

						// RuntimeInvisibleAnnotations_attribute {
						//     u2         attribute_name_index;
						//     u4         attribute_length;
						//     u2         num_annotations;
						//     annotation annotations[num_annotations];
						// }

						println("    attribute length: " + byteArrayRangeToInt(data, index, 4) + "\n");
						index += 4;

						int numberOfAnnotations = byteArrayRangeToInt(data, index, 2);
						index += 2;
						println("    number of annotations: " + numberOfAnnotations + "\n");

						for (int k = 0; k < numberOfAnnotations; k++) {
							// annotation {
							//     u2 type_index;
							//     u2 num_element_value_pairs;
							//     {   u2            element_name_index;
							//         element_value value;
							//     } element_value_pairs[num_element_value_pairs];
							// }

							int typeIndex = byteArrayRangeToInt(data, index, 2);
							index += 2;
							println("      type index: " + typeIndex + "");
							String typeName = arrayOfConstants[typeIndex];
							println("      type name: " + typeName + "\n");

							int numberOfValuePairs = byteArrayRangeToInt(data, index, 2);
							index += 2;
							println("      number of value pairs: " + numberOfValuePairs + "\n");

							//WORK IN PROGRESS
							for (int l = 0; l < numberOfValuePairs; l++) {
								// element_value {
								//     u1 tag;
								//     union {
								//         u2 const_value_index;

								//         {   u2 type_name_index;
								//             u2 const_name_index;
								//         } enum_const_value;

								//         u2 class_info_index;

								//         annotation annotation_value;

								//         {   u2            num_values;
								//             element_value values[num_values];
								//         } array_value;
								//     } value;
								// }
								int elementNameIndex = byteArrayRangeToInt(data, index, 2);
								index += 2;
								println("        element name index: " + elementNameIndex);
								println("        element name value: " + arrayOfConstants[elementNameIndex] + "\n");
								char elementTag = (char) signBitAsValue(data[index]);
								index++;
								println("        element tag: " + elementTag + "\n");
								//The const_value_index item is used if the tag item is one of B, C, D, F, I, J, S, Z, or s. 
								String tagString = "BCDFIJSZs";
								boolean useConst = false;
								for(int m = 0; m < tagString.length(); m++) {
									if (elementTag == tagString.charAt(m)) {
										useConst = true;
										break;
									}
								}

								if (useConst) {
									int unionConstNameIndex = byteArrayRangeToInt(data, index, 2);
									println("        union constant index: " + unionConstNameIndex);
									println("        union constant value: " + arrayOfConstants[unionConstNameIndex] + "\n");
									index+=2;
								}
								else if ('[' == elementTag) {
									int numberOfArrayValues = byteArrayRangeToInt(data, index, 2);
									index += 2;
									println("        number of array values: " + numberOfArrayValues + "\n");

									for (int m = 0; m < numberOfArrayValues; m++) {
										char arrayElementTag = (char) signBitAsValue(data[index]);
										index++;
										println("          array element tag: " + arrayElementTag + "\n");


										boolean arrayUseConst = false;

										for(int n = 0; n < tagString.length(); n++) {
											if (arrayElementTag == tagString.charAt(n)) {
												arrayUseConst = true;
												break;
											}
										}

										if (arrayUseConst) {
											int unionConstNameIndex = byteArrayRangeToInt(data, index, 2);
											println("          union constant index: " + unionConstNameIndex);
											println("          union constant value: " + arrayOfConstants[unionConstNameIndex] + "\n");
											index+=2;
										}
										else if ('c' == arrayElementTag) {
											int classInfoIndex = byteArrayRangeToInt(data, index, 2);
											index += 2;
											println("          class info index: " + classInfoIndex + "\n");
											println("          class info value: " + arrayOfConstants[classInfoIndex] + "\n");
										}
										else {
											printlnError("WAAAT " + elementTag);
											System.exit(1);
										}

										println("          array element index is " + (m+1) + "\n");
									}
								}
								else if ('e' == elementTag) {
									int enumConstTypeNameIndex = byteArrayRangeToInt(data, index, 2);
									index += 2;
									println("        enum constant type index: " + enumConstTypeNameIndex + "\n");
									println("        enum constant type value: " + arrayOfConstants[enumConstTypeNameIndex] + "\n");

									int enumConstNameIndex = byteArrayRangeToInt(data, index, 2);
									index += 2;
									println("        enum constant name index: " + enumConstNameIndex + "\n");
									println("        enum constant name value: " + arrayOfConstants[enumConstNameIndex] + "\n");
								}
								else if ('c' == elementTag) {
									int classInfoIndex = byteArrayRangeToInt(data, index, 2);
									index += 2;
									println("        class info index: " + classInfoIndex + "\n");
									println("        class info value: " + arrayOfConstants[classInfoIndex] + "\n");
								}
								else {
									printlnError("WAAAT " + elementTag);
									System.exit(1);
								}

								println("        element value index is " + (l+1) + "\n");
							}

							println("      annotation index is " + (k+1) + "\n");
						}
					}
					else if ("RuntimeVisibleParameterAnnotations".equals(attributeName)
							|| "RuntimeInvisibleParameterAnnotations".equals(attributeName)) {
						// RuntimeVisibleParameterAnnotations_attribute {
						//     u2 attribute_name_index;
						//     u4 attribute_length;
						//     u1 num_parameters;
						//     {   u2         num_annotations;
						//         annotation annotations[num_annotations];
						//     } parameter_annotations[num_parameters];
						// }

						// RuntimeInvisibleParameterAnnotations_attribute {
						//     u2 attribute_name_index;
						//     u4 attribute_length;
						//     u1 num_parameters;
						//     {   u2         num_annotations;
						//         annotation annotations[num_annotations];
						//     } parameter_annotations[num_parameters];
						// }

						println("    attribute length: " + byteArrayRangeToInt(data, index, 4) + "\n");
						index += 4;

						int numberOfParameters = signBitAsValue(data[index]);
						index++;

						println("    number of parameters: " + numberOfParameters + "\n");

						for (int k = 0; k < numberOfParameters; k++) {
							printlnError("    no idea how to shou annotation stuff");
							System.exit(2);
						}
					}

					println("    attribute[" + (j+1) + "]\n");
				}

				println("  method[" + (i+1) + "]\n");
			}


			int numberOfAttributes = 0;

			if (data.length >= index+1) {
				numberOfAttributes = byteArrayRangeToInt(data, index, 2);
				println("Number of attributes: " + numberOfAttributes + "\n");

				index +=2;
			}
			else {
				printlnError("No number of attributes");
				System.exit(1);
			}

			for (int i = 0; i < numberOfAttributes; i++) {
				int attributeNameIndex = byteArrayRangeToInt(data, index, 2);
				index += 2;
				println("    attribute name index: " + attributeNameIndex + "");
				String attributeName = arrayOfConstants[attributeNameIndex];
				println("    attribute name value: " + attributeName + "\n");

				if ("InnerClasses".equals(attributeName)) {
					// InnerClasses_attribute {
					//     u2 attribute_name_index;
					//     u4 attribute_length;
					//     u2 number_of_classes;
					//     {   u2 inner_class_info_index;
					//         u2 outer_class_info_index;
					//         u2 inner_name_index;
					//         u2 inner_class_access_flags;
					//     } classes[number_of_classes];
					// }

					println("    attribute length: " + byteArrayRangeToInt(data, index, 4) + "\n");
					index += 4;

					int numberOfInnerClasses = byteArrayRangeToInt(data, index, 2);
					index += 2;
					println("    number of inner classes: " + numberOfInnerClasses + "\n");

					for (int j = 0; j < numberOfInnerClasses; j++) {
						//blabla
						index+=4;


						int innerClassNameIndex = byteArrayRangeToInt(data, index, 2);
						index += 2;
						println("      inner class name index: " + innerClassNameIndex);
						println("      inner class name value: " + arrayOfConstants[innerClassNameIndex] + "\n");

						//flags
						index += 2;

						println("      inner class index is " + (j+1) + "\n");
					}
				}
				else if ("EnclosingMethod".equals(attributeName)) {
					// EnclosingMethod_attribute {
					//     u2 attribute_name_index;
					//     u4 attribute_length;
					//     u2 class_index;
					//     u2 method_index;
					// }

					println("    attribute length: " + byteArrayRangeToInt(data, index, 4) + "\n");
					index += 4;

					int classIndex = byteArrayRangeToInt(data, index, 2);
					index += 2;
					println("    enclosing method class index: " + classIndex + "\n");
					String className = arrayOfConstants[arrayOfInts[classIndex]];
					println("    enclosing method class value: " + className + "\n");

					index+=2;
				}
				else if ("Synthetic".equals(attributeName)) {
					// Synthetic_attribute {
					//     u2 attribute_name_index;
					//     u4 attribute_length;
					// }

					println("    attribute length: " + byteArrayRangeToInt(data, index, 4) + "\n");
					index += 4;
				}
				else if ("Signature".equals(attributeName)) {
					// Signature_attribute {
					//     u2 attribute_name_index;
					//     u4 attribute_length;
					//     u2 signature_index;
					// }

					println("    attribute length: " + byteArrayRangeToInt(data, index, 4) + "\n");
					index += 4;

					int signatureIndex = byteArrayRangeToInt(data, index, 2);
					index += 2;
					println("    signature index: " + signatureIndex + "\n");
				}
				else if ("SourceFile".equals(attributeName)) {
					// SourceFile_attribute {
					//     u2 attribute_name_index;
					//     u4 attribute_length;
					//     u2 sourcefile_index;
					// }

					println("    attribute length: " + byteArrayRangeToInt(data, index, 4) + "\n");
					index += 4;

					int sourcefileIndex = byteArrayRangeToInt(data, index, 2);
					index += 2;
					println("    source file index: " + sourcefileIndex);
					String sourcefileName = arrayOfConstants[sourcefileIndex];
					println("    source file value: " + sourcefileName + "\n");
				}
				else if ("RuntimeVisibleAnnotations".equals(attributeName) || "RuntimeInvisibleAnnotations".equals(attributeName)) {
					// RuntimeVisibleAnnotations_attribute {
					//     u2         attribute_name_index;
					//     u4         attribute_length;
					//     u2         num_annotations;
					//     annotation annotations[num_annotations];
					// }

					// RuntimeInvisibleAnnotations_attribute {
					//     u2         attribute_name_index;
					//     u4         attribute_length;
					//     u2         num_annotations;
					//     annotation annotations[num_annotations];
					// }

					println("    attribute length: " + byteArrayRangeToInt(data, index, 4) + "\n");
					index += 4;

					int numberOfAnnotations = byteArrayRangeToInt(data, index, 2);
					index += 2;
					println("    number of annotations: " + numberOfAnnotations + "\n");

					for (int k = 0; k < numberOfAnnotations; k++) {
						// annotation {
						//     u2 type_index;
						//     u2 num_element_value_pairs;
						//     {   u2            element_name_index;
						//         element_value value;
						//     } element_value_pairs[num_element_value_pairs];
						// }

						int typeIndex = byteArrayRangeToInt(data, index, 2);
						index += 2;
						println("      type index: " + typeIndex + "");
						String typeName = arrayOfConstants[typeIndex];
						println("      type name: " + typeName + "\n");

						int numberOfValuePairs = byteArrayRangeToInt(data, index, 2);
						index += 2;
						println("      number of value pairs: " + numberOfValuePairs + "\n");

						//WORK IN PROGRESS
						for (int l = 0; l < numberOfValuePairs; l++) {
							// element_value {
							//     u1 tag;
							//     union {
							//         u2 const_value_index;

							//         {   u2 type_name_index;
							//             u2 const_name_index;
							//         } enum_const_value;

							//         u2 class_info_index;

							//         annotation annotation_value;

							//         {   u2            num_values;
							//             element_value values[num_values];
							//         } array_value;
							//     } value;
							// }
							int elementNameIndex = byteArrayRangeToInt(data, index, 2);
							index += 2;
							println("        element name index: " + elementNameIndex);
							println("        element name value: " + arrayOfConstants[elementNameIndex] + "\n");
							char elementTag = (char) signBitAsValue(data[index]);
							index++;
							println("        element tag: " + elementTag + "\n");
							//The const_value_index item is used if the tag item is one of B, C, D, F, I, J, S, Z, or s. 
							String tagString = "BCDFIJSZs";
							boolean useConst = false;
							for(int m = 0; m < tagString.length(); m++) {
								if (elementTag == tagString.charAt(m)) {
									useConst = true;
									break;
								}
							}

							if (useConst) {
								int unionConstNameIndex = byteArrayRangeToInt(data, index, 2);
								println("        union constant index: " + unionConstNameIndex);
								println("        union constant value: " + arrayOfConstants[unionConstNameIndex] + "\n");
								index+=2;
							}
							else if ('[' == elementTag) {
								int numberOfArrayValues = byteArrayRangeToInt(data, index, 2);
								index += 2;
								println("        number of array values: " + numberOfArrayValues + "\n");

								for (int m = 0; m < numberOfArrayValues; m++) {
									char arrayElementTag = (char) signBitAsValue(data[index]);
									index++;
									println("          array element tag: " + arrayElementTag + "\n");


									boolean arrayUseConst = false;

									for(int n = 0; n < tagString.length(); n++) {
										if (arrayElementTag == tagString.charAt(n)) {
											arrayUseConst = true;
											break;
										}
									}

									if (arrayUseConst) {
										int unionConstNameIndex = byteArrayRangeToInt(data, index, 2);
										println("          union constant index: " + unionConstNameIndex);
										println("          union constant value: " + arrayOfConstants[unionConstNameIndex] + "\n");
										index+=2;
									}
									else if ('c' == arrayElementTag) {
										int classInfoIndex = byteArrayRangeToInt(data, index, 2);
										index += 2;
										println("          class info index: " + classInfoIndex + "\n");
										println("          class info value: " + arrayOfConstants[classInfoIndex] + "\n");
									}
									else {
										printlnError("WAAAT " + elementTag);
										System.exit(1);
									}

									println("          array element index is " + (m+1) + "\n");
								}
							}
							else if ('e' == elementTag) {
								int enumConstTypeNameIndex = byteArrayRangeToInt(data, index, 2);
								index += 2;
								println("        enum constant type index: " + enumConstTypeNameIndex + "\n");
								println("        enum constant type value: " + arrayOfConstants[enumConstTypeNameIndex] + "\n");

								int enumConstNameIndex = byteArrayRangeToInt(data, index, 2);
								index += 2;
								println("        enum constant name index: " + enumConstNameIndex + "\n");
								println("        enum constant name value: " + arrayOfConstants[enumConstNameIndex] + "\n");
							}
							else if ('c' == elementTag) {
								int classInfoIndex = byteArrayRangeToInt(data, index, 2);
								index += 2;
								println("        class info index: " + classInfoIndex + "\n");
								println("        class info value: " + arrayOfConstants[classInfoIndex] + "\n");
							}
							else {
								printlnError("WAAAT " + elementTag);
								System.exit(1);
							}

							println("        element value index is " + (l+1) + "\n");
						}

						println("      annotation index is " + (k+1) + "\n");
					}
				}
				else if ("SourceDebugExtension".equals(attributeName)) {
					// SourceDebugExtension_attribute {
					//     u2 attribute_name_index;
					//     u4 attribute_length;
					//     u1 debug_extension[attribute_length];
					// }

					int attributeLength = byteArrayRangeToInt(data, index, 4);
					println("    attribute length: " + attributeLength + "\n");
					index+=4;

					index += attributeLength;
				}
				else if ("SourceDebugExtension".equals(attributeName)) {
					// SourceDebugExtension_attribute {
					//     u2 attribute_name_index;
					//     u4 attribute_length;
					//     u1 debug_extension[attribute_length];
					// }

					int attributeLength = byteArrayRangeToInt(data, index, 4);
					println("    attribute length: " + attributeLength + "\n");
					index+=4;

					index += attributeLength;
				}
				else if ("BootstrapMethods".equals(attributeName)) {
					// BootstrapMethods_attribute {
					//     u2 attribute_name_index;
					//     u4 attribute_length;
					//     u2 num_bootstrap_methods;
					//     {   u2 bootstrap_method_ref;
					//         u2 num_bootstrap_arguments;
					//         u2 bootstrap_arguments[num_bootstrap_arguments];
					//     } bootstrap_methods[num_bootstrap_methods];
					// }

					int attributeLength = byteArrayRangeToInt(data, index, 4);
					println("    attribute length: " + attributeLength + "\n");
					index+=4;

					int numberOfBootstrapMethods = byteArrayRangeToInt(data, index, 2);
					println("    number of bootstrap methods: " + numberOfBootstrapMethods + "\n");
					index += 2;

					for (int j = 0; j < numberOfBootstrapMethods; j++) {
						int methodRef = byteArrayRangeToInt(data, index, 2);
						index += 2;
						println("    bootstrap method reference is " + methodRef + "\n");

						int numberOfBootstrapArguments = byteArrayRangeToInt(data, index, 2);
						index += 2;
						println("    number of arguments " + numberOfBootstrapArguments + "\n");

						for (int k = 0; k < numberOfBootstrapArguments; k++) {
							int bootstrapArgumentConstantIndex = byteArrayRangeToInt(data, index, 2);
							println("      argument constant index " + bootstrapArgumentConstantIndex + "\n");
							index += 2;
						}

						println("    bootstrap method index is " + (j+1) + "\n");
					}
				}

				println("    attribute index is " + (i+1) + "\n");
			}


			HackMap indexCheckResult = checkIndexReachedArrayLength(data, index);

			evaluateResult(indexCheckResult, 2);
		}
	}

}
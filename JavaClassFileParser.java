import java.io.File;
import java.io.FileInputStream;

import java.util.Arrays;

public class JavaClassFileParser {

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

		for (int i = 0; i < 4; i++) {
			// System.out.println(String.format("%02x", b[i]));
			result = (result << 8) + (b[i] & 0xff);
		}

		return result;
	}

	public static int twoUnsignedBytesToInt(byte byte1, byte byte2) {
		return signBitAsValue(byte1)*256 + signBitAsValue(byte2);
	}


	/*
	 *  If any questions appear. Please read official specification of java class file structure
	 *  https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html
	 */
	public static void main(String[] args) {
		if (args.length == 0)
			System.out.println("Please specify class-file for analysis");
		else {
			System.out.println("File \""+args[0] + "\" will be analyzed\n");

			byte[] data = readContentIntoByteArray(new File(args[0]));

			if (data.length > 0)
				System.out.println("File length: " + data.length + " bytes\n");
			else {
				System.out.println("Failure reading file or it is empty");
				System.exit(1);
			}

			//http://web.cecs.pdx.edu/~apt/vmspec/ClassFile.doc.html

			if (data.length >= 4
				&& unsignedByteCompare(data[0], 0xCA)
				&& unsignedByteCompare(data[1], 0xFE)
				&& unsignedByteCompare(data[2], 0xBA)
				&& unsignedByteCompare(data[3], 0xBE))
				System.out.println("Magic part is correct\n");
			else {
				System.out.println("First bytes do not contain correct values");
				System.exit(1);
			}


			if (data.length >= 8) {
				String failedToGetMajorVersion = "not identified";
				String majorVersionJava = failedToGetMajorVersion;

				int majorVersionNumber = signBitAsValue(data[7]);

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

				if (majorVersionJava.equals(failedToGetMajorVersion)) {
					System.out.println("Can not identify version of java compiler");
					System.exit(1);
				}

				System.out.println("Major version: " + majorVersionJava + "\n");
			}
			else {
				System.out.println("Can not identify version of java compiler");
				System.exit(1);
			}

			int numberOfConstants = -1;

			if (data.length >= 10) {
				numberOfConstants = twoUnsignedBytesToInt(data[8], data[9]);

				System.out.println("Number of constants: " + numberOfConstants + "\n");
			}
			else {
				System.out.println("Can not identify number of constants");
				System.exit(1);
			}


			int index = 10;

			String[] arrayOfConstants = new String[numberOfConstants+1];
			int[] arrayOfInts = new int[numberOfConstants+1];

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

				while(i < numberOfConstants && data.length >= index+1) {
					int constantTag = signBitAsValue(data[index]);

					String type = "";

					if (1 == constantTag) {
						type = "Utf8";
						// CONSTANT_Utf8_info {
						// 	u1 tag;
						// 	u2 length;
						// 	u1 bytes[length];
						// }

						int length = twoUnsignedBytesToInt(data[index+1], data[index+2]);
						System.out.println(length);
						byte[] utf8Data = Arrays.copyOfRange(data, index+3, index+3+length);
						System.out.println(new String(utf8Data));

						arrayOfConstants[i] = new String(utf8Data);

						index += 3+length;
					}
					else if (3 == constantTag) {
						type = "Integer";
						// CONSTANT_Integer_info {
						// 	u1 tag;
						// 	u4 bytes;
						// }
						System.out.println(byteArrayToInt(Arrays.copyOfRange(data, index+1, index+1+4)));
						index += 5;
					}
					else if (4 == constantTag) {
						type = "Float";
						// CONSTANT_Float_info {
						// 	u1 tag;
						// 	u4 bytes;
						// }

						index += 5;
					}
					else if (5 == constantTag) {
						type = "Long";
						// CONSTANT_Long_info {
						// 	u1 tag;
						// 	u4 high_bytes;
						// 	u4 low_bytes;
						// }

						index += 9;
					}
					else if (6 == constantTag) {
						type = "Double";
						// CONSTANT_Double_info {
						// 	u1 tag;
						// 	u4 high_bytes;
						// 	u4 low_bytes;
						// }

						index += 9;
					}
					else if (7 == constantTag) {
						type = "Class";
						// CONSTANT_Class_info {
						// 	u1 tag;
						// 	u2 name_index;
						// }
						int nameIndex = twoUnsignedBytesToInt(data[index+1], data[index+2]);
						arrayOfInts[i] = nameIndex;
						System.out.println(nameIndex);

						index += 3;
					}
					else if (8 == constantTag) {
						type = "String";
						// CONSTANT_String_info {
						// 	u1 tag;
						// 	u2 string_index;
						// }

						System.out.println(twoUnsignedBytesToInt(data[index+1], data[index+2]));

						index += 3;
					}
					else if (9 == constantTag) {
						type = "Field Reference";
						// CONSTANT_Fieldref_info {
						// 	u1 tag;
						// 	u2 class_index;
						// 	u2 name_and_type_index;
						// }

						System.out.println(twoUnsignedBytesToInt(data[index+1], data[index+2]));
						System.out.println(signBitAsValue(data[index+3])*256+signBitAsValue(data[index+4]));

						index += 5;
					}
					else if (10 == constantTag) {
						type = "Method Reference";
						// CONSTANT_Methodref_info {
						// 	u1 tag;
						// 	u2 class_index;
						// 	u2 name_and_type_index;
						// }

						System.out.println(twoUnsignedBytesToInt(data[index+1], data[index+2]));
						System.out.println(signBitAsValue(data[index+3])*256+signBitAsValue(data[index+4]));

						index+=5;
					}
					else if (11 == constantTag) {
						type = "Interface Method Reference";
						// CONSTANT_InterfaceMethodref_info {
						// 	u1 tag;
						// 	u2 class_index;
						// 	u2 name_and_type_index;
						// }

						System.out.println(twoUnsignedBytesToInt(data[index+1], data[index+2]));
						System.out.println(signBitAsValue(data[index+3])*256+signBitAsValue(data[index+4]));

						index+=5;
					}
					else if (12 == constantTag) {
						type = "Name and Type";
						// CONSTANT_NameAndType_info {
						// 	u1 tag;
						// 	u2 name_index;
						// 	u2 descriptor_index;
						// }

						System.out.println(twoUnsignedBytesToInt(data[index+1], data[index+2]));
						System.out.println(signBitAsValue(data[index+3])*256+signBitAsValue(data[index+4]));

						index+=5;
					}
					else if (15 == constantTag){
						type = "Method Handle";
						// CONSTANT_MethodHandle_info {
						//     u1 tag;
						//     u1 reference_kind;
						//     u2 reference_index;
						// }

						index += 4;
					}
					else if (16 == constantTag){
						type = "Method Type";
						// CONSTANT_MethodType_info {
						//     u1 tag;
						//     u2 descriptor_index;
						// }

						index += 3;
					}
					else if (18 == constantTag) {
						type = "Invoke Dynamic";
						// CONSTANT_InvokeDynamic_info {
						//     u1 tag;
						//     u2 bootstrap_method_attr_index;
						//     u2 name_and_type_index;
						// }

						index += 5;
					}


					if ("".equals(type)) {
						System.out.println("Failed to get tag for constant["+i+"]");
						System.exit(1);
					}


					System.out.println("  Constant["+i+"] is " + type + "\n");


					i++;
				}
			}
			else {
				System.out.println("Wrong number of constants");
				System.exit(1);
			}

			if (data.length >= index+1) {
				int value = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
				System.out.println("Access flags bitmask: " + String.format("%02x", data[index]) + String.format("%02x", data[index+1]) + "\n");
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
				System.out.println("No data for access flags!");
				System.exit(1);
			}


			if (data.length >= index+1) {
				int value = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
				System.out.println("This class index: " + value + "\n");

				index +=2;
			}
			else {
				System.out.println("No this class index!");
				System.exit(1);
			}

			if (data.length >= index+1) {
				int value = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
				System.out.println("Super class index: " + value + "\n");

				index +=2;
			}
			else {
				System.out.println("No super class index!");
				System.exit(1);
			}

			int numberOfInterfaces = 0;
			if (data.length >= index+1) {
				numberOfInterfaces = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
				System.out.println("Number of interfaces: " + numberOfInterfaces + "\n");

				index +=2;
			}
			else {
				System.out.println("No number of interfaces!");
				System.exit(1);
			}


			for (int i = 0; i < numberOfInterfaces; i++) {
				int interfaceIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);

				System.out.println("  interface[" + (i+1) + "] is " + interfaceIndex + "\n");

				index += 2;
			}


			int numberOfFields = 0;

			if (data.length >= index+1) {
				numberOfFields = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
				System.out.println("Number of fields: " + numberOfFields + "\n");

				index +=2;
			}
			else {
				System.out.println("No number of fields");
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

				System.out.println("  access flags bitmask: " + String.format("%02x", data[index]) + String.format("%02x", data[index+1]) + "\n");
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


				int nameIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
				index +=2;
				System.out.println("  name index: " + nameIndex + "");
				System.out.println("  name value: " + arrayOfConstants[nameIndex] + "\n");

				int descriptorIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
				index +=2;
				System.out.println("  descriptor index: " + descriptorIndex + "\n");

				int numberOfAttributes = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
				index +=2;
				System.out.println("  number of attributes: " + numberOfAttributes + "\n");

				for (int j = 0; j < numberOfAttributes; j++) {
					int attributeNameIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
					index += 2;
					System.out.println("    attribute name index: " + attributeNameIndex + "");
					String attributeName = arrayOfConstants[attributeNameIndex];
					System.out.println("    attribute name value: " + attributeName + "\n");

					if ("ConstantValue".equals(attributeName)) {
						// ConstantValue_attribute {
						// 	u2 attribute_name_index;
						// 	u4 attribute_length;
						// 	u2 constantvalue_index;
						// }

						//attribute_length    The value of the attribute_length item of a ConstantValue_attribute structure must be 2.
						//it should be unsigned int to be precise
						System.out.println("    attribute length: " + byteArrayToInt(Arrays.copyOfRange(data, index, index+4)) + "\n");
						index+=4;

						int constantValueIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
						index += 2;
						System.out.println("    constant value index: " + constantValueIndex + "\n");
					}
					else if ("Synthetic".equals(attributeName)) {
						// Synthetic_attribute {
						// 	u2 attribute_name_index;
						// 	u4 attribute_length;
						// }

						//attribute_length    The value of the attribute_length item is zero.
						//it should be unsigned int to be precise
						System.out.println("    attribute length: " + byteArrayToInt(Arrays.copyOfRange(data, index, index+4)) + "\n");
						index += 4;
					}
					else if ("Signature".equals(attributeName)) {
						// Signature_attribute {
						//     u2 attribute_name_index;
						//     u4 attribute_length;
						//     u2 signature_index;
						// }

						System.out.println("    attribute length: " + byteArrayToInt(Arrays.copyOfRange(data, index, index+4)) + "\n");
						index += 4;

						int signatureIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
						index += 2;
						System.out.println("    signature index: " + signatureIndex + "\n");
					}
					else if ("Deprecated".equals(attributeName)) {
						// Synthetic_attribute {
						// 	u2 attribute_name_index;
						// 	u4 attribute_length;
						// }

						//attribute_length    The value of the attribute_length item is zero.
						//it should be unsigned int to be precise
						System.out.println("    attribute length: " + byteArrayToInt(Arrays.copyOfRange(data, index, index+4)) + "\n");
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

						System.out.println("    attribute length: " + byteArrayToInt(Arrays.copyOfRange(data, index, index+4)) + "\n");
						index += 4;

						int numberOfAnnotations = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
						index += 2;
						System.out.println("    number of annotations: " + numberOfAnnotations + "\n");

						for (int k = 0; k < numberOfAnnotations; k++) {
							// annotation {
							//     u2 type_index;
							//     u2 num_element_value_pairs;
							//     {   u2            element_name_index;
							//         element_value value;
							//     } element_value_pairs[num_element_value_pairs];
							// }

							int typeIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
							index += 2;
							System.out.println("      type index: " + typeIndex + "");
							String typeName = arrayOfConstants[typeIndex];
							System.out.println("      type name: " + typeName + "\n");

							int numberOfValuePairs = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
							index += 2;
							System.out.println("      number of value pairs: " + numberOfValuePairs + "\n");

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
								int elementNameIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
								index += 2;
								System.out.println("        element name index: " + elementNameIndex + "\n");
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
									int unionConstNameIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
									System.out.println("        union constant index: " + unionConstNameIndex);
									System.out.println("        union constant value: " + arrayOfConstants[unionConstNameIndex] + "\n");
									index+=2;
								}
								else {
									//WORK IN PROGRESS
									System.out.println("WAAAT " + elementTag);
									System.exit(1);
								}
							}
						}
					}

					System.out.println("    attribute[" + (j+1) + "]\n");
				}


				System.out.println("  field[" + (i+1) + "]\n");
			}


			int numberOfMethods = 0;

			if (data.length >= index+1) {
				numberOfMethods = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
				System.out.println("Number of methods: " + numberOfMethods + "\n");

				index +=2;
			}
			else {
				System.out.println("No number of methods");
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

				System.out.println("  access flags bitmask: " + String.format("%02x", data[index]) + String.format("%02x", data[index+1]) + "\n");
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


				int nameIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
				index +=2;
				System.out.println("  name index: " + nameIndex + "");
				System.out.println("  name value: " + arrayOfConstants[nameIndex] + "\n");

				int descriptorIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
				index +=2;
				System.out.println("  descriptor index: " + descriptorIndex + "\n");

				int numberOfAttributes = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
				index +=2;
				System.out.println("  number of attributes: " + numberOfAttributes + "\n");

				for (int j = 0; j < numberOfAttributes; j++) {
					int attributeNameIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
					index += 2;
					System.out.println("    attribute name index: " + attributeNameIndex + "");
					String attributeName = arrayOfConstants[attributeNameIndex];
					System.out.println("    attribute name value: " + attributeName + "\n");

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
						System.out.println("    attribute length: " + byteArrayToInt(Arrays.copyOfRange(data, index, index+4)) + "\n");
						index+=4;

						int maxStack = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
						index += 2;
						System.out.println("    max stack: " + maxStack + "\n");

						int maxLocals = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
						index += 2;
						System.out.println("    max locals: " + maxLocals + "\n");

						int lengthOfCode = byteArrayToInt(Arrays.copyOfRange(data, index, index+4));
						System.out.println("    code length: " + lengthOfCode + "\n");
						index += 4;

						System.out.println("    No idea how to show bytecode now\n");
						index += lengthOfCode;

						int lengthOfExceptionTable = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
						index += 2;
						System.out.println("    length of exception table: " + lengthOfExceptionTable + "\n");

						for (int k = 0; k < lengthOfExceptionTable; k++) {
							// {   u2 start_pc;
							//     u2 end_pc;
							//     u2 handler_pc;
							//     u2 catch_type;
							// }
							System.out.println("      8 boring bytes about exception handlers\n");
							index += 8;
						}

						int numberOfCodeAttributes = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
						index += 2;
						System.out.println("    number of attributes: " + numberOfCodeAttributes + "\n");

						for (int k = 0; k < numberOfCodeAttributes; k++) {
							int codeAttributeNameIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
							index += 2;
							System.out.println("      attribute name index: " + codeAttributeNameIndex + "");
							String codeAttributeName = arrayOfConstants[codeAttributeNameIndex];
							System.out.println("      attribute name value: " + codeAttributeName + "\n");

							if ("LineNumberTable".equals(codeAttributeName)) {
								// LineNumberTable_attribute {
								//     u2 attribute_name_index;
								//     u4 attribute_length;
								//     u2 line_number_table_length;
								//     {   u2 start_pc;
								//         u2 line_number;	
								//     } line_number_table[line_number_table_length];
								// }

								System.out.println("      attribute length: " + byteArrayToInt(Arrays.copyOfRange(data, index, index+4)) + "\n");
								index += 4;

								int lengthOfLineNumberTable = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
								index += 2;
								System.out.println("      length of line number table: " + lengthOfLineNumberTable + "\n");

								System.out.println("      no idea how to show line number table\n");
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

								System.out.println("      attribute length: " + byteArrayToInt(Arrays.copyOfRange(data, index, index+4)) + "\n");
								index += 4;

								int lengthOfLocalVariableTable = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
								index += 2;
								System.out.println("      length of local variable table: " + lengthOfLocalVariableTable + "\n");

								System.out.println("      no idea how to show local variable table\n");
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

								System.out.println("      attribute length: " + byteArrayToInt(Arrays.copyOfRange(data, index, index+4)) + "\n");
								index += 4;

								int lengthOfLocalVariableTypeTable = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
								index += 2;
								System.out.println("      length of local variable type table: " + lengthOfLocalVariableTypeTable + "\n");

								System.out.println("      no idea how to show local variable type table\n");
								index += lengthOfLocalVariableTypeTable*10;
							}
							else if ("StackMapTable".equals(codeAttributeName)) {
								// StackMapTable_attribute {
								//     u2              attribute_name_index;
								//     u4              attribute_length;
								//     u2              number_of_entries;
								//     stack_map_frame entries[number_of_entries];
								// }

								System.out.println("      attribute length: " + byteArrayToInt(Arrays.copyOfRange(data, index, index+4)) + "\n");
								index += 4;

								int numberOfEntries = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
								index += 2;
								System.out.println("      number of entries: " + numberOfEntries + "\n");

								for (int l = 0; l < numberOfEntries; l++) {
									int frameTag = signBitAsValue(data[index]);
									index++;
									System.out.println("        frame tag: " + frameTag + "\n");

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
										System.out.println("          verification type info tag: " + verificationTypeInfoTag + "\n");

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

										int offsetDelta = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
										index += 2;
										System.out.println("        offset delta: " + offsetDelta + "\n");

										int verificationTypeInfoTag = signBitAsValue(data[index]);
										index++;
										System.out.println("          verification type info tag: " + verificationTypeInfoTag + "\n");

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

										int offsetDelta = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
										index += 2;
										System.out.println("        offset delta: " + offsetDelta + "\n");
									}
									else if (frameTag == 251) {
										// same_frame_extended {
										//     u1 frame_type = SAME_FRAME_EXTENDED; /* 251 */
										//     u2 offset_delta;
										// }

										int offsetDelta = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
										index += 2;
										System.out.println("        offset delta: " + offsetDelta + "\n");
									}
									else if (frameTag > 251 && frameTag <= 254) {
										// append_frame {
										//     u1 frame_type = APPEND; /* 252-254 */
										//     u2 offset_delta;
										//     verification_type_info locals[frame_type - 251];
										// }

										int offsetDelta = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
										index += 2;
										System.out.println("        offset delta: " + offsetDelta + "\n");

										for (int m = 0; m < frameTag - 251; m++) {
											int verificationTypeInfoTag = signBitAsValue(data[index]);
											index++;
											System.out.println("          verification type info tag: " + verificationTypeInfoTag + "\n");

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

											System.out.println("          verification type info tag with index " + (m+1) + "\n");
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

										int offsetDelta = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
										index += 2;
										System.out.println("        offset delta: " + offsetDelta + "\n");

										int numberOfLocals = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
										index += 2;
										System.out.println("        number of locals: " + numberOfLocals + "\n");

										int numberOfStackFrames = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
										index += 2;
										System.out.println("        number of stack frames: " + numberOfStackFrames + "\n");
									}

									System.out.println("        frame[" + (l+1) + "]\n");
								}
							}

							System.out.println("      attribute[" + (k+1) + "]\n");
						}
					}
					else if ("Exceptions".equals(attributeName)) {
						// Exceptions_attribute {
						//     u2 attribute_name_index;
						//     u4 attribute_length;
						//     u2 number_of_exceptions;
						//     u2 exception_index_table[number_of_exceptions];
						// }

						System.out.println("    attribute length: " + byteArrayToInt(Arrays.copyOfRange(data, index, index+4)) + "\n");
						index += 4;

						int numberOfExceptions = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
						index += 2;
						System.out.println("    number of exceptions: " + numberOfExceptions + "\n");


						for (int k = 0; k < numberOfExceptions; k++) {
							int exceptionIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
							index += 2;
							System.out.println("      exception class index: " + exceptionIndex + "");
							String exceptionClass = arrayOfConstants[arrayOfInts[exceptionIndex]];
							System.out.println("      exception class value: " + exceptionClass + "\n");
						}
					}
					else if ("Synthetic".equals(attributeName)) {
						// Synthetic_attribute {
						//     u2 attribute_name_index;
						//     u4 attribute_length;
						// }

						System.out.println("    attribute length: " + byteArrayToInt(Arrays.copyOfRange(data, index, index+4)) + "\n");
						index += 4;
					}
					else if ("Signature".equals(attributeName)) {
						// Signature_attribute {
						//     u2 attribute_name_index;
						//     u4 attribute_length;
						//     u2 signature_index;
						// }

						System.out.println("    attribute length: " + byteArrayToInt(Arrays.copyOfRange(data, index, index+4)) + "\n");
						index += 4;

						int signatureIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
						index += 2;
						System.out.println("    signature index: " + signatureIndex + "\n");
					}
					else if ("Deprecated".equals(attributeName)) {
						// Synthetic_attribute {
						// 	u2 attribute_name_index;
						// 	u4 attribute_length;
						// }

						//attribute_length    The value of the attribute_length item is zero.
						//it should be unsigned int to be precise
						System.out.println("    attribute length: " + byteArrayToInt(Arrays.copyOfRange(data, index, index+4)) + "\n");
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

						System.out.println("    attribute length: " + byteArrayToInt(Arrays.copyOfRange(data, index, index+4)) + "\n");
						index += 4;

						int numberOfAnnotations = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
						index += 2;
						System.out.println("    number of annotations: " + numberOfAnnotations + "\n");

						for (int k = 0; k < numberOfAnnotations; k++) {
							// annotation {
							//     u2 type_index;
							//     u2 num_element_value_pairs;
							//     {   u2            element_name_index;
							//         element_value value;
							//     } element_value_pairs[num_element_value_pairs];
							// }

							int typeIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
							index += 2;
							System.out.println("      type index: " + typeIndex + "");
							String typeName = arrayOfConstants[typeIndex];
							System.out.println("      type name: " + typeName + "\n");

							int numberOfValuePairs = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
							index += 2;
							System.out.println("      number of value pairs: " + numberOfValuePairs + "\n");

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
								int elementNameIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
								index += 2;
								System.out.println("        element name index: " + elementNameIndex);
								System.out.println("        element name value: " + arrayOfConstants[elementNameIndex] + "\n");
								char elementTag = (char) signBitAsValue(data[index]);
								index++;
								System.out.println("        element tag: " + elementTag + "\n");
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
									int unionConstNameIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
									System.out.println("        union constant index: " + unionConstNameIndex);
									System.out.println("        union constant value: " + arrayOfConstants[unionConstNameIndex] + "\n");
									index+=2;
								}
								else if ('[' == elementTag) {
									int numberOfArrayValues = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
									index += 2;
									System.out.println("        number of array values: " + numberOfArrayValues + "\n");

									for (int m = 0; m < numberOfArrayValues; m++) {
										char arrayElementTag = (char) signBitAsValue(data[index]);
										index++;
										System.out.println("          array element tag: " + arrayElementTag + "\n");


										boolean arrayUseConst = false;

										for(int n = 0; n < tagString.length(); n++) {
											if (arrayElementTag == tagString.charAt(n)) {
												arrayUseConst = true;
												break;
											}
										}

										if (arrayUseConst) {
											int unionConstNameIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
											System.out.println("          union constant index: " + unionConstNameIndex);
											System.out.println("          union constant value: " + arrayOfConstants[unionConstNameIndex] + "\n");
											index+=2;
										}
										else if ('c' == arrayElementTag) {
											int classInfoIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
											index += 2;
											System.out.println("          class info index: " + classInfoIndex + "\n");
											System.out.println("          class info value: " + arrayOfConstants[classInfoIndex] + "\n");
										}
										else {
											System.out.println("WAAAT " + elementTag);
											System.exit(1);
										}

										System.out.println("          array element index is " + (m+1) + "\n");
									}
								}
								else if ('e' == elementTag) {
									int enumConstTypeNameIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
									index += 2;
									System.out.println("        enum constant type index: " + enumConstTypeNameIndex + "\n");
									System.out.println("        enum constant type value: " + arrayOfConstants[enumConstTypeNameIndex] + "\n");

									int enumConstNameIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
									index += 2;
									System.out.println("        enum constant name index: " + enumConstNameIndex + "\n");
									System.out.println("        enum constant name value: " + arrayOfConstants[enumConstNameIndex] + "\n");
								}
								else if ('c' == elementTag) {
									int classInfoIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
									index += 2;
									System.out.println("        class info index: " + classInfoIndex + "\n");
									System.out.println("        class info value: " + arrayOfConstants[classInfoIndex] + "\n");
								}
								else {
									System.out.println("WAAAT " + elementTag);
									System.exit(1);
								}

								System.out.println("        element value index is " + (l+1) + "\n");
							}

							System.out.println("      annotation index is " + (k+1) + "\n");
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

						System.out.println("    attribute length: " + byteArrayToInt(Arrays.copyOfRange(data, index, index+4)) + "\n");
						index += 4;

						int numberOfParameters = signBitAsValue(data[index]);
						index++;

						System.out.println("    number of parameters: " + numberOfParameters + "\n");

						for (int k = 0; k < numberOfParameters; k++) {
							System.out.println("    no idea how to shou annotation stuff");
							System.exit(2);
						}
					}

					System.out.println("    attribute[" + (j+1) + "]\n");
				}

				System.out.println("  method[" + (i+1) + "]\n");
			}


			int numberOfAttributes = 0;

			if (data.length >= index+1) {
				numberOfAttributes = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
				System.out.println("Number of attributes: " + numberOfAttributes + "\n");

				index +=2;
			}
			else {
				System.out.println("No number of attributes");
				System.exit(1);
			}

			for (int i = 0; i < numberOfAttributes; i++) {
				int attributeNameIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
				index += 2;
				System.out.println("    attribute name index: " + attributeNameIndex + "");
				String attributeName = arrayOfConstants[attributeNameIndex];
				System.out.println("    attribute name value: " + attributeName + "\n");

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

					System.out.println("    attribute length: " + byteArrayToInt(Arrays.copyOfRange(data, index, index+4)) + "\n");
					index += 4;

					int numberOfInnerClasses = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
					index += 2;
					System.out.println("    number of inner classes: " + numberOfInnerClasses + "\n");

					for (int j = 0; j < numberOfInnerClasses; j++) {
						//blabla
						index+=4;


						int innerClassNameIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
						index += 2;
						System.out.println("      inner class name index: " + innerClassNameIndex);
						System.out.println("      inner class name value: " + arrayOfConstants[innerClassNameIndex] + "\n");

						//flags
						index += 2;

						System.out.println("      inner class index is " + (j+1) + "\n");
					}
				}
				else if ("EnclosingMethod".equals(attributeName)) {
					// EnclosingMethod_attribute {
					//     u2 attribute_name_index;
					//     u4 attribute_length;
					//     u2 class_index;
					//     u2 method_index;
					// }

					System.out.println("    attribute length: " + byteArrayToInt(Arrays.copyOfRange(data, index, index+4)) + "\n");
					index += 4;

					int classIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
					index += 2;
					System.out.println("    enclosing method class index: " + classIndex + "\n");
					String className = arrayOfConstants[arrayOfInts[classIndex]];
					System.out.println("    enclosing method class value: " + className + "\n");

					index+=2;
				}
				else if ("Synthetic".equals(attributeName)) {
					// Synthetic_attribute {
					//     u2 attribute_name_index;
					//     u4 attribute_length;
					// }

					System.out.println("    attribute length: " + byteArrayToInt(Arrays.copyOfRange(data, index, index+4)) + "\n");
					index += 4;
				}
				else if ("Signature".equals(attributeName)) {
					// Signature_attribute {
					//     u2 attribute_name_index;
					//     u4 attribute_length;
					//     u2 signature_index;
					// }

					System.out.println("    attribute length: " + byteArrayToInt(Arrays.copyOfRange(data, index, index+4)) + "\n");
					index += 4;

					int signatureIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
					index += 2;
					System.out.println("    signature index: " + signatureIndex + "\n");
				}
				else if ("SourceFile".equals(attributeName)) {
					// SourceFile_attribute {
					//     u2 attribute_name_index;
					//     u4 attribute_length;
					//     u2 sourcefile_index;
					// }

					System.out.println("    attribute length: " + byteArrayToInt(Arrays.copyOfRange(data, index, index+4)) + "\n");
					index += 4;

					int sourcefileIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
					index += 2;
					System.out.println("    source file index: " + sourcefileIndex);
					String sourcefileName = arrayOfConstants[sourcefileIndex];
					System.out.println("    source file value: " + sourcefileName + "\n");
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

					System.out.println("    attribute length: " + byteArrayToInt(Arrays.copyOfRange(data, index, index+4)) + "\n");
					index += 4;

					int numberOfAnnotations = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
					index += 2;
					System.out.println("    number of annotations: " + numberOfAnnotations + "\n");

					for (int k = 0; k < numberOfAnnotations; k++) {
						// annotation {
						//     u2 type_index;
						//     u2 num_element_value_pairs;
						//     {   u2            element_name_index;
						//         element_value value;
						//     } element_value_pairs[num_element_value_pairs];
						// }

						int typeIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
						index += 2;
						System.out.println("      type index: " + typeIndex + "");
						String typeName = arrayOfConstants[typeIndex];
						System.out.println("      type name: " + typeName + "\n");

						int numberOfValuePairs = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
						index += 2;
						System.out.println("      number of value pairs: " + numberOfValuePairs + "\n");

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
							int elementNameIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
							index += 2;
							System.out.println("        element name index: " + elementNameIndex);
							System.out.println("        element name value: " + arrayOfConstants[elementNameIndex] + "\n");
							char elementTag = (char) signBitAsValue(data[index]);
							index++;
							System.out.println("        element tag: " + elementTag + "\n");
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
								int unionConstNameIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
								System.out.println("        union constant index: " + unionConstNameIndex);
								System.out.println("        union constant value: " + arrayOfConstants[unionConstNameIndex] + "\n");
								index+=2;
							}
							else if ('[' == elementTag) {
								int numberOfArrayValues = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
								index += 2;
								System.out.println("        number of array values: " + numberOfArrayValues + "\n");

								for (int m = 0; m < numberOfArrayValues; m++) {
									char arrayElementTag = (char) signBitAsValue(data[index]);
									index++;
									System.out.println("          array element tag: " + arrayElementTag + "\n");


									boolean arrayUseConst = false;

									for(int n = 0; n < tagString.length(); n++) {
										if (arrayElementTag == tagString.charAt(n)) {
											arrayUseConst = true;
											break;
										}
									}

									if (arrayUseConst) {
										int unionConstNameIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
										System.out.println("          union constant index: " + unionConstNameIndex);
										System.out.println("          union constant value: " + arrayOfConstants[unionConstNameIndex] + "\n");
										index+=2;
									}
									else if ('c' == arrayElementTag) {
										int classInfoIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
										index += 2;
										System.out.println("          class info index: " + classInfoIndex + "\n");
										System.out.println("          class info value: " + arrayOfConstants[classInfoIndex] + "\n");
									}
									else {
										System.out.println("WAAAT " + elementTag);
										System.exit(1);
									}

									System.out.println("          array element index is " + (m+1) + "\n");
								}
							}
							else if ('e' == elementTag) {
								int enumConstTypeNameIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
								index += 2;
								System.out.println("        enum constant type index: " + enumConstTypeNameIndex + "\n");
								System.out.println("        enum constant type value: " + arrayOfConstants[enumConstTypeNameIndex] + "\n");

								int enumConstNameIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
								index += 2;
								System.out.println("        enum constant name index: " + enumConstNameIndex + "\n");
								System.out.println("        enum constant name value: " + arrayOfConstants[enumConstNameIndex] + "\n");
							}
							else if ('c' == elementTag) {
								int classInfoIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
								index += 2;
								System.out.println("        class info index: " + classInfoIndex + "\n");
								System.out.println("        class info value: " + arrayOfConstants[classInfoIndex] + "\n");
							}
							else {
								System.out.println("WAAAT " + elementTag);
								System.exit(1);
							}

							System.out.println("        element value index is " + (l+1) + "\n");
						}

						System.out.println("      annotation index is " + (k+1) + "\n");
					}
				}
				else if ("SourceDebugExtension".equals(attributeName)) {
					// SourceDebugExtension_attribute {
					//     u2 attribute_name_index;
					//     u4 attribute_length;
					//     u1 debug_extension[attribute_length];
					// }

					int attributeLength = byteArrayToInt(Arrays.copyOfRange(data, index, index+4));
					System.out.println("    attribute length: " + attributeLength + "\n");
					index+=4;

					index += attributeLength;
				}
				else if ("SourceDebugExtension".equals(attributeName)) {
					// SourceDebugExtension_attribute {
					//     u2 attribute_name_index;
					//     u4 attribute_length;
					//     u1 debug_extension[attribute_length];
					// }

					int attributeLength = byteArrayToInt(Arrays.copyOfRange(data, index, index+4));
					System.out.println("    attribute length: " + attributeLength + "\n");
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

					int attributeLength = byteArrayToInt(Arrays.copyOfRange(data, index, index+4));
					System.out.println("    attribute length: " + attributeLength + "\n");
					index+=4;

					int numberOfBootstrapMethods = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
					System.out.println("    number of bootstrap methods: " + numberOfBootstrapMethods + "\n");
					index += 2;

					for (int j = 0; j < numberOfBootstrapMethods; j++) {
						int methodRef = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
						index += 2;
						System.out.println("    bootstrap method reference is " + methodRef + "\n");

						int numberOfBootstrapArguments = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
						index += 2;
						System.out.println("    number of arguments " + numberOfBootstrapArguments + "\n");

						for (int k = 0; k < numberOfBootstrapArguments; k++) {
							int bootstrapArgumentConstantIndex = signBitAsValue(data[index])*256+signBitAsValue(data[index+1]);
							System.out.println("      argument constant index " + bootstrapArgumentConstantIndex + "\n");
							index += 2;
						}

						System.out.println("    bootstrap method index is " + (j+1) + "\n");
					}
				}

				System.out.println("    attribute index is " + (i+1) + "\n");
			}

			if (data.length == index) {
				System.out.println("Success!!");
			}
			else {
				System.out.println("Failure!!");
			}
		}
	}

}
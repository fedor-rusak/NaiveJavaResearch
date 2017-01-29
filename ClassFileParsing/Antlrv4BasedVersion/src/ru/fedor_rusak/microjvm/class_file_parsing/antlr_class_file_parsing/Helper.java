package ru.fedor_rusak.microjvm.antlr_class_file_parsing;

public class Helper {

	public static int hexToInt(String hex) {
		return Integer.parseInt(hex, 16);
	}

	public static String hexToString(String hex) {
		int length = hex.length();

		byte[] data = new byte[length / 2];
		for (int i = 0; i < length; i += 2) {
			data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
		                         + Character.digit(hex.charAt(i+1), 16));
		}

		return new String(data);
	}

}
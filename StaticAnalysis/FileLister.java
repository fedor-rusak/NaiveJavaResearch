import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.Enumeration;

import java.io.InputStream;

import java.nio.CharBuffer;


public class FileLister {

	public static void main(String[] args) throws Exception {
		if (args.length == 0) System.exit(1);

		ZipFile zipFile = new ZipFile(args[0]);

		Enumeration zipEntries = zipFile.entries();
		while (zipEntries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) zipEntries.nextElement();
			String fileName = (entry).getName();


			if (entry.isDirectory() == false && fileName.endsWith(".class")) {
				System.out.println(fileName);

				InputStream inputStream = zipFile.getInputStream(entry);

				int value;
				CharBuffer charBuffer = CharBuffer.allocate((int) entry.getSize());
				while ((value = inputStream.read()) != -1) {
					charBuffer.put((char) value);
				}

				inputStream.close();

				ru.fedor_rusak.microjvm.antlr_class_file_parsing.ParserHelper.getClassDetailsJSONString(charBuffer.array());
	        }
		}

		zipFile.close();
	}

}
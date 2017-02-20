import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.Enumeration;
import java.util.Stack;

import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;

import java.nio.CharBuffer;


public class FileLister {

	public static void main(String[] args) throws Exception {
		if (args.length == 0) System.exit(1);

		File file = new File(args[0]);

		String targetFolder = "result";

		if (args.length > 1)
			targetFolder = args[1];

		if (file.isDirectory())
			forDirectory(file, targetFolder);
		else
			forZip(file, targetFolder);
	}

	public static char[] getCharData(InputStream inputStream, long length) throws Exception {
		int value;
		CharBuffer charBuffer = CharBuffer.allocate((int) length);
		while ((value = inputStream.read()) != -1) {
			charBuffer.put((char) value);
		}

		return charBuffer.array();
	}

	public static void writeToFile(String filePath, String content) throws Exception {
		File resultFile = new File(filePath);
		resultFile.getParentFile().mkdirs();
		resultFile.createNewFile();

		PrintStream out = new PrintStream(new FileOutputStream(resultFile));
		out.print(content);
		out.close();
	}

	public static void handleResult(String filePath, char[] classData) throws Exception {
		String data = ru.fedor_rusak.microjvm.antlr_class_file_parsing.ParserHelper.getClassDetailsJSONString(classData);

		writeToFile(filePath, data);
	}

	public static void forZip(File file, String targetFolder) throws Exception {
		ZipFile zipFile = new ZipFile(file);

		Enumeration zipEntries = zipFile.entries();
		while (zipEntries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) zipEntries.nextElement();
			String filePath = (entry).getName();


			if (entry.isDirectory() == false && filePath.endsWith(".class")) {
				System.out.println(filePath);

				InputStream inputStream = zipFile.getInputStream(entry);
				char[] classData = getCharData(inputStream, entry.getSize());
				inputStream.close();

				String resultFilePath = targetFolder+"/"+filePath+".json";

				handleResult(resultFilePath, classData);
			}
		}

		zipFile.close();
	}

	public static void forDirectory(File file, String targetFolder) throws Exception {
		Stack<File> stack = new Stack<File>();
		stack.push(file);

		File currentFolder;
		while (stack.empty() == false) {
			currentFolder = stack.pop();

			File[] currentEntries = currentFolder.listFiles();

			for (int i = 0; i < currentEntries.length; i++) {
				File currentEntry = currentEntries[i];
				if (currentEntry.isDirectory()) {
					stack.push(currentEntry);
				}
				else if (currentEntry.getPath().endsWith(".class")) {
					String filePath = currentEntry.getPath().substring(file.getPath().length()+1);
					System.out.println(currentEntry.getPath());
			
					InputStream inputStream = new FileInputStream(currentEntry);		
					char[] classData = getCharData(inputStream, currentEntry.length());
					inputStream.close();

					String data = ru.fedor_rusak.microjvm.antlr_class_file_parsing.ParserHelper.getClassDetailsJSONString(classData);

					String resultFilePath = targetFolder+"/"+filePath+".json";

					writeToFile(resultFilePath, data);
				}
			}
		}
	}

}
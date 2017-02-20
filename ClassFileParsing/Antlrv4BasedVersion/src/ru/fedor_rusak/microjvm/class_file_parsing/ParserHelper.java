package ru.fedor_rusak.microjvm.antlr_class_file_parsing;


import org.antlr.v4.runtime.CommonTokenStream; 
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream; 
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.Token;

import org.antlr.v4.runtime.atn.PredictionMode;

import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.misc.Pair;

import org.antlr.v4.runtime.tree.xpath.XPath;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;


public class ParserHelper {

	static class BinaryANTLRInputStream extends ANTLRInputStream {

		public BinaryANTLRInputStream(char[] data, int numberOfActualCharsInArray) {
			super(data, numberOfActualCharsInArray);
		}

		/** Print the decimal value rather than treat as char */
		@Override
		public String getText(Interval interval) {
			StringBuilder buf = new StringBuilder();
			int start = interval.a;
			int stop = interval.b;
			if(stop >= this.n) {
				stop = this.n - 1;
			}

			for (int i = start; i<=stop; i++) {
				int v = data[i];
				buf.append(String.format("%02x", v));
			}
			return buf.toString().toUpperCase();
		}
	}

	static class MyTokenSource implements TokenSource {

		private CharStream charStream;

		private CommonTokenFactory defaultFactory = (CommonTokenFactory) CommonTokenFactory.DEFAULT;

		public MyTokenSource(CharStream charStream) {
			this.charStream = charStream;
		}

		int charPosition = 0;


		public Token nextToken() {
			String result = charStream.getText(new Interval(charPosition, charPosition));
			charPosition++;

			int receivedBytePosition = charPosition - 1;

			return defaultFactory.create(
				new Pair(null, null), //special pair object
				result.length() == 0 ? Token.EOF : Token.MIN_USER_TOKEN_TYPE, //type
				result, //text
				Token.DEFAULT_CHANNEL, //channel
				receivedBytePosition, //start
				receivedBytePosition, //stop
				0, //line
				receivedBytePosition //charPositionInLine
			);
		}

		public int getLine() {return 0;}

		public int getCharPositionInLine() {return charPosition;}

		public CharStream getInputStream() {return null;}

		public String getSourceName() {return null;}

		private TokenFactory factory;

		public void setTokenFactory(TokenFactory<?> factory) {}

		public TokenFactory<?> getTokenFactory() {return null;}
	}

	public static ClassFileParser getParser(char[] fileData) throws Exception {
		CharStream input = new BinaryANTLRInputStream(fileData, fileData.length);

		CommonTokenStream tokens = new CommonTokenStream(new MyTokenSource(input)); 

		ClassFileParser parser = new ClassFileParser(tokens);
		//magic settings for performance
		parser.setErrorHandler(new org.antlr.v4.runtime.BailErrorStrategy());
		parser.getInterpreter().setPredictionMode(PredictionMode.SLL);

		return parser;
	}

	public static char[] getFileDataISOEncoding(String filePath) throws java.io.IOException {
		return org.antlr.v4.runtime.misc.Utils.readFile(filePath, "ISO-8859-1");
	}

	public static ParseTree findElement(RuleContext tree, String query, Parser parser) {
		return (ParseTree) XPath.findAll(tree, query, parser).toArray()[0];
	}

	public static String findElementText(RuleContext tree, String query, Parser parser) {
		return (findElement(tree, query, parser)).getText();
	}


	public static String getClassDetailsJSONString(String filePath) throws Exception {
		char[] fileData = getFileDataISOEncoding(filePath);
		return getClassDetailsJSONString(fileData);
	}

	public static String getClassDetailsJSONString(char[] fileData) throws Exception {
		long start = System.currentTimeMillis();

		ClassFileParser parser = getParser(fileData);
		ParserRuleContext rootElementContext = parser.startPoint();

		// System.out.println("Parsing time: " + (System.currentTimeMillis() - start)/1000.0 + " second(s)");

		int thisClassIndex = Helper.hexToInt(findElementText(rootElementContext, "startPoint/thisClass", parser));
		int superClassIndex = Helper.hexToInt(findElementText(rootElementContext, "startPoint/superClass", parser));

		ParseTree constantPoolStorage = findElement(rootElementContext, "/startPoint/constantPoolStorage", parser);
		int poolCount = Helper.hexToInt(constantPoolStorage.getChild(0).getText());


		Map<String, String> data;
		int constantIndex = 0;

		String indent = "\t";
		String innerIndent = indent + "\t";

		List<Map<String, String>> usefulData = new ArrayList<Map<String, String>>();
		Set<String> classIndices = new HashSet<String>();

		for (ParseTree tree : XPath.findAll(rootElementContext, "/startPoint/constantPoolStorage/constantElement", parser)) {
			ParseTree subtree = tree.getChild(1);
			String ruleName = ClassFileParser.ruleNames[((RuleContext) subtree).getRuleIndex()];

			data = new HashMap<String, String>();
			constantIndex += 1;
			data.put("index", ""+constantIndex);
			data.put("type", ""+ruleName);

			if ("utf8Data".equals(ruleName)) {
				data.put("value", Helper.hexToString(subtree.getChild(1).getText()));
			}
			else if ("classData".equals(ruleName)) {
				data.put("value", ""+Helper.hexToInt(subtree.getText()));
				classIndices.add(data.get("value"));
			}


			if ("utf8Data".equals(ruleName) || "classData".equals(ruleName)) {
				usefulData.add(data);
			}

			if ("doubleData".equals(ruleName) || "longData".equals(ruleName))
				constantIndex+=1;
		}

		for (int i = usefulData.size()-1; i >= 0; i--) {
			String type = usefulData.get(i).get("type");
			String value = usefulData.get(i).get("value");
			String index = usefulData.get(i).get("index");

			if ("utf8Data".equals(type) && classIndices.contains(index) == false) {
				usefulData.remove(i);
			}
		}


		String constants = "";

		for (int i = 0; i < usefulData.size(); i++) {
			String type = usefulData.get(i).get("type");
			String value = usefulData.get(i).get("value");
			String index = usefulData.get(i).get("index");

			String newElement = indent + "\t" + "{\n";

			newElement += indent + "\t\t" + "\"index\": " + index + ",\n";
			newElement += indent + "\t\t" + "\"type\": \"" + type + "\",\n";
			if ("utf8Data".equals(type)) {
				newElement += indent + "\t\t" + "\"value\": \"" + value + "\"\n";
			}
			else if ("classData".equals(type)) {
				newElement += indent + "\t\t" + "\"classIndex\": " + value + "\n";
			}

			newElement += indent + "\t" + "}";

			if (i+1 != usefulData.size())
				newElement += ",\n";

			constants += newElement;
		}

		constants = "[\n"+constants+"\n"+indent+"]";

		return "{\n"+"\t\"thisClassIndex\": " + thisClassIndex + ",\n" + "\t\"superClassIndex\": " + superClassIndex + ",\n" + "\t\"constantPool\": " + constants + "\n}";
	}

}
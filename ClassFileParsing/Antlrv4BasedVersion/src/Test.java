import org.antlr.v4.runtime.tree.xpath.XPath;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;

import ru.fedor_rusak.microjvm.antlr_class_file_parsing.ParserHelper;
import ru.fedor_rusak.microjvm.antlr_class_file_parsing.ClassFileParser;


public class Test {

	public static void main(String[] args) throws Exception {
		if (args.length == 0) System.exit(1);

		char[] fileData = ParserHelper.getFileDataISOEncoding(args[0]);

		long start = System.currentTimeMillis();

		ClassFileParser parser = ParserHelper.getParser(fileData);
		ParserRuleContext rootElementContext = parser.startPoint();

		System.out.println("Parsing time: " + (System.currentTimeMillis() - start)/1000.0 + " second(s)");

		System.out.println("Constants:");
		for (ParseTree t : XPath.findAll(rootElementContext, "/startPoint/constantPoolStorage/constantElement", parser)) {
			RuleContext r = (RuleContext) t;
			r = (RuleContext) r.getChild(1);
			System.out.println("  "+ClassFileParser.ruleNames[r.getRuleIndex()]+": " + r.getText());
		}
	}

}
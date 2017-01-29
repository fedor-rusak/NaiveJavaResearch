import ru.fedor_rusak.microjvm.antlr_class_file_parsing.ParserHelper;


public class Test {

	public static void main(String[] args) throws Exception {
		if (args.length == 0) System.exit(1);

		System.out.println(ParserHelper.getClassDetailsJSONString(args[0]));
	}

}
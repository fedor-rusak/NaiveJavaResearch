import org.antlr.v4.runtime.CommonTokenStream; 
import org.antlr.v4.runtime.ANTLRInputStream; 
import org.antlr.v4.runtime.ANTLRFileStream; 
import org.antlr.v4.runtime.CharStream; 
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.WritableToken;

import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.misc.Pair;

public class Test {

	static class BinaryANTLRFileStream extends ANTLRFileStream {

		public BinaryANTLRFileStream(String fileName) throws java.io.IOException {
			super(fileName, "ISO-8859-1");
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

	public static void main(String[] args) throws Exception { 
		if (args.length == 0) System.exit(1);

		CharStream input = new BinaryANTLRFileStream(args[0]);

		CommonTokenStream tokens = new CommonTokenStream(new MyTokenSource(input)); 

		ClassFileParser parser = new ClassFileParser(tokens);

		parser.startPoint();
	}

} 
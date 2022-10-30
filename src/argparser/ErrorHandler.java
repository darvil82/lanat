package argparser;

import argparser.displayFormatter.TextFormatter;
import argparser.utils.Pair;

import java.util.ArrayList;

public class ErrorHandler {
	private final ArrayList<Token> tokens;

	static class TokenizeError {
		public final ParseErrorType type;
		public final int index;

		TokenizeError(ParseErrorType type, int index) {
			this.type = type;
			this.index = index;
		}
	}

	static class ParseError extends TokenizeError {
		public final Argument<?, ?> arg;
		public final int valueCount;

		ParseError(ParseErrorType type, int index, Argument<?, ?> arg, int valueCount) {
			super(type, index);
			this.arg = arg;
			this.valueCount = valueCount;
		}
	}


	public ErrorHandler(ArrayList<Token> tokens) {
		this.tokens = tokens;
	}

	public void displayErrors() {
		TextFormatter.print(TextFormatter.format(" ", tokens));
	}


	private int getSubCommandTokenIndexByNestingLevel(int level) {
		for (int i = 0, appearances = 0; i < this.tokens.size(); i++) {
			if (this.tokens.get(i).type() == TokenType.SubCommand) {
				appearances++;
			}
			if (appearances >= level) {
				return i;
			}
		}
		return -1;
	}

	public void handleTokensResult(ParseResult<Void> result) {
		Pair<Integer, ParseResult<Void>> failedResult = result.getFirstMatchingSubResult(r -> !r.isCorrect());
		if (failedResult == null) {
			return;
		}

		var x = this.getSubCommandTokenIndexByNestingLevel(failedResult.first());
//		this.addDisplayIndicator(x + failedResult.first(), "an error occurred");
	}
}

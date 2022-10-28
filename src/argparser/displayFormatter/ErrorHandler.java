package argparser.displayFormatter;

import argparser.ParseResult;
import argparser.Token;
import argparser.TokenType;
import argparser.utils.Pair;

import java.util.ArrayList;

public class ErrorHandler {
	private final ArrayList<Token> tokens;
	private boolean isCorrect = true;

	public ErrorHandler(ArrayList<Token> tokens) {
		this.tokens = tokens;
	}

	public void displayTokens() {
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

		System.out.println("a");
	}
}

package argparser.displayFormatter;

import argparser.ParseResult;
import argparser.Token;
import argparser.TokenType;
import argparser.utils.Pair;

import java.util.ArrayList;

public class ErrorHandler {
	private final ArrayList<Token> tokens;

	public ErrorHandler(ArrayList<Token> tokens) {
		this.tokens = tokens;
	}

	public void displayTokens() {
		TerminalDisplayer.display(tokens, " ");
	}

	private int getTokenIndexByAppearance(TokenType tt, int index) {
		for (int i = 0, appearances = 0; i < this.tokens.size(); i++) {
			if (this.tokens.get(i).type() == tt) {
				appearances++;
			}
			if (appearances == index) {
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
		var x = this.getTokenIndexByAppearance(TokenType.SubCommand, failedResult.first());

		System.out.println("a");
	}
}

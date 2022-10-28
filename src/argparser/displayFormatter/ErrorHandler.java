package argparser.displayFormatter;

import argparser.ParseResult;
import argparser.Token;

import java.util.ArrayList;

public class ErrorHandler {
	public static void handleTokensResult(ArrayList<Token> tokens, ParseResult<Void> result) {
		TerminalDisplayer.display(tokens, " ");
	}
}

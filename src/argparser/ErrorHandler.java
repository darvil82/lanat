package argparser;

import argparser.displayFormatter.TextFormatter;
import argparser.utils.Pair;

import java.util.ArrayList;

public class ErrorHandler {
	private final Command rootCmd;
	private final ArrayList<Token> tokens;
	private final ArrayList<Pair<String, ArrayList<TokenizeError>>> tokenizeErrors = new ArrayList<>();
	private final ArrayList<Pair<String, ArrayList<ParseError>>> parserErrors = new ArrayList<>();

	record TokenizeError(TokenizeErrorType type, int index) {}

	record ParseError(ParseErrorType type, int index, Argument<?, ?> arg, int valueCount) {}


	public ErrorHandler(Command cmd) {
		this.rootCmd = cmd;
		this.tokens = cmd.getFullTokenList();
	}

	public void displayErrors() {
		TextFormatter.print(TextFormatter.format(" ", tokens));
	}


	private void collectErrors(Command cmd) {
		if (!cmd.tokenizeState.errors.isEmpty())
			this.tokenizeErrors.add(new Pair<>(cmd.name, cmd.tokenizeState.errors));
		if (!cmd.parseState.errors.isEmpty())
			this.parserErrors.add(new Pair<>(cmd.name, cmd.parseState.errors));

		for (var subCmd : cmd.subCommands) {
			this.collectErrors(subCmd);
		}
	}

	/**
	 * Collects the errors of all subcommands.
	 */
	public void collectErrors() {
		this.collectErrors(this.rootCmd);
	}
}

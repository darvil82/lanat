package argparser;

import argparser.displayFormatter.Color;
import argparser.displayFormatter.FormatOption;
import argparser.displayFormatter.TextFormatter;

import java.util.List;

enum ParseErrorType {
	None(null),
	ArgumentNotFound("Argument '%s' not found"),
	ObligatoryArgumentNotUsed("Obligatory argument '%s' not used"),
	UnmatchedToken("Unmatched token '%s'"),
	ArgIncorrectValueNumber("Argument '%s' expects to receive from %d to %d values, but received %d.");

	public final String msg;

	ParseErrorType(String msg) {
		this.msg = msg;
	}
}

enum TokenizeErrorType {
	None,
	TupleAlreadyOpen,
	UnexpectedTupleClose,
	TupleNotClosed,
	StringNotClosed,
}

public class ErrorHandler {
	private final Command rootCmd;
	private final List<Token> tokens;

	record TokenizeError(TokenizeErrorType type, int index) {}

	record ParseError(ParseErrorType type, int index, Argument<?, ?> arg, int valueCount) {}


	public ErrorHandler(Command cmd) {
		this.rootCmd = cmd;
		this.tokens = cmd.getFullTokenList();
	}

	private void displayTokensWithError(int start, int end) {
		StringBuilder buff = new StringBuilder();

		if (start >= this.tokens.size()) {
			buff.append(String.join(" ", this.tokens.stream().map(t -> t.getFormatter().toString()).toList()))
				.append(" ")
				.append(TextFormatter.ERROR("<-"));
		} else {
			for (int i = 0; i < this.tokens.size(); i++) {
				var content = this.tokens.get(i).getFormatter();
				if (i >= start && i <= end + start) {
					content.setColor(Color.BrightRed).addFormat(FormatOption.Bold, FormatOption.Reverse);
				}
				buff.append(content).append(" ");
			}
		}
		System.out.println(buff);
	}

	private void displayTokensWithError(int index) {
		this.displayTokensWithError(index, index);
	}


	public void displayErrors() {
		var x = this.rootCmd.getTokenizedSubCommands();
		for (int i = 0; i < x.size(); i++) {
			var cmd = x.get(i);
			var cmdTokenIndex = getSubCommandTokenIndexByNestingLevel(i);
			for (var tokenizeError : cmd.tokenizeState.errors) {
				displayTokensWithError(cmdTokenIndex + tokenizeError.index);
				System.out.println(tokenizeError.type);
			}

			for (var parseError : cmd.parseState.errors) {
				displayTokensWithError(cmdTokenIndex + parseError.index, parseError.valueCount);
				System.out.printf((parseError.type.msg) + "%n", parseError.arg.getAlias(), parseError.arg.getNumberOfValues().min, parseError.arg.getNumberOfValues().max, Math.max(parseError.valueCount - 1, 0));
			}
		}
	}

	private int getSubCommandTokenIndexByNestingLevel(int level) {
		for (int i = 0, appearances = 0; i < this.tokens.size(); i++) {
			if (this.tokens.get(i).type() == TokenType.SubCommand) {
				appearances++;
			}
			if (appearances >= level) {
				return i + (level == 0 ? 0 : 1); // this is done to skip the subcommand token itself
			}
		}
		return -1;
	}


	/**
	 * Collects the errors of all subcommands.
	 */
	public void collectErrors() {
//		this.collectErrors(this.rootCmd);
		System.out.println();
	}
}

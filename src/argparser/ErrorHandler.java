package argparser;

import argparser.displayFormatter.Color;
import argparser.displayFormatter.FormatOption;
import argparser.displayFormatter.TextFormatter;
import argparser.utils.UtlString;

import java.util.ArrayList;
import java.util.List;

enum ParseErrorType {
	None,
	ArgumentNotFound,
	ObligatoryArgumentNotUsed,
	UnmatchedToken,
	ArgIncorrectValueNumber;
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

	record TokenizeError(TokenizeErrorType type, int index) {
	}

	record ParseError(ParseErrorType type, int index, Argument<?, ?> arg, int valueCount) {
	}

	private int cmdAbsoluteTokenIndex = 0;

	private class ParseErrorHandlers {
		private int index;

		public void handleParseError(ParseError err) {
			this.index = err.index;

			formatErrorInfo(switch (err.type) {
				case ArgIncorrectValueNumber -> this.handleIncorrectValueNumber(err.arg, err.valueCount);
				case ObligatoryArgumentNotUsed -> this.handleObligatoryArgumentNotUsed(err.arg);
				case ArgumentNotFound -> this.handleArgumentNotFound(tokens.get(this.index).contents());

				default -> {
					displayTokensWithError(err.index, true);
					yield err.type.toString();
				}
			});
		}

		private String handleIncorrectValueNumber(Argument<?, ?> arg, int valueCount) {
			displayTokensWithError(this.index + (valueCount == 0 ? 0 : 1), valueCount, valueCount == 0);
			return String.format(
				"Incorrect number of values for argument '%s'.%nExpected %s, but got %d.",
				arg.getAlias(), arg.getNumberOfValues().getMessage(), Math.max(valueCount - 1, 0)
			);
		}

		private String handleObligatoryArgumentNotUsed(Argument<?, ?> arg) {
			displayTokensWithError(this.index, true);
			return "Obligatory argument '" + arg.getAlias() + "' not used.";
		}

		private String handleArgumentNotFound(String argName) {
			return "Argument '" + argName + "' not found.";
		}
	}

	public ErrorHandler(Command cmd) {
		this.rootCmd = cmd;
		this.tokens = cmd.getFullTokenList();
	}

	private void formatErrorInfo(String contents) {
		// first figure out the length of the longest line
		var maxLength = UtlString.getLongestLine(contents).length();

		var formatter = new TextFormatter()
			.setColor(Color.BrightRed)
			.addFormat(FormatOption.Bold);

		System.out.println(
			contents.replaceAll(
				"^|\\n",
				formatter.setContents("\n │ ").toString() // first insert a vertical bar at the start of each
				// line
			)
				// then insert a horizontal bar at the end, with the length of the longest line
				// approximately
				+ formatter.setContents("\n └" + "─".repeat(Math.max(maxLength - 5, 0)) + " ───── ── ─")
				.toString()
				+ "\n");
	}

	private void displayTokensWithError(int start, int offset, boolean placeArrow) {
		start += this.cmdAbsoluteTokenIndex;
		final var arrow = TextFormatter.ERROR("<-");
		var tokensFormatters = new ArrayList<>(this.tokens.stream().map(Token::getFormatter).toList());
		int tokensLength = this.tokens.size();

		if (start < 0) {
			tokensFormatters.add(0, arrow);
		} else if (start >= tokensLength) {
			tokensFormatters.add(arrow);
		}

		for (int i = 0; i < tokensLength; i++) {
			if (i < this.cmdAbsoluteTokenIndex) {
				tokensFormatters.get(i).addFormat(FormatOption.Dim);
			}

			if (i >= start && i < start + offset + 1) {
				if (placeArrow) {
					tokensFormatters.add(i + 1, arrow);
				} else {
					tokensFormatters.get(i).setColor(Color.BrightRed).addFormat(FormatOption.Reverse,
						FormatOption.Bold);
				}
			}
		}

		System.out.print(String.join(" ", tokensFormatters.stream().map(TextFormatter::toString).toList()));
	}

	private void displayTokensWithError(int index, boolean placeArrow) {
		this.displayTokensWithError(index, 0, placeArrow);
	}

	public void handleErrors() {
		var parseErrorHandler = this.new ParseErrorHandlers();
		List<Command> commands = this.rootCmd.getTokenizedSubCommands();

		for (int i = 0; i < commands.size(); i++) {
			Command cmd = commands.get(i);
			this.cmdAbsoluteTokenIndex = getCommandTokenIndexByNestingLevel(i);

			for (var tokenizeError : cmd.tokenizeState.errors) {
				System.out.println(tokenizeError.type);
			}

			for (var parseError : cmd.parseState.errors) {
				parseErrorHandler.handleParseError(parseError);
			}
		}
	}

	private int getCommandTokenIndexByNestingLevel(int level) {
		for (int i = 0, appearances = 0; i < this.tokens.size(); i++) {
			if (this.tokens.get(i).type() == TokenType.SubCommand) {
				appearances++;
			}
			if (appearances >= level) {
				return i - (level == 0 ? 1 : 0); // this is done to skip the subcommand token itself
			}
		}
		return -1;
	}
}

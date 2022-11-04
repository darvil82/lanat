package argparser;

import argparser.displayFormatter.Color;
import argparser.displayFormatter.FormatOption;
import argparser.displayFormatter.TextFormatter;
import argparser.utils.UtlString;

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

	record TokenizeError(TokenizeErrorType type, int index) {}

	record ParseError(ParseErrorType type, int index, Argument<?, ?> arg, int valueCount) {}

	ParseErrorHandlers parseErrorHandlers;
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
			var errorMsg = new StringBuilder();
			errorMsg.append(String.format("Incorrect number of values for argument '%s'.%n", arg.getAlias()));

			ArgValueCount argValueCount;
			if ((argValueCount = arg.getNumberOfValues()).isRange()) {
				errorMsg.append(String.format("Expected %d to %d values", argValueCount.min, argValueCount.max));
			} else {
				errorMsg.append(String.format("Expected %d value%s", argValueCount.min, argValueCount.min == 1 ? "" : "s"));
			}
			return errorMsg.append(String.format(", but got %d.", Math.max(valueCount - 1, 0))).toString();
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
		this.parseErrorHandlers = this.new ParseErrorHandlers();
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
				formatter.setContents("\n │ ").toString() // first insert a vertical bar at the start of each line
			)
				// then insert a horizontal bar at the end, with the length of the longest line approximately
				+ formatter.setContents("\n └" + "─".repeat(Math.max(maxLength - 5, 0)) + " ───── ── ─").toString() + "\n"
		);
	}

	private void displayTokensWithError(int start, int offset, boolean placeArrow) {
		// TODO ill rewrite this ok i know its cringe at the moment
		start += this.cmdAbsoluteTokenIndex;
		StringBuilder buff = new StringBuilder();
		var arrow = TextFormatter.ERROR("<-");
		int tokensLength = this.tokens.size();

		if (start < 0 || start >= tokensLength) {
			if (start < 0) {
				buff.append(arrow).append(" ");
			}

			// just show all of them
			buff.append(String.join(" ", this.tokens.stream().map(t -> t.getFormatter().addFormat(FormatOption.Dim).toString()).toList()));

			if (start >= tokensLength) {
				buff.append(" ").append(arrow);
			}
		} else {
			for (int i = 0; i < tokensLength; i++) {
				var content = this.tokens.get(i).getFormatter();

				if (i < this.cmdAbsoluteTokenIndex)
					content.addFormat(FormatOption.Dim);

				if (i >= start && i <= start + offset) {
					if (!placeArrow)
						content.setColor(Color.BrightRed).addFormat(FormatOption.Bold, FormatOption.Reverse);
				}

				buff.append(content).append(" ");

				if (
					(i >= start && i <= start + offset)
						&& placeArrow
				) {
					buff.append(arrow).append(" ");
				}
			}
		}

		System.out.print(buff);
	}

	private void displayTokensWithError(int index, boolean placeArrow) {
		this.displayTokensWithError(index, 0, placeArrow);
	}


	public void handleErrors() {
		List<Command> commands = this.rootCmd.getTokenizedSubCommands();
		for (int i = 0; i < commands.size(); i++) {
			Command cmd = commands.get(i);
			this.cmdAbsoluteTokenIndex = getCommandTokenIndexByNestingLevel(i);

			for (var tokenizeError : cmd.tokenizeState.errors) {
				System.out.println(tokenizeError.type);
			}

			for (var parseError : cmd.parseState.errors) {
				parseErrorHandlers.handleParseError(parseError);
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

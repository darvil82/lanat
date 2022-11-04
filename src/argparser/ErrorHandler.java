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
					displayTokensWithError(err.index);
					yield err.type.toString();
				}
			});
		}

		private String handleIncorrectValueNumber(Argument<?, ?> arg, int valueCount) {
			displayTokensWithError(this.index + 1, valueCount);
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

	private void displayTokensWithError(int start, int offset) {
		start += this.cmdAbsoluteTokenIndex;
		StringBuilder buff = new StringBuilder();

		if (start >= this.tokens.size() || start < 0) {
			if (start < 0) buff.append(TextFormatter.ERROR("->")).append(" ");

			buff.append(String.join(" ", this.tokens.stream().map(t -> t.getFormatter().toString()).toList()));

			if (start >= this.tokens.size()) buff.append(" ").append(TextFormatter.ERROR("<-"));
		} else {
			for (int i = 0; i < this.tokens.size(); i++) {
				var content = this.tokens.get(i).getFormatter();
				if (i >= start && i <= offset + start) {
					content.setColor(Color.BrightRed).addFormat(FormatOption.Bold, FormatOption.Reverse);
				}
				buff.append(content).append(" ");
			}
		}
		System.out.print(buff);
	}

	private void displayTokensWithError(int index) {
		this.displayTokensWithError(index, index);
	}


	public void handleErrors() {
		List<Command> commands = this.rootCmd.getTokenizedSubCommands();
		for (int i = 0; i < commands.size(); i++) {
			Command cmd = commands.get(i);
			this.cmdAbsoluteTokenIndex = getSubCommandTokenIndexByNestingLevel(i);

			for (var tokenizeError : cmd.tokenizeState.errors) {
				displayTokensWithError(tokenizeError.index);
				System.out.println(tokenizeError.type);
			}

			for (var parseError : cmd.parseState.errors) {
				parseErrorHandlers.handleParseError(parseError);
			}
		}
	}

	private int getSubCommandTokenIndexByNestingLevel(int level) {
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

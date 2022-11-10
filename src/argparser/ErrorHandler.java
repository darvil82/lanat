package argparser;

import argparser.displayFormatter.Color;
import argparser.displayFormatter.FormatOption;
import argparser.displayFormatter.TextFormatter;
import argparser.utils.UtlString;

import java.util.ArrayList;
import java.util.List;

enum ErrorLevel {
	ERROR,
	WARNING,
	INFO;
}

enum ParseErrorType {
	NONE,
	ARGUMENT_NOT_FOUND,
	OBLIGATORY_ARGUMENT_NOT_USED,
	UNMATCHED_TOKEN(ErrorLevel.WARNING),
	ARG_INCORRECT_VALUE_NUMBER,
	CUSTOM_ERROR;

	public final ErrorLevel level;

	ParseErrorType() {
		this.level = ErrorLevel.ERROR;
	}

	ParseErrorType(ErrorLevel level) {
		this.level = level;
	}
}

enum TokenizeErrorType {
	NONE,
	TUPLE_ALREADY_OPEN,
	UNEXPECTED_TUPLE_CLOSE,
	TUPLE_NOT_CLOSED,
	STRING_NOT_CLOSED
}

class ParseStateErrorBase<Type> {
	public final Type type;
	public final int index;

	public ParseStateErrorBase(Type type, int index) {
		this.type = type;
		this.index = index;
	}
}

class TokenizeError extends ParseStateErrorBase<TokenizeErrorType> {
	public TokenizeError(TokenizeErrorType type, int index) {
		super(type, index);
	}
}

class ParseError extends ParseStateErrorBase<ParseErrorType> {
	public final Argument<?, ?> argument;
	public final int valueCount;

	public ParseError(ParseErrorType type, int index, Argument<?, ?> argument, int valueCount) {
		super(type, index);
		this.argument = argument;
		this.valueCount = valueCount;
	}
}

class CustomParseError extends ParseError {
	public final String message;
	public final ErrorLevel level;

	public CustomParseError(String message, int index, ErrorLevel level) {
		super(ParseErrorType.CUSTOM_ERROR, index, null, 0);
		this.message = message;
		this.level = level;
	}
}

public class ErrorHandler {
	private final Command rootCmd;
	private final List<Token> tokens;

	private int cmdAbsoluteTokenIndex = 0;

	private class ParseErrorHandlers {
		protected int index;

		public void handleParseErrors(ArrayList<ParseError> errList) {
			var newList = new ArrayList<>(errList);
			for (var err : newList) {
				/* if we are going to show an error about an argument being incorrectly used, and that argument is defined
				 * as obligatory, we don't need to show the obligatory error since its obvious that the user knows that
				 * the argument is obligatory */
				if (err.type == ParseErrorType.ARG_INCORRECT_VALUE_NUMBER) {
					newList.removeIf(e -> e.argument.equals(err.argument) && e.type == ParseErrorType.OBLIGATORY_ARGUMENT_NOT_USED);
				}
			}
			newList.forEach(this::handleError);
		}

		protected void handleError(ParseError err) {
			this.index = err.index;
			Token currentToken = tokens.get(this.index);

			formatErrorInfo(switch (err.type) {
				case ARG_INCORRECT_VALUE_NUMBER -> this.handleIncorrectValueNumber(err.argument, err.valueCount);
				case OBLIGATORY_ARGUMENT_NOT_USED -> this.handleObligatoryArgumentNotUsed(err.argument);
				case ARGUMENT_NOT_FOUND -> this.handleArgumentNotFound(currentToken.contents());
				case UNMATCHED_TOKEN -> this.handleUnmatchedToken(currentToken.contents());

				default -> {
					displayTokensWithError(err.index + 1);
					yield err.type.toString();
				}
			});
		}

		protected String handleIncorrectValueNumber(Argument<?, ?> arg, int valueCount) {
			displayTokensWithError(this.index + 1, valueCount, valueCount == 0);
			return String.format(
				"Incorrect number of values for argument '%s'.%nExpected %s, but got %d.",
				arg.getAlias(), arg.getNumberOfValues().getMessage(), Math.max(valueCount - 1, 0)
			);
		}

		protected String handleObligatoryArgumentNotUsed(Argument<?, ?> arg) {
			displayTokensWithError(this.index);
			var argCmd = arg.getParentCmd();
			return argCmd.isRootCommand()
				? String.format("Obligatory argument '%s' not used.", arg.getAlias())
				: String.format("Obligatory argument '%s' for command '%s' not used.", arg.getAlias(), argCmd.name);
		}

		protected String handleArgumentNotFound(String argName) {
			return "Argument '" + argName + "' not found.";
		}

		protected String handleUnmatchedToken(String token) {
			displayTokensWithError(this.index + 1);
			return "Unmatched token '" + token + "'.";
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
			.setColor(Color.BRIGHT_RED)
			.addFormat(FormatOption.BOLD);

		System.err.println(
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
				tokensFormatters.get(i).addFormat(FormatOption.DIM);
			}

			if (i >= start && i < start + offset + 1) {
				if (placeArrow) {
					tokensFormatters.add(i + 1, arrow);
				} else {
					tokensFormatters.get(i)
						.setColor(Color.BRIGHT_RED)
						.addFormat(FormatOption.REVERSE, FormatOption.BOLD);
				}
			}
		}

		System.err.print(String.join(" ", tokensFormatters.stream().map(TextFormatter::toString).toList()));
	}

	private void displayTokensWithError(int index) {
		this.displayTokensWithError(index, 0, true);
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

			parseErrorHandler.handleParseErrors(cmd.parseState.errors);
		}
	}

	private int getCommandTokenIndexByNestingLevel(int level) {
		for (int i = 0, appearances = 0; i < this.tokens.size(); i++) {
			if (this.tokens.get(i).type() == TokenType.SUB_COMMAND) {
				appearances++;
			}
			if (appearances >= level) {
				return i - (level == 0 ? 1 : 0); // this is done to skip the subcommand token itself
			}
		}
		return -1;
	}

	public int getErrorCode() {
		// TODO: implement error codes
		return 1;
	}

	public boolean hasErrors() {
		return this.rootCmd.getTokenizedSubCommands().stream().anyMatch(cmd -> !cmd.parseState.errors.isEmpty());
	}
}

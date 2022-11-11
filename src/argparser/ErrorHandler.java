package argparser;

import argparser.displayFormatter.Color;
import argparser.displayFormatter.FormatOption;
import argparser.displayFormatter.TextFormatter;
import argparser.utils.UtlString;

import java.util.ArrayList;
import java.util.List;

enum ErrorLevel {
	ERROR(Color.BRIGHT_CYAN),
	WARNING(Color.BRIGHT_YELLOW),
	INFO(Color.BRIGHT_BLUE);

	public final Color color;

	ErrorLevel(Color color) {
		this.color = color;
	}
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
		protected ErrorFormatter formatter;

		public void handleParseErrors(ArrayList<ParseError> errList) {
			var newList = new ArrayList<>(errList);
			for (var err : newList) {
				/* if we are going to show an error about an argument being incorrectly used, and that argument is defined
				 * as obligatory, we don't need to show the obligatory error since its obvious that the user knows that
				 * the argument is obligatory */
				if (err.type == ParseErrorType.ARG_INCORRECT_VALUE_NUMBER) {
					newList.removeIf(e ->
						e.argument != null
							&& e.argument.equals(err.argument)
							&& e.type == ParseErrorType.OBLIGATORY_ARGUMENT_NOT_USED
					);
				}
			}
			newList.forEach(this::handleError);
		}

		protected void handleError(ParseError err) {
			this.index = err.index;
			Token currentToken = tokens.get(Math.min(this.index + cmdAbsoluteTokenIndex + 1, tokens.size() - 1));
			this.formatter = new ErrorFormatter(err.type.level);

			switch (err.type) {
				case ARG_INCORRECT_VALUE_NUMBER -> this.handleIncorrectValueNumber(err.argument, err.valueCount);
				case OBLIGATORY_ARGUMENT_NOT_USED -> this.handleObligatoryArgumentNotUsed(err.argument);
				case ARGUMENT_NOT_FOUND -> this.handleArgumentNotFound(currentToken.contents());
				case UNMATCHED_TOKEN -> this.handleUnmatchedToken(currentToken.contents());

				default -> this.formatter.displayTokens(err.index + 1);
			}

			this.formatter.print();
		}

		protected void handleIncorrectValueNumber(Argument<?, ?> arg, int valueCount) {
			this.formatter
				.setContents(String.format(
					"Incorrect number of values for argument '%s'.%nExpected %s, but got %d.",
					arg.getAlias(), arg.getNumberOfValues().getMessage(), Math.max(valueCount - 1, 0)
				))
				.displayTokens(this.index + 1, valueCount, valueCount == 0);
		}

		protected void handleObligatoryArgumentNotUsed(Argument<?, ?> arg) {
			var argCmd = arg.getParentCmd();

			this.formatter
				.setContents(
					argCmd.isRootCommand()
						? String.format("Obligatory argument '%s' not used.", arg.getAlias())
						: String.format("Obligatory argument '%s' for command '%s' not used.", arg.getAlias(), argCmd.name)
				)
				.displayTokens(this.index);
		}

		protected void handleArgumentNotFound(String argName) {
			this.formatter.setContents(String.format("Argument '%s' not found.", argName));
		}

		protected void handleUnmatchedToken(String token) {
			this.formatter
				.setContents(String.format("Token '%s' does not correspond with a valid argument, value, or command.", token))
				.displayTokens(this.index + 1, 0, false);
		}
	}

	private class ErrorFormatter {
		private String contents;
		private String tokensView;
		private final ErrorLevel errorLevel;

		public ErrorFormatter(ErrorLevel level) {
			this.errorLevel = level;
		}

		public ErrorFormatter setContents(String contents) {
			this.contents = contents;
			return this;
		}

		public void print() {
			// first figure out the length of the longest line
			var maxLength = UtlString.getLongestLine(this.contents).length();

			var formatter = new TextFormatter()
				.setColor(this.errorLevel.color)
				.addFormat(FormatOption.BOLD);

			System.err.println(
				formatter.setContents(String.format(" │ %s\n", this.errorLevel)).toString()
					+ this.tokensView
					+ this.contents.replaceAll(
					"^|\\n",
					formatter.setContents("\n │ ").toString() // first insert a vertical bar at the start of each line
				)
					// then insert a horizontal bar at the end, with the length of the longest line
					// approximately
					+ formatter.setContents("\n └" + "─".repeat(Math.max(maxLength - 5, 0)) + " ───── ── ─")
					.toString()
					+ "\n"
			);
		}

		private ErrorFormatter displayTokens(int start, int offset, boolean placeArrow) {
			start += cmdAbsoluteTokenIndex;
			final var arrow = TextFormatter.ERROR("<-").setColor(this.errorLevel.color);
			var tokensFormatters = new ArrayList<>(tokens.stream().map(Token::getFormatter).toList());
			int tokensLength = tokens.size();

			if (start < 0) {
				tokensFormatters.add(0, arrow);
			} else if (start >= tokensLength) {
				tokensFormatters.add(arrow);
			}

			for (int i = 0; i < tokensLength; i++) {
				if (i < cmdAbsoluteTokenIndex) {
					tokensFormatters.get(i).addFormat(FormatOption.DIM);
				}

				if (i >= start && i < start + offset + 1) {
					if (placeArrow) {
						tokensFormatters.add(i + 1, arrow);
					} else {
						tokensFormatters.get(i)
							.setColor(this.errorLevel.color)
							.addFormat(FormatOption.REVERSE, FormatOption.BOLD);
					}
				}
			}

			this.tokensView = String.join(" ", tokensFormatters.stream().map(TextFormatter::toString).toList());
			return this;
		}

		private ErrorFormatter displayTokens(int index) {
			return this.displayTokens(index, 0, true);
		}
	}

	public ErrorHandler(Command cmd) {
		this.rootCmd = cmd;
		this.tokens = cmd.getFullTokenList();
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

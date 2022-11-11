package argparser;

import argparser.displayFormatter.Color;

import java.util.ArrayList;
import java.util.List;

interface ErrorLevelProvider {
	ErrorLevel getErrorLevel();
}

enum ErrorLevel {
	ERROR(Color.BRIGHT_RED),
	WARNING(Color.BRIGHT_YELLOW),
	INFO(Color.BRIGHT_BLUE);

	public final Color color;

	ErrorLevel(Color color) {
		this.color = color;
	}
}


abstract class ParseStateErrorBase<T extends ErrorLevelProvider> {
	public final T type;
	public final int index;

	public ParseStateErrorBase(T type, int index) {
		this.type = type;
		this.index = index;
	}

	public abstract void handle(ErrorHandler handler);

	public boolean isError() {
		return this.type.getErrorLevel() == ErrorLevel.ERROR;
	}
}

class TokenizeError extends ParseStateErrorBase<TokenizeError.TokenizeErrorType> {
	enum TokenizeErrorType implements ErrorLevelProvider {
		TUPLE_ALREADY_OPEN,
		UNEXPECTED_TUPLE_CLOSE,
		TUPLE_NOT_CLOSED,
		STRING_NOT_CLOSED;

		@Override
		public ErrorLevel getErrorLevel() {
			return ErrorLevel.ERROR;
		}
	}

	public TokenizeError(TokenizeErrorType type, int index) {
		super(type, index);
	}

	@Override
	public void handle(ErrorHandler handler) {
		handler.handleErrors();
	}
}

class ParseError extends ParseStateErrorBase<ParseError.ParseErrorType> {
	public final Argument<?, ?> argument;
	public final int valueCount;

	enum ParseErrorType implements ErrorLevelProvider {
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

		@Override
		public ErrorLevel getErrorLevel() {
			return this.level;
		}
	}

	public ParseError(ParseError.ParseErrorType type, int index, Argument<?, ?> argument, int valueCount) {
		super(type, index);
		this.argument = argument;
		this.valueCount = valueCount;
	}

	public static void handleErrors(List<ParseError> errors, ErrorHandler handler) {
		var newList = new ArrayList<>(errors);
		for (var err : newList) {
			/* if we are going to show an error about an argument being incorrectly used, and that argument is defined
			 * as obligatory, we don't need to show the obligatory error since its obvious that the user knows that
			 * the argument is obligatory */
			if (err.type == ParseError.ParseErrorType.ARG_INCORRECT_VALUE_NUMBER) {
				newList.removeIf(e ->
					e.argument != null
						&& e.argument.equals(err.argument)
						&& e.type == ParseError.ParseErrorType.OBLIGATORY_ARGUMENT_NOT_USED
				);
			}
		}
		newList.forEach(e -> e.handle(handler));
	}

	@Override
	public void handle(ErrorHandler handler) {
		Token currentToken = handler.tokens.get(Math.min(this.index + handler.cmdAbsoluteTokenIndex + 1, handler.tokens.size() - 1));
		this.formatter = new ErrorHandler.ErrorFormatter(err.type.level);

		switch (err.type) {
			case ParseErrorType.ARG_INCORRECT_VALUE_NUMBER -> this.handleIncorrectValueNumber();
			case ParseErrorType.OBLIGATORY_ARGUMENT_NOT_USED -> this.handleObligatoryArgumentNotUsed();
			case ParseErrorType.ARGUMENT_NOT_FOUND -> this.handleArgumentNotFound();
			case ParseErrorType.UNMATCHED_TOKEN -> this.handleUnmatchedToken();

			default -> this.formatter.displayTokens(err.index + 1);
		}

		this.formatter.print();
	}

	protected void handleIncorrectValueNumber() {
		this.formatter
			.setContents(String.format(
				"Incorrect number of values for argument '%s'.%nExpected %s, but got %d.",
				argument.getAlias(), argument.getNumberOfValues().getMessage(), Math.max(valueCount - 1, 0)
			))
			.displayTokens(this.index + 1, valueCount, valueCount == 0);
	}

	protected void handleObligatoryArgumentNotUsed() {
		var argCmd = argument.getParentCmd();

		this.formatter
			.setContents(
				argCmd.isRootCommand()
					? String.format("Obligatory argument '%s' not used.", argument.getAlias())
					: String.format("Obligatory argument '%s' for command '%s' not used.", argument.getAlias(), argCmd.name)
			)
			.displayTokens(this.index);
	}

	protected void handleArgumentNotFound() {
		this.formatter.setContents(String.format("Argument '%s' not found.", argument.getAlias()));
	}

	protected void handleUnmatchedToken() {
		this.formatter
			.setContents(String.format("Token '%s' does not correspond with a valid argument, value, or command.", token))
			.displayTokens(this.index + 1, 0, false);
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
	final List<Token> tokens;

	int cmdAbsoluteTokenIndex = 0;


	public ErrorHandler(Command cmd) {
		this.rootCmd = cmd;
		this.tokens = cmd.getFullTokenList();
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


	public void handleErrors() {
		List<Command> commands = this.rootCmd.getTokenizedSubCommands();

		for (int i = 0; i < commands.size(); i++) {
			Command cmd = commands.get(i);
			this.cmdAbsoluteTokenIndex = getCommandTokenIndexByNestingLevel(i);

			for (var tokenizeError : cmd.tokenizeState.errors) {
				tokenizeError.handle(this);
			}

			ParseError.handleErrors(cmd.parseState.errors, this);
		}
	}

}
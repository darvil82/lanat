package argparser;

import argparser.utils.ErrorLevel;
import argparser.utils.ErrorLevelProvider;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;


abstract class ParseStateErrorBase<T extends ErrorLevelProvider> implements ErrorLevelProvider {
	public final T type;
	public int index;
	private ErrorHandler errorHandler;
	private ErrorFormatter formatter;

	public ParseStateErrorBase(T type, int index) {
		this.type = type;
		this.index = index;
	}

	public final void handle(ErrorHandler handler) {
		this.errorHandler = handler;
		this.formatter = new ErrorFormatter(handler, type.getErrorLevel());

		for (var method : this.getClass().getDeclaredMethods()) {
			Handler annotation = method.getAnnotation(Handler.class);

			if (annotation != null && annotation.value().equals(this.type.toString())) {
				try {
					method.invoke(this);
				} catch (IllegalAccessException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}
		}

		this.formatter.print();
	}

	@Override
	public ErrorLevel getErrorLevel() {
		return this.type.getErrorLevel();
	}

	protected Token getCurrentToken() {
		return this.errorHandler.getRelativeToken(this.index);
	}

	protected ErrorFormatter fmt() {
		return this.formatter;
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Handler {
		String value();
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

	@Handler("TUPLE_ALREADY_OPEN")
	protected void handleTupleAlreadyOpen() {
		this.fmt()
			.setContents("Tuple already open.")
			.displayTokens(this.index);
	}

	@Handler("TUPLE_NOT_CLOSED")
	protected void handleTupleNotClosed() {
		this.fmt()
			.setContents("Tuple not closed.")
			.displayTokens(this.index);
	}

	@Handler("UNEXPECTED_TUPLE_CLOSE")
	protected void handleUnexpectedTupleClose() {
		this.fmt()
			.setContents("Unexpected tuple close.")
			.displayTokens(this.index);
	}

	@Handler("STRING_NOT_CLOSED")
	protected void handleStringNotClosed() {
		this.fmt()
			.setContents("String not closed.")
			.displayTokens(this.index, 0, true);
	}
}

class ParseError extends ParseStateErrorBase<ParseError.ParseErrorType> {
	public final Argument<?, ?> argument;
	public final int valueCount;

	enum ParseErrorType implements ErrorLevelProvider {
		ARGUMENT_NOT_FOUND,
		OBLIGATORY_ARGUMENT_NOT_USED,
		UNMATCHED_TOKEN(ErrorLevel.WARNING),
		ARG_INCORRECT_VALUE_NUMBER;

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

	public static void handleAll(List<ParseError> errors, ErrorHandler handler) {
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

	@Handler("ARG_INCORRECT_VALUE_NUMBER")
	protected void handleIncorrectValueNumber() {
		this.fmt()
			.setContents(String.format(
				"Incorrect number of values for argument '%s'.%nExpected %s, but got %d.",
				argument.getAlias(), argument.getNumberOfValues().getMessage(), Math.max(valueCount - 1, 0)
			))
			.displayTokens(this.index + 1, valueCount, valueCount == 0);
	}

	@Handler("OBLIGATORY_ARGUMENT_NOT_USED")
	protected void handleObligatoryArgumentNotUsed() {
		var argCmd = argument.getParentCmd();

		this.fmt()
			.setContents(
				argCmd.isRootCommand()
					? String.format("Obligatory argument '%s' not used.", argument.getAlias())
					: String.format("Obligatory argument '%s' for command '%s' not used.", argument.getAlias(), argCmd.name)
			)
			.displayTokens(this.index + 1);
	}

	@Handler("ARGUMENT_NOT_FOUND")
	protected void handleArgumentNotFound() {
		this.fmt().setContents(String.format("Argument '%s' not found.", argument.getAlias()));
	}

	@Handler("UNMATCHED_TOKEN")
	protected void handleUnmatchedToken() {
		this.fmt()
			.setContents(String.format(
				"Token '%s' does not correspond with a valid argument, value, or command.",
				this.getCurrentToken().contents())
			)
			.displayTokens(this.index + 1, 0, false);
	}
}

class CustomError extends ParseStateErrorBase<CustomError.CustomParseErrorType> {
	private final String message;
	private final ErrorLevel level;
	private boolean showTokens = true;

	enum CustomParseErrorType implements ErrorLevelProvider {
		DEFAULT;

		@Override
		public ErrorLevel getErrorLevel() {
			return ErrorLevel.ERROR;
		}
	}

	public CustomError(String message, int index, ErrorLevel level) {
		super(CustomParseErrorType.DEFAULT, index);
		this.message = message;
		this.level = level;
	}

	public CustomError(String message, ErrorLevel level) {
		this(message, -1, level);
		this.showTokens = false;
	}

	@Override
	public ErrorLevel getErrorLevel() {
		return this.level;
	}

	@Handler("DEFAULT")
	protected void handleDefault() {
		this.fmt()
			.setErrorLevel(this.level)
			.setContents(this.message);

		if (this.showTokens)
			this.fmt().displayTokens(this.index, 0, false);
	}
}

public class ErrorHandler {
	final List<Token> tokens;
	private final Command rootCmd;
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
		return this.rootCmd.getErrorCode();
	}


	public void handleErrors() {
		List<Command> commands = this.rootCmd.getTokenizedSubCommands();

		for (int i = 0; i < commands.size(); i++) {
			Command cmd = commands.get(i);
			this.cmdAbsoluteTokenIndex = this.getCommandTokenIndexByNestingLevel(i);

			for (var cmdError : cmd.getErrorsUnderDisplayLevel()) {
				cmdError.handle(this);
			}

			for (var tokenizeError : cmd.tokenizeState.getErrorsUnderDisplayLevel()) {
				tokenizeError.handle(this);
			}

			for (var customError : cmd.parseState.getCustomErrors()) {
				customError.handle(this);
			}

			ParseError.handleAll(cmd.parseState.getErrorsUnderDisplayLevel(), this);
		}
	}

	public Token getRelativeToken(int index) {
		return this.tokens.get(Math.min(this.cmdAbsoluteTokenIndex + index + 1, this.tokens.size() - 1));
	}
}
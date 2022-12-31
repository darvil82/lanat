package argparser;

import argparser.utils.ErrorLevelProvider;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * Provides a {@link ParseStateErrorBase#handle(ErrorHandler)} method that when called, automatically invokes the
 * appropriate defined method with the {@link Handler} annotation value that matches the value passed to the constructor.
 * This is used to display the errors on screen.
 *
 * <p>
 *  Example class that inherits from this:
 * <pre>
 * {@code
 * class MyHandler extends ParseStateErrorBase<MyHandler.MyErrors> {
 *    public enum MyErrors implements ErrorLevelProvider {
 *       ERROR1,
 *       ERROR2;
 *
 *       @Override
 *       public ErrorLevel getErrorLevel() {
 *          return ErrorLevel.ERROR;
 *       }
 *    }
 *
 *    @Handler("ERROR1")
 *    public void handle1() {
 *       // do something
 *    }
 *
 *    @Handler("ERROR2")
 *    public void handle2() {
 *       // do something
 *    }
 * }
 *
 * ...
 *
 * var handler = new MyHandler(MyHandler.MyErrors.ERROR1);
 * handler.handle(errorHandler); // will call handle1()
 * }
 * </pre>
 * </p>
 *
 * The enum type must implement {@link ErrorLevelProvider}. This allows the error text formatter to color errors
 * according to their severity.
 *
 * @param <T> An enum with the possible error types to handle.
 */
abstract class ParseStateErrorBase<T extends ErrorLevelProvider> implements ErrorLevelProvider {
	public final T type;
	public int tokenIndex;
	private ErrorHandler errorHandler;
	private ErrorFormatter formatter;

	public ParseStateErrorBase(T type, int tokenIndex) {
		this.type = type;
		this.tokenIndex = tokenIndex;
	}

	public final void handle(ErrorHandler handler) {
		this.errorHandler = handler;
		this.formatter = new ErrorFormatter(handler, type.getErrorLevel());

		Method[] methods;
		Class<?> currentClass = this.getClass();

		// if there are no methods defined, get super class
		// this is done for cases like usage of anonymous classes
		while ((methods = currentClass.getDeclaredMethods()).length == 0)
			currentClass = currentClass.getSuperclass();

		for (var method : methods) {
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
		return this.errorHandler.getRelativeToken(this.tokenIndex);
	}

	/**
	 * Returns the current {@link ErrorFormatter} instance that can be configured to display the error.
	 */
	protected ErrorFormatter fmt() {
		return this.formatter;
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Handler {
		String value();
	}
}

@SuppressWarnings("unused")
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
			.displayTokens(this.tokenIndex + 1);
	}

	@Handler("TUPLE_NOT_CLOSED")
	protected void handleTupleNotClosed() {
		this.fmt()
			.setContents("Tuple not closed.")
			.displayTokens(this.tokenIndex + 1);
	}

	@Handler("UNEXPECTED_TUPLE_CLOSE")
	protected void handleUnexpectedTupleClose() {
		this.fmt()
			.setContents("Unexpected tuple close.")
			.displayTokens(this.tokenIndex + 1);
	}

	@Handler("STRING_NOT_CLOSED")
	protected void handleStringNotClosed() {
		this.fmt()
			.setContents("String not closed.")
			.displayTokens(this.tokenIndex + 1);
	}
}

@SuppressWarnings("unused")
class ParseError extends ParseStateErrorBase<ParseError.ParseErrorType> {
	public final Argument<?, ?> argument;
	public final int valueCount;
	private ArgumentGroup argumentGroup;

	enum ParseErrorType implements ErrorLevelProvider {
		OBLIGATORY_ARGUMENT_NOT_USED,
		UNMATCHED_TOKEN(ErrorLevel.WARNING),
		ARG_INCORRECT_VALUE_NUMBER,
		MULTIPLE_ARGS_IN_EXCLUSIVE_GROUP_USED;

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

	public void setArgumentGroup(ArgumentGroup argumentGroup) {
		this.argumentGroup = argumentGroup;
	}

	public static List<ParseError> filter(List<ParseError> errors) {
		var newList = new ArrayList<>(errors);
		for (var err : errors) {
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
		return newList;
	}

	@Handler("ARG_INCORRECT_VALUE_NUMBER")
	protected void handleIncorrectValueNumber() {
		this.fmt()
			.setContents(String.format(
				"Incorrect number of values for argument '%s'.%nExpected %s, but got %d.",
				argument.getDisplayName(), argument.getNumberOfValues().getMessage(), Math.max(this.valueCount - 1, 0)
			))
			.displayTokens(this.tokenIndex + 1, this.valueCount, this.valueCount == 0);
	}

	@Handler("OBLIGATORY_ARGUMENT_NOT_USED")
	protected void handleObligatoryArgumentNotUsed() {
		var argCmd = argument.getParentCmd();

		this.fmt()
			.setContents(
				argCmd.isRootCommand()
					? String.format("Obligatory argument '%s' not used.", argument.getDisplayName())
					: String.format("Obligatory argument '%s' for command '%s' not used.", argument.getDisplayName(), argCmd.name)
			)
			.displayTokens(this.tokenIndex + 1);
	}

	@Handler("UNMATCHED_TOKEN")
	protected void handleUnmatchedToken() {
		this.fmt()
			.setContents(String.format(
				"Token '%s' does not correspond with a valid argument, value, or command.",
				this.getCurrentToken().contents())
			)
			.displayTokens(this.tokenIndex, this.valueCount, false);
	}

	@Handler("MULTIPLE_ARGS_IN_EXCLUSIVE_GROUP_USED")
	protected void handleMultipleArgsInExclusiveGroupUsed() {
		this.fmt()
			.setContents(String.format(
				"Multiple arguments in exclusive group '%s' used.",
				argumentGroup.name
			))
			.displayTokens(this.tokenIndex, this.valueCount, false);
	}
}

public class ErrorHandler {
	final List<Token> tokens;
	private final Command rootCmd;
	int cmdAbsoluteTokenIndex = 0;


	public ErrorHandler(Command rootCommand) {
		this.rootCmd = rootCommand;
		this.tokens = rootCommand.getFullTokenList();
	}

	/**
	 * Handles all errors and displays them to the user.
	 */
	public void handleErrorsView() {
		List<Command> commands = this.rootCmd.getTokenizedSubCommands();

		for (int i = 0; i < commands.size(); i++) {
			Command cmd = commands.get(i);
			this.cmdAbsoluteTokenIndex = this.getCommandTokenIndexByNestingLevel(i);

			new ArrayList<ParseStateErrorBase<?>>() {{
				addAll(cmd.getErrorsUnderDisplayLevel());
				addAll(cmd.tokenizingState.getErrorsUnderDisplayLevel());
				addAll(cmd.parsingState.getCustomErrors());
				addAll(ParseError.filter(cmd.parsingState.getErrorsUnderDisplayLevel()));
			}}.stream()
				.sorted(Comparator.comparingInt(x -> x.tokenIndex))
				.forEach(e -> e.handle(this));
		}
	}

	/**
	 * Returns the token at the specified index, offset by the current command's token index ({@link #cmdAbsoluteTokenIndex}).
	 */
	public Token getRelativeToken(int index) {
		return this.tokens.get(this.cmdAbsoluteTokenIndex + index);
	}

	/**
	 * Returns the index of a command in the token list by its nesting level by order of appearance.
	 * For example, in a token list like this:<br>
	 * <pre>{@code
	 * {
	 *   SUB_COMMAND,
	 *   ARGUMENT_NAME,
	 *   ARGUMENT_VALUE,
	 *   SUB_COMMAND, // <- here
	 *   ARGUMENT_NAME_LIST,
	 *   SUB_COMMAND,
	 *   ARGUMENT_NAME
	 * }}</pre>
	 * The nesting level of the second subcommand is <strong>1</strong> (starting at 0),
	 * and its index in the token list is <strong>3</strong>.
	 * @return <code>-1</code> if the command is not found.
	 */
	private int getCommandTokenIndexByNestingLevel(int level) {
		if (level <= 0) return 0;

		for (int i = 0, appearances = 0; i < this.tokens.size(); i++) {
			if (this.tokens.get(i).type() == TokenType.SUB_COMMAND) {
				appearances++;
			}
			if (appearances > level) {
				return i;
			}
		}

		return -1;
	}

	public int getErrorCode() {
		return this.rootCmd.getErrorCode();
	}
}
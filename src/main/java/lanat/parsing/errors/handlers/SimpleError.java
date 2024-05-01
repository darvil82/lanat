package lanat.parsing.errors.handlers;

import lanat.parsing.errors.Error;
import lanat.parsing.errors.contexts.ErrorFormattingContext;
import lanat.parsing.errors.contexts.ParseErrorContext;
import lanat.utils.errors.ErrorLevel;
import org.jetbrains.annotations.NotNull;

/** A simple error that has a message and an error level. */
public class SimpleError implements Error.ParseError {
	private final @NotNull String message;
	private final @NotNull ErrorLevel errorLevel;
	private final boolean showInput;

	/**
	 * Instantiates a new simple error.
	 * @param message the message of the error
	 * @param errorLevel the error level
	 * @param showInput whether to show the input in the error message
	 */
	public SimpleError(@NotNull String message, @NotNull ErrorLevel errorLevel, boolean showInput) {
		this.message = message;
		this.errorLevel = errorLevel;
		this.showInput = showInput;
	}

	/**
	 * Instantiates a new simple error.
	 * @param message the message of the error
	 * @param errorLevel the error level
	 */
	public SimpleError(@NotNull String message, @NotNull ErrorLevel errorLevel) {
		this(message, errorLevel, false);
	}

	@Override
	public void handle(@NotNull ErrorFormattingContext fmt, @NotNull ParseErrorContext ctx) {
		fmt.withContent(this.message);

		if (this.showInput)
			fmt.showInput();
	}

	@Override
	public @NotNull ErrorLevel getErrorLevel() {
		return this.errorLevel;
	}
}
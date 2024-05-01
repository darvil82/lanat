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

	public SimpleError(@NotNull String message, @NotNull ErrorLevel errorLevel) {
		this.message = message;
		this.errorLevel = errorLevel;
	}

	@Override
	public void handle(@NotNull ErrorFormattingContext fmt, @NotNull ParseErrorContext ctx) {
		fmt.withContent(this.message);
	}

	@Override
	public @NotNull ErrorLevel getErrorLevel() {
		return this.errorLevel;
	}
}
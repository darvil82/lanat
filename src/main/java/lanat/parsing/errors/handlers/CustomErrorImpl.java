package lanat.parsing.errors.handlers;

import lanat.ErrorLevel;
import lanat.parsing.errors.Error;
import lanat.parsing.errors.contexts.ErrorFormattingContext;
import lanat.parsing.errors.contexts.ParseErrorContext;
import org.jetbrains.annotations.NotNull;

/**
 * Implements the basic behavior of a custom error.
 * @see Error.CustomError
 */
public class CustomErrorImpl implements Error.CustomError {
	/** The error message. */
	private final @NotNull String message;
	/** The error level. */
	private final @NotNull ErrorLevel errorLevel;
	/** The index of the token that caused the error. */
	private int index;

	/**
	 * Instantiates a new custom error.
	 * @param message the error message
	 * @param errorLevel the error level
	 * @param index the index of the token that caused the error
	 */
	public CustomErrorImpl(@NotNull String message, @NotNull ErrorLevel errorLevel, int index) {
		this.message = message;
		this.errorLevel = errorLevel;
		this.index = index;
	}

	@Override
	public void handle(@NotNull ErrorFormattingContext fmt, @NotNull ParseErrorContext ctx) {
		fmt
			.withContent(this.message)
			.highlight(this.index, 0, false);
	}

	@Override
	public int getIndex() {
		return this.index;
	}

	/**
	 * {@inheritDoc}
	 * The offset must be positive.
	 */
	@Override
	public void offsetIndex(int offset) {
		if (offset < 0)
			throw new IllegalArgumentException("offset must be positive");
		this.index += offset;
	}

	@Override
	public @NotNull ErrorLevel getErrorLevel() {
		return this.errorLevel;
	}
}
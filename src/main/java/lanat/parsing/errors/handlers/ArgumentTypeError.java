package lanat.parsing.errors.handlers;

import lanat.parsing.errors.Error;
import lanat.parsing.errors.contexts.ParseErrorContext;
import lanat.parsing.errors.contexts.formatting.DisplayInput;
import lanat.parsing.errors.contexts.formatting.ErrorFormattingContext;
import lanat.utils.errors.ErrorLevel;
import org.jetbrains.annotations.NotNull;
import utils.Pair;

/**
 * An error that may be thrown by an argument type.
 * @see Error.ParseError
 */
public class ArgumentTypeError implements Error.ParseError {
	/** The error message. */
	private final @NotNull String message;
	/** The error level. */
	private final @NotNull ErrorLevel errorLevel;
	/** The index of the token that caused the error. */
	private int index;
	/** The offset from the index. */
	private final int offset;

	/**
	 * Instantiates a new custom error.
	 * @param message the error message
	 * @param errorLevel the error level
	 * @param indexAndOffset the index of the token that caused the error and the offset from the index. Both must be positive.
	 */
	public ArgumentTypeError(
		@NotNull String message,
		@NotNull ErrorLevel errorLevel,
		@NotNull Pair<Integer, Integer> indexAndOffset
	) {
		this.message = message;
		this.errorLevel = errorLevel;
		this.index = indexAndOffset.first();
		this.offset = indexAndOffset.second();

		if (this.index < 0)
			throw new IllegalArgumentException("index must be positive");

		if (this.offset < 0)
			throw new IllegalArgumentException("offset must be positive");
	}

	/**
	 * Instantiates a new custom error.
	 * @param message the error message
	 * @param errorLevel the error level
	 * @param index the index of the token that caused the error. Must be positive.
	 */
	public ArgumentTypeError(@NotNull String message, @NotNull ErrorLevel errorLevel, int index) {
		this(message, errorLevel, new Pair<>(index, 0));
	}

	@Override
	public void handle(@NotNull ErrorFormattingContext fmt, @NotNull ParseErrorContext ctx) {
		fmt
			.withContent(this.message)
			.displayAndHighlightInput(new DisplayInput.Highlight(this.index, this.offset, false));
	}

	/**
	 * Returns the index of the token that caused the error.
	 * @return the index of the token that caused the error
	 */
	public final int getIndex() {
		return this.index;
	}

	/**
	 * Returns the offset from the index, which is the number of tokens that caused the error.
	 * @return the offset from the index
	 */
	public final int getOffsetFromIndex() {
		return this.offset;
	}

	/** Offsets the index by the given positive amount. */
	public final void offsetIndex(int offset) {
		if (offset < 0)
			throw new IllegalArgumentException("offset must be positive");
		this.index += offset;
	}

	@Override
	public @NotNull ErrorLevel getErrorLevel() {
		return this.errorLevel;
	}
}
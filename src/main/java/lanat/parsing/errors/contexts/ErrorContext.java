package lanat.parsing.errors.contexts;

import lanat.Command;
import org.jetbrains.annotations.NotNull;
import utils.Range;

/**
 * Base class for error contexts. Provides methods to easily get data about the context in which the error occurred.
 * Such data includes the command that was being parsed, the absolute index of the error, the number of
 * tokens in the command, etc.
 */
public sealed abstract class ErrorContext permits ParseErrorContext, TokenizeErrorContext {
	/** The command that was being parsed when the error occurred. */
	protected final @NotNull Command command;

	/**
	 * Instantiates a new error context.
	 * @param command the command that was being parsed when the error occurred
	 */
	public ErrorContext(@NotNull Command command) {
		this.command = command;
	}

	/**
	 * Returns the number of input values in the context.
	 * @return the number of input values in the context
	 */
	public abstract int getCount();

	/**
	 * Returns the absolute index of the context. This is the position of the first input value in the
	 * full input.
	 * @return the absolute index of the context
	 */
	public abstract int getAbsoluteIndex();

	/**
	 * Returns the absolute index of the context, offset by the given index.
	 * @param index the index to offset by
	 * @return the absolute index of the context, offset by the given index
	 */
	public int getAbsoluteIndex(int index) {
		return this.getAbsoluteIndex() + index;
	}

	/**
	 * Applies the {@link #getAbsoluteIndex()} offset to the given {@link Range}.
	 * @param range the range to offset
	 * @return a new {@link Range} with the offset applied
	 */
	public @NotNull Range applyAbsoluteOffset(@NotNull Range range) {
		return range.offset(this.getAbsoluteIndex());
	}

	/**
	 * Returns the command that was being parsed when the error occurred.
	 * @return the command that was being parsed when the error occurred
	 */
	public @NotNull Command getCommand() {
		return this.command;
	}
}
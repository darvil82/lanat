package lanat.parsing.errors.handlers;

import lanat.parsing.errors.Error;
import lanat.parsing.errors.contexts.ErrorFormattingContext;
import lanat.parsing.errors.contexts.TokenizeErrorContext;
import org.jetbrains.annotations.NotNull;

/** Contains all the errors definitions for errors that occur during tokenization. */
public abstract class TokenizeErrors {
	private TokenizeErrors() {}

	/**
	 * Error that occurs when a tuple is already open.
	 * @param index the index of the character that caused the error
	 */
	public record TupleAlreadyOpenError(int index) implements Error.TokenizeError {
		@Override
		public void handle(@NotNull ErrorFormattingContext fmt, @NotNull TokenizeErrorContext ctx) {
			fmt
				.withContent("Tuple already open.")
				.highlight(this.index, 0, false);
		}
	}

	/**
	 * Error that occurs when a tuple is not closed.
	 * @param index the index of the last start tuple character
	 */
	public record TupleNotClosedError(int index) implements Error.TokenizeError {
		@Override
		public void handle(@NotNull ErrorFormattingContext fmt, @NotNull TokenizeErrorContext ctx) {
			fmt
				.withContent("Tuple not closed.")
				.highlight(this.index, ctx.getCount() - this.index - 1, false);
		}
	}

	/**
	 * Error that occurs when a tuple close character is found without a tuple being open.
	 * @param index the index of the character that caused the error
	 */
	public record UnexpectedTupleCloseError(int index) implements Error.TokenizeError {
		@Override
		public void handle(@NotNull ErrorFormattingContext fmt, @NotNull TokenizeErrorContext ctx) {
			fmt
				.withContent("Unexpected tuple close.")
				.highlight(this.index, 0, false);
		}
	}

	/**
	 * Error that occurs when a string is not closed.
	 * @param index the index of the last quote character that opened the string
	 */
	public record StringNotClosedError(int index) implements Error.TokenizeError {
		@Override
		public void handle(@NotNull ErrorFormattingContext fmt, @NotNull TokenizeErrorContext ctx) {
			fmt
				.withContent("String not closed.")
				.highlight(this.index, ctx.getCount() - this.index - 1, false);
		}
	}

	/**
	 * Error that occurs when a space is required between two characters.
	 * @param index the index of the character that caused the error
	 */
	public record SpaceRequiredError(int index) implements Error.TokenizeError {
		@Override
		public void handle(@NotNull ErrorFormattingContext fmt, @NotNull TokenizeErrorContext ctx) {
			fmt
				.withContent("A space is required between these characters.")
				.highlight(this.index, 1, false);
		}
	}
}
package lanat.parsing.errors;

import lanat.ErrorLevel;
import lanat.parsing.errors.contexts.ErrorContext;
import lanat.parsing.errors.contexts.ErrorFormattingContext;
import lanat.parsing.errors.contexts.ParseErrorContext;
import lanat.parsing.errors.contexts.TokenizeErrorContext;
import lanat.utils.errors.ErrorLevelProvider;
import org.jetbrains.annotations.NotNull;

/**
 * An error that indicates a failure in the parsing process.
 * @param <C> the type of the error context
 */
public sealed interface Error<C extends ErrorContext> extends ErrorLevelProvider
	permits Error.ParseError, Error.TokenizeError
{
	/**
	 * Formats the error message to be displayed to the user.
	 * @param fmt The error formatting context.
	 * @param ctx The error context, which may provide useful methods to get more data about the error.
	 */
	void handle(@NotNull ErrorFormattingContext fmt, @NotNull C ctx);

	/**
	 * Returns the error level of the error. This is used to determine whether the error
	 * should be displayed to the user, depending on the error level set in the configuration of the commands.
	 * <br>
	 * By default, this method returns {@link ErrorLevel#ERROR}.
	 * @return The error level of the error.
	 */
	@Override
	default @NotNull ErrorLevel getErrorLevel() {
		return ErrorLevel.ERROR;
	}

	/** A parse error. Indicates a failure in the parsing process. */
	non-sealed interface ParseError extends Error<ParseErrorContext> { }

	/** A tokenize error. Indicates a failure in the tokenization process. */
	non-sealed interface TokenizeError extends Error<TokenizeErrorContext> { }

	/**
	 * A custom error. Indicates a failure in the parsing process in an {@link lanat.ArgumentType}.
	 * <p>
	 * Allows offsetting the index of the error. This is used by {@link lanat.ArgumentType}s to offset the index
	 * of the error when the error is dispatched to a parent {@link lanat.ArgumentType}.
	 * </p>
	 * */
	interface CustomError extends ParseError {
		/**
		 * Returns the index of the token that caused the error.
		 * @return the index of the token that caused the error
		 */
		int getIndex();

		/**
		 * Offsets the index of the error by the given offset.
		 * @param offset the offset to apply to the index
		 */
		void offsetIndex(int offset);
	}
}
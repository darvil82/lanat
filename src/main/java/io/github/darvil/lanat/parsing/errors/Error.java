package io.github.darvil.lanat.parsing.errors;

import io.github.darvil.lanat.parsing.errors.contexts.ErrorContext;
import io.github.darvil.lanat.parsing.errors.contexts.ParseErrorContext;
import io.github.darvil.lanat.parsing.errors.contexts.TokenizeErrorContext;
import io.github.darvil.lanat.parsing.errors.contexts.formatting.ErrorFormattingContext;
import io.github.darvil.lanat.utils.errors.ErrorLevel;
import io.github.darvil.lanat.utils.errors.ErrorLevelProvider;
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

	/**
	 * Determine whether this error should remove the other error from the list of errors.
	 * Only errors of the same command are compared.
	 * <p>
	 * This method is invoked for each error in the list of errors,
	 * and the error is removed if this method returns {@code true}.
	 * @param other the other error
	 * @return whether this error should remove the other error. By default, this method returns {@code false}.
	 */
	default boolean shouldRemoveOther(@NotNull Error<?> other) {
		return false;
	}


	/** A parse error. Indicates a failure in the parsing process. */
	non-sealed interface ParseError extends Error<ParseErrorContext> { }

	/** A tokenize error. Indicates a failure in the tokenization process. */
	non-sealed interface TokenizeError extends Error<TokenizeErrorContext> { }
}
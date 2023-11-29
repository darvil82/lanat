package lanat.parsing.errors;

import lanat.ErrorLevel;
import lanat.utils.ErrorLevelProvider;
import org.jetbrains.annotations.NotNull;

public sealed interface Error<C> extends ErrorLevelProvider
	permits Error.ParseError, Error.TokenizeError
{
	void handle(@NotNull ErrorFormattingContext fmt, @NotNull C ctx);

	@Override
	default @NotNull ErrorLevel getErrorLevel() {
		return ErrorLevel.ERROR;
	}

	non-sealed interface ParseError extends Error<ParseErrorContext> { }
	non-sealed interface TokenizeError extends Error<TokenizeErrorContext> { }

	interface CustomError extends ParseError {
		int getIndex();
		void offsetIndex(int offset);
	}
}
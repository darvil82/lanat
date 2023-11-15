package lanat.parsing.errors;

import lanat.ErrorLevel;
import lanat.utils.ErrorLevelProvider;
import org.jetbrains.annotations.NotNull;

public sealed interface Error<C> extends ErrorLevelProvider
	permits Error.ParseError, Error.TokenizeError, Error.CustomError
{
	void handle(@NotNull ErrorFormatter fmt, @NotNull C ctx);

	@Override
	default @NotNull ErrorLevel getErrorLevel() {
		return ErrorLevel.ERROR;
	}

	non-sealed interface ParseError extends Error<ParseContext> { }
	non-sealed interface TokenizeError extends Error<TokenizeContext> { }

	non-sealed interface CustomError extends Error<ParseContext> {
		void setIndex(int index);
		int getIndex();

		default void offsetIndex(int offset) {
			if (offset < 0)
				throw new IllegalArgumentException("offset must be positive");
			this.setIndex(this.getIndex() + offset);
		}
	}
}
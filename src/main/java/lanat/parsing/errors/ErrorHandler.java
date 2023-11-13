package lanat.parsing.errors;

import lanat.ErrorLevel;
import lanat.utils.ErrorLevelProvider;
import org.jetbrains.annotations.NotNull;

public sealed interface ErrorHandler<C> extends ErrorLevelProvider
	permits ErrorHandler.ParseErrorHandler, ErrorHandler.TokenizeErrorHandler, ErrorHandler.ArgumentTypeErrorHandler
{
	void handle(@NotNull ErrorFormatter fmt, @NotNull C ctx);

	@Override
	default @NotNull ErrorLevel getErrorLevel() {
		return ErrorLevel.ERROR;
	}

	non-sealed interface ParseErrorHandler extends ErrorHandler<ParseContext> { }
	non-sealed interface TokenizeErrorHandler extends ErrorHandler<TokenizeContext> { }
	non-sealed interface ArgumentTypeErrorHandler extends ErrorHandler<Object> { }
}
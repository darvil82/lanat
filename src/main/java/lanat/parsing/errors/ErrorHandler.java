package lanat.parsing.errors;

import lanat.ErrorLevel;
import lanat.utils.ErrorLevelProvider;
import org.jetbrains.annotations.NotNull;

public interface ErrorHandler<C> extends ErrorLevelProvider {
	void handle(@NotNull ErrorFormatter fmt, @NotNull C ctx);

	@Override
	default @NotNull ErrorLevel getErrorLevel() {
		return ErrorLevel.ERROR;
	}

	interface ParseErrorHandler extends ErrorHandler<Object> { }
	interface TokenizeErrorHandler extends ErrorHandler<Object> { }
}
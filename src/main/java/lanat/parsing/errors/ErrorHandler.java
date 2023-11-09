package lanat.parsing.errors;

import lanat.ErrorLevel;
import lanat.utils.ErrorLevelProvider;
import org.jetbrains.annotations.NotNull;

public interface ErrorHandler<F extends ErrorFormatter> extends ErrorLevelProvider {
	void handle(@NotNull F fmt);

	@Override
	default @NotNull ErrorLevel getErrorLevel() {
		return ErrorLevel.ERROR;
	}

	interface ParseErrorHandler extends ErrorHandler<ErrorFormatter> {}
	interface TokenizerErrorHandler extends ErrorHandler<ErrorFormatter> {}
}
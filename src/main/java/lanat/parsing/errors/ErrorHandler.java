package lanat.parsing.errors;

import lanat.ErrorFormatter;
import lanat.ErrorLevel;
import lanat.utils.ErrorLevelProvider;
import org.jetbrains.annotations.NotNull;

public interface ErrorHandler extends ErrorLevelProvider {
	void handle(@NotNull ErrorFormatter fmt);

	@Override
	default @NotNull ErrorLevel getErrorLevel() {
		return ErrorLevel.ERROR;
	}
}
package lanat.utils;

import lanat.ErrorLevel;
import org.jetbrains.annotations.NotNull;

public interface ErrorLevelProvider {
	@NotNull ErrorLevel getErrorLevel();
}

package lanat.utils;

import lanat.ErrorLevel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface MinimumErrorLevelConfig<T extends ErrorLevelProvider> {
	List<T> getErrorsUnderExitLevel();

	List<T> getErrorsUnderDisplayLevel();

	boolean hasExitErrors();

	boolean hasDisplayErrors();


	void setMinimumExitErrorLevel(@NotNull ErrorLevel level);

	ModifyRecord<ErrorLevel> getMinimumExitErrorLevel();

	void setMinimumDisplayErrorLevel(@NotNull ErrorLevel level);

	ModifyRecord<ErrorLevel> getMinimumDisplayErrorLevel();
}

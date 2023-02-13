package lanat.utils;

import lanat.ErrorLevel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ErrorsContainer<T extends ErrorLevelProvider> {
	@NotNull List<@NotNull T> getErrorsUnderExitLevel();

	@NotNull List<@NotNull T> getErrorsUnderDisplayLevel();

	boolean hasExitErrors();

	boolean hasDisplayErrors();


	void setMinimumExitErrorLevel(@NotNull ErrorLevel level);

	@NotNull ModifyRecord<@NotNull ErrorLevel> getMinimumExitErrorLevel();

	void setMinimumDisplayErrorLevel(@NotNull ErrorLevel level);

	@NotNull ModifyRecord<@NotNull ErrorLevel> getMinimumDisplayErrorLevel();
}
package lanat.utils;

import lanat.ErrorLevel;

import java.util.List;

public interface MinimumErrorLevelConfig<T extends ErrorLevelProvider> {
	List<T> getErrorsUnderExitLevel();

	List<T> getErrorsUnderDisplayLevel();

	boolean hasExitErrors();

	boolean hasDisplayErrors();


	void setMinimumExitErrorLevel(ErrorLevel level);

	ModifyRecord<ErrorLevel> getMinimumExitErrorLevel();

	void setMinimumDisplayErrorLevel(ErrorLevel level);

	ModifyRecord<ErrorLevel> getMinimumDisplayErrorLevel();
}

package argparser.utils;

import java.util.List;

public interface IMinimumErrorLevelConfig<T extends IErrorLevelProvider> {
	List<T> getErrorsUnderExitLevel();
	List<T> getErrorsUnderDisplayLevel();
	boolean hasExitErrors();
	boolean hasDisplayErrors();


	void setMinimumExitErrorLevel(ErrorLevel level);
	ModifyRecord<ErrorLevel> getMinimumExitErrorLevel();
	void setMinimumDisplayErrorLevel(ErrorLevel level);
	ModifyRecord<ErrorLevel> getMinimumDisplayErrorLevel();
}

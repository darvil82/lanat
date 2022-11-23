package argparser.utils;

import java.util.List;
import java.util.function.Consumer;

public interface IMinimumErrorLevelConfig<T extends ErrorLevelProvider> {
	List<T> getErrorsUnderExitLevel();
	List<T> getErrorsUnderDisplayLevel();


	void setMinimumExitErrorLevel(ErrorLevel level);
	ModifyRecord<ErrorLevel> getMinimumExitErrorLevel();
	void setMinimumDisplayErrorLevel(ErrorLevel level);
	ModifyRecord<ErrorLevel> getMinimumDisplayErrorLevel();
}

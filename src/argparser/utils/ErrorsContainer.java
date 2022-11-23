package argparser.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class ErrorsContainer<T extends ErrorLevelProvider, TCbOk, TCbErr> implements IMinimumErrorLevelConfig<T> {
	private ModifyRecord<ErrorLevel> minimumExitErrorLevel = new ModifyRecord<>(ErrorLevel.ERROR);
	private ModifyRecord<ErrorLevel> minimumDisplayErrorLevel = new ModifyRecord<>(ErrorLevel.INFO);
	private final List<T> errors = new ArrayList<>();

	public ErrorsContainer() {}

	public ErrorsContainer(
		ModifyRecord<ErrorLevel> minimumExitErrorLevelRecord,
		ModifyRecord<ErrorLevel> minimumDisplayErrorLevelRecord
	) {
		this.minimumExitErrorLevel = minimumExitErrorLevelRecord;
		this.minimumDisplayErrorLevel = minimumDisplayErrorLevelRecord;
	}

	public void addError(T error) {
		this.errors.add(error);
	}

	public boolean hasExitErrors() {
		return !this.getErrorsUnderExitLevel().isEmpty();
	}

	public boolean hasDisplayErrors() {
		return !this.getErrorsUnderDisplayLevel().isEmpty();
	}

	@Override
	public List<T> getErrorsUnderExitLevel() {
		return this.getErrorsInLevelMinimum(this.errors, false);
	}

	@Override
	public List<T> getErrorsUnderDisplayLevel() {
		return this.getErrorsInLevelMinimum(this.errors, true);
	}

	protected <TErr extends ErrorLevelProvider>
	List<TErr> getErrorsInLevelMinimum(List<TErr> errors, boolean isDisplayError) {
		return errors.stream().filter(e -> this.errorIsInMinimumLevel(e, isDisplayError)).toList();
	}

	private <TErr extends ErrorLevelProvider> boolean errorIsInMinimumLevel(TErr error, boolean isDisplayError) {
		return error.getErrorLevel().isInErrorMinimum((
			isDisplayError
				? this.minimumDisplayErrorLevel
				: this.minimumExitErrorLevel
		).get());
	}

	protected <TErr extends ErrorLevelProvider> boolean anyErrorInMinimum(List<TErr> errors, boolean isDisplayError) {
		return errors.stream().anyMatch(e -> this.errorIsInMinimumLevel(e, isDisplayError));
	}

	// --------------------------------------------- Getters and Setters -----------------------------------------------
	@Override
	public void setMinimumExitErrorLevel(ErrorLevel level) {
		this.minimumExitErrorLevel.set(level);
	}

	@Override
	public ModifyRecord<ErrorLevel> getMinimumExitErrorLevel() {
		return this.minimumExitErrorLevel;
	}

	@Override
	public void setMinimumDisplayErrorLevel(ErrorLevel level) {
		this.minimumDisplayErrorLevel.set(level);
	}

	@Override
	public ModifyRecord<ErrorLevel> getMinimumDisplayErrorLevel() {
		return this.minimumDisplayErrorLevel;
	}
}
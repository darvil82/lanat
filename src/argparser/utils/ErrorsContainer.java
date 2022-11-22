package argparser.utils;

import java.util.ArrayList;
import java.util.List;

public class ErrorsContainer<T extends ErrorLevelProvider> {
	private ModifyRecord<ErrorLevel> minimumExitErrorLevel = new ModifyRecord<>(ErrorLevel.ERROR);
	private final List<T> errors = new ArrayList<>();

	public ErrorsContainer(ModifyRecord<ErrorLevel> minimumExitErrorLevelRecord) {
		this.minimumExitErrorLevel = minimumExitErrorLevelRecord;
	}

	public ErrorsContainer() {}

	public void addError(T error) {
		this.errors.add(error);
	}

	public List<T> getErrors() {
		return this.getErrorsInMinimum(this.errors);
	}

	protected <TErr extends ErrorLevelProvider> List<TErr> getErrorsInMinimum(List<TErr> errors) {
		return errors.stream().filter(this::errorIsInMinimum).toList();
	}

	public boolean hasErrors() {
		return !this.getErrors().isEmpty();
	}

	public void setMinimumExitErrorLevel(ErrorLevel level) {
		this.minimumExitErrorLevel.set(level);
	}

	public ErrorLevel getMinimumExitErrorLevel() {
		return minimumExitErrorLevel.get();
	}

	public ModifyRecord<ErrorLevel> getMinimumExitErrorLevelRecord() {
		return this.minimumExitErrorLevel;
	}

	private <TErr extends ErrorLevelProvider> boolean errorIsInMinimum(TErr error) {
		return error.getErrorLevel().isInErrorMinimum(this.minimumExitErrorLevel.get());
	}

	protected <TErr extends ErrorLevelProvider> boolean anyErrorInMinimum(List<TErr> errors) {
		return errors.stream().anyMatch(this::errorIsInMinimum);
	}
}
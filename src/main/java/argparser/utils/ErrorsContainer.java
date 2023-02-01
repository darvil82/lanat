package argparser.utils;

import argparser.ErrorLevel;

import java.util.ArrayList;
import java.util.List;

public abstract class ErrorsContainer<T extends ErrorLevelProvider> implements MinimumErrorLevelConfig<T> {
	private ModifyRecord<ErrorLevel> minimumExitErrorLevel = new ModifyRecord<>(ErrorLevel.ERROR);
	private ModifyRecord<ErrorLevel> minimumDisplayErrorLevel = new ModifyRecord<>(ErrorLevel.INFO);
	private final List<T> errors = new ArrayList<>();

	public ErrorsContainer() {}

	public ErrorsContainer(
		ModifyRecord<ErrorLevel> minimumExitErrorLevelRecord,
		ModifyRecord<ErrorLevel> minimumDisplayErrorLevelRecord
	)
	{
		this.minimumExitErrorLevel = minimumExitErrorLevelRecord;
		this.minimumDisplayErrorLevel = minimumDisplayErrorLevelRecord;
	}

	/**
	 * Adds an error to the list of errors.
	 * @param error The error to add.
	 */
	public void addError(T error) {
		this.errors.add(error);
	}

	@Override
	public boolean hasExitErrors() {
		return !this.getErrorsUnderExitLevel().isEmpty();
	}

	@Override
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

	/**
	 * The minimum error level that will cause the program to exit.
	 * All errors with a level equal to or higher than this will cause the program to exit.
	 * For example, if this is set to {@link ErrorLevel#WARNING}, then all errors with a level of {@link ErrorLevel#WARNING}
	 * or {@link ErrorLevel#ERROR} will cause the program to exit.
	 */
	@Override
	public void setMinimumExitErrorLevel(ErrorLevel level) {
		this.minimumExitErrorLevel.set(level);
	}

	@Override
	public ModifyRecord<ErrorLevel> getMinimumExitErrorLevel() {
		return this.minimumExitErrorLevel;
	}

	/**
	 * The minimum error level that will be displayed to the user.
	 * All errors with a level lower than this will be ignored.
	 * For example: If this is set to {@link ErrorLevel#INFO}, then all errors
	 * (including {@link ErrorLevel#INFO}, {@link ErrorLevel#WARNING}, and {@link ErrorLevel#ERROR}) will be displayed,
	 * but {@link ErrorLevel#DEBUG} will not.
	 */
	@Override
	public void setMinimumDisplayErrorLevel(ErrorLevel level) {
		this.minimumDisplayErrorLevel.set(level);
	}

	@Override
	public ModifyRecord<ErrorLevel> getMinimumDisplayErrorLevel() {
		return this.minimumDisplayErrorLevel;
	}
}
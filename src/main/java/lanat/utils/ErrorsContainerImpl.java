package lanat.utils;

import lanat.ErrorLevel;
import org.jetbrains.annotations.NotNull;
import utils.ModifyRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * A container for errors. This class is used to store errors and their respective minimum error levels.
 * It also has methods for getting errors under the minimum error level.
 *
 * @param <T> The type of the errors to store.
 */
public abstract class ErrorsContainerImpl<T extends ErrorLevelProvider> implements ErrorsContainer<T>, Resettable {
	private final ModifyRecord<ErrorLevel> minimumExitErrorLevel;
	private final ModifyRecord<ErrorLevel> minimumDisplayErrorLevel;
	private final @NotNull List<T> errors = new ArrayList<>();

	/**
	 * Creates a new {@link ErrorsContainerImpl} with the default values, those being {@link ErrorLevel#ERROR} for
	 * {@link #minimumExitErrorLevel} and {@link ErrorLevel#INFO} for {@link #minimumDisplayErrorLevel}.
	 */
	public ErrorsContainerImpl() {
		// default values
		this(ModifyRecord.of(ErrorLevel.ERROR), ModifyRecord.of(ErrorLevel.INFO));
	}

	/**
	 * Creates a new {@link ErrorsContainerImpl} with the given values.
	 * @param minimumExitErrorLevelRecord    The minimum error level that will cause the program to exit.
	 * @param minimumDisplayErrorLevelRecord The minimum error level that will be displayed to the user.
	 */
	public ErrorsContainerImpl(
		@NotNull ModifyRecord<ErrorLevel> minimumExitErrorLevelRecord,
		@NotNull ModifyRecord<ErrorLevel> minimumDisplayErrorLevelRecord
	)
	{
		this.minimumExitErrorLevel = minimumExitErrorLevelRecord;
		this.minimumDisplayErrorLevel = minimumDisplayErrorLevelRecord;
	}

	/**
	 * Adds an error to the list of errors.
	 *
	 * @param error The error to add.
	 */
	public void addError(@NotNull T error) {
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
	public @NotNull List<T> getErrorsUnderExitLevel() {
		return this.getErrorsInLevelMinimum(this.errors, false);
	}

	@Override
	public @NotNull List<T> getErrorsUnderDisplayLevel() {
		return this.getErrorsInLevelMinimum(this.errors, true);
	}

	protected <TErr extends ErrorLevelProvider>
	@NotNull List<TErr> getErrorsInLevelMinimum(@NotNull List<TErr> errors, boolean isDisplayError) {
		return errors.stream().filter(e -> this.errorIsInMinimumLevel(e, isDisplayError)).toList();
	}

	private <TErr extends ErrorLevelProvider> boolean errorIsInMinimumLevel(@NotNull TErr error, boolean isDisplayError) {
		return error.getErrorLevel().isInMinimum((
			isDisplayError
				? this.minimumDisplayErrorLevel
				: this.minimumExitErrorLevel
		).get());
	}

	protected <TErr extends ErrorLevelProvider> boolean anyErrorInMinimum(@NotNull List<TErr> errors, boolean isDisplayError) {
		return errors.stream().anyMatch(e -> this.errorIsInMinimumLevel(e, isDisplayError));
	}

	@Override
	public void resetState() {
		this.errors.clear();
	}

	// --------------------------------------------- Getters and Setters -----------------------------------------------

	/**
	 * The minimum error level that will cause the program to exit. All errors with a level equal to or higher than this
	 * will cause the program to exit. For example, if this is set to {@link ErrorLevel#WARNING}, then all errors with a
	 * level of {@link ErrorLevel#WARNING} or {@link ErrorLevel#ERROR} will cause the program to exit.
	 */
	@Override
	public void setMinimumExitErrorLevel(@NotNull ErrorLevel level) {
		this.minimumExitErrorLevel.set(level);
	}

	@Override
	public @NotNull ModifyRecord<ErrorLevel> getMinimumExitErrorLevel() {
		return this.minimumExitErrorLevel;
	}

	/**
	 * The minimum error level that will be displayed to the user. All errors with a level lower than this will be
	 * ignored. For example: If this is set to {@link ErrorLevel#INFO}, then all errors (including
	 * {@link ErrorLevel#INFO}, {@link ErrorLevel#WARNING}, and {@link ErrorLevel#ERROR}) will be displayed, but
	 * {@link ErrorLevel#DEBUG} will not.
	 */
	@Override
	public void setMinimumDisplayErrorLevel(@NotNull ErrorLevel level) {
		this.minimumDisplayErrorLevel.set(level);
	}

	@Override
	public @NotNull ModifyRecord<ErrorLevel> getMinimumDisplayErrorLevel() {
		return this.minimumDisplayErrorLevel;
	}
}
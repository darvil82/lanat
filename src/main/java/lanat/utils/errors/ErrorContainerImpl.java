package lanat.utils.errors;

import lanat.utils.Resettable;
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
public abstract class ErrorContainerImpl<T extends ErrorLevelProvider> implements ErrorContainer<T>, Resettable {
	private final @NotNull ModifyRecord<ErrorLevel> minimumExitErrorLevel;
	private final @NotNull ModifyRecord<ErrorLevel> minimumDisplayErrorLevel;
	private final @NotNull List<T> errors = new ArrayList<>(5);

	/**
	 * Creates a new {@link ErrorContainerImpl} with the default values, those being {@link ErrorLevel#ERROR} for
	 * {@link #minimumExitErrorLevel} and {@link ErrorLevel#INFO} for {@link #minimumDisplayErrorLevel}.
	 */
	public ErrorContainerImpl() {
		// default values
		this(ModifyRecord.of(ErrorLevel.ERROR), ModifyRecord.of(ErrorLevel.INFO));
	}

	/**
	 * Creates a new {@link ErrorContainerImpl} with the given values.
	 * @param minimumExitErrorLevelRecord    The minimum error level that will cause the program to exit.
	 * @param minimumDisplayErrorLevelRecord The minimum error level that will be displayed to the user.
	 */
	public ErrorContainerImpl(
		@NotNull ModifyRecord<ErrorLevel> minimumExitErrorLevelRecord,
		@NotNull ModifyRecord<ErrorLevel> minimumDisplayErrorLevelRecord
	)
	{
		this.minimumExitErrorLevel = minimumExitErrorLevelRecord;
		this.minimumDisplayErrorLevel = minimumDisplayErrorLevelRecord;
	}

	@Override
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
	public void setMinimumExitErrorLevel(@NotNull ErrorLevel level) {
		this.minimumExitErrorLevel.set(level);
		this.checkValidMinimums();
	}

	@Override
	public @NotNull ModifyRecord<ErrorLevel> getMinimumExitErrorLevel() {
		return this.minimumExitErrorLevel;
	}


	@Override
	public void setMinimumDisplayErrorLevel(@NotNull ErrorLevel level) {
		this.minimumDisplayErrorLevel.set(level);
		this.checkValidMinimums();
	}

	@Override
	public @NotNull ModifyRecord<ErrorLevel> getMinimumDisplayErrorLevel() {
		return this.minimumDisplayErrorLevel;
	}

	/**
	 * Checks if the minimum error levels are valid.
	 * If the minimum exit error level is higher than the minimum display
	 * error level, then an {@link IllegalStateException} is thrown.
	 */
	private void checkValidMinimums() {
		if (!this.minimumExitErrorLevel.get().isInMinimum(this.minimumDisplayErrorLevel.get())) {
			throw new IllegalStateException("Minimum exit error level must be less than or equal to minimum display error level.");
		}
	}


	@Override
	public void resetState() {
		this.errors.clear();
	}
}
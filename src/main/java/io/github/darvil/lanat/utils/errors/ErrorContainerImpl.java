package io.github.darvil.lanat.utils.errors;

import io.github.darvil.lanat.utils.Resettable;
import io.github.darvil.utils.ModifyRecord;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A container for errors.
 * This class is used to store errors and their respective thresholds.
 * @param <T> The type of the errors to store.
 */
public abstract class ErrorContainerImpl<T extends ErrorLevelProvider> implements ErrorContainer<T>, Resettable {
	private final @NotNull ModifyRecord<ErrorLevel> errorExitThreshold;
	private final @NotNull ModifyRecord<ErrorLevel> errorDisplayThreshold;
	private final @NotNull List<T> errors = new ArrayList<>(5);

	/**
	 * Creates a new {@link ErrorContainerImpl} with the default values, those being {@link ErrorLevel#ERROR} for
	 * {@link #errorExitThreshold} and {@link ErrorLevel#INFO} for {@link #errorDisplayThreshold}.
	 */
	public ErrorContainerImpl() {
		// default values
		this(ModifyRecord.of(ErrorLevel.ERROR), ModifyRecord.of(ErrorLevel.INFO));
	}

	/**
	 * Creates a new {@link ErrorContainerImpl} with the given values.
	 * @param errorExitThreshold The threshold error level at which the program should exit.
	 * @param errorDisplayThreshold The threshold error level at which errors should be displayed to the user.
	 */
	public ErrorContainerImpl(
		@NotNull ModifyRecord<ErrorLevel> errorExitThreshold,
		@NotNull ModifyRecord<ErrorLevel> errorDisplayThreshold
	)
	{
		this.errorExitThreshold = errorExitThreshold;
		this.errorDisplayThreshold = errorDisplayThreshold;
		this.checkValidThresholds();
	}

	@Override
	public void addError(@NotNull T error) {
		this.errors.add(error);
	}

	@Override
	public boolean hasExitErrors() {
		return !this.getErrorsUnderExitThreshold().isEmpty();
	}

	@Override
	public boolean hasDisplayErrors() {
		return !this.getErrorsUnderDisplayThreshold().isEmpty();
	}

	@Override
	public @NotNull List<T> getErrorsUnderExitThreshold() {
		return this.getErrorsUnderThreshold(this.errors, this.errorExitThreshold.get());
	}

	@Override
	public @NotNull List<T> getErrorsUnderDisplayThreshold() {
		return this.getErrorsUnderThreshold(this.errors, this.errorDisplayThreshold.get());
	}

	protected <TErr extends ErrorLevelProvider>
	@NotNull List<TErr> getErrorsUnderThreshold(@NotNull List<TErr> errors, @NotNull ErrorLevel threshold) {
		return errors.stream().filter(e -> e.getErrorLevel().isInThreshold(threshold)).toList();
	}

	@Override
	public void setErrorExitThreshold(@NotNull ErrorLevel level) {
		this.errorExitThreshold.set(level);
		this.checkValidThresholds();
	}

	@Override
	public @NotNull ModifyRecord<ErrorLevel> getErrorExitThreshold() {
		return this.errorExitThreshold;
	}


	@Override
	public void setErrorDisplayThreshold(@NotNull ErrorLevel level) {
		this.errorDisplayThreshold.set(level);
		this.checkValidThresholds();
	}

	@Override
	public @NotNull ModifyRecord<ErrorLevel> getErrorDisplayThreshold() {
		return this.errorDisplayThreshold;
	}

	/**
	 * Checks if the thresholds are valid, i.e. if the error exit threshold is not lower than the error display threshold.
	 * @throws IllegalStateException if the thresholds are not valid.
	 */
	private void checkValidThresholds() {
		if (!this.errorExitThreshold.get().isInThreshold(this.errorDisplayThreshold.get())) {
			throw new IllegalStateException("The error exit threshold cannot be lower than the error display threshold.");
		}
	}


	@Override
	public void resetState() {
		this.errors.clear();
	}
}
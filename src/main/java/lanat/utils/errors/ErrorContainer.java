package lanat.utils.errors;

import org.jetbrains.annotations.NotNull;
import utils.ModifyRecord;

import java.util.List;

/**
 * Represents a container of errors. Methods are provided to get the errors under a certain level, and to check if
 * there are errors under a certain level.
 * @param <T> the type of error level provider
 */
public interface ErrorContainer<T extends ErrorLevelProvider> {
	/**
	 * Adds an error to the list of errors.
	 * @param error The error to add.
	 */
	void addError(@NotNull T error);

	/**
	 * Returns a list of all the errors under the exit level defined in this error container.
	 * These are errors that would cause the program to exit.
	 * @return a list of all the errors under the exit level
	 */
	@NotNull List<@NotNull T> getErrorsUnderExitThreshold();

	/**
	 * Returns a list of all the errors under the display level defined in this error container.
	 * These are errors that would be displayed to the user.
	 * @return a list of all the errors under the display level
	 */
	@NotNull List<@NotNull T> getErrorsUnderDisplayThreshold();

	/**
	 * Returns {@code true} if there are errors under the exit level defined in this error container.
	 * @return {@code true} if there are exit errors
	 */
	boolean hasExitErrors();

	/**
	 * Returns {@code true} if there are errors under the display level defined in this error container.
	 * @return {@code true} if there are display errors
	 */
	boolean hasDisplayErrors();


	/**
	 * Specify the error level threshold at which the program should exit.
	 * All errors with a level equal to or higher than this will cause the program to exit.
	 * For example, If this is set to {@link ErrorLevel#WARNING}, then errors including
	 * {@link ErrorLevel#WARNING} and {@link ErrorLevel#ERROR} will cause the program to exit, but
	 * {@link ErrorLevel#INFO} and below will not.
	 * @param level The threshold error level at which the program should exit.
	 * @throws IllegalStateException If the exit threshold is set lower than the display threshold.
	 */
	void setErrorExitThreshold(@NotNull ErrorLevel level);

	/**
	 * Returns the threshold error level that will cause the program to exit.
	 * @return the error exit threshold
	 */
	@NotNull ModifyRecord<@NotNull ErrorLevel> getErrorExitThreshold();

	/**
	 * Specify the error level threshold at which errors should be displayed to the user.
	 * All errors with a level lower than this will be ignored.
	 * For example, If this is set to {@link ErrorLevel#INFO}, then errors including
	 * {@link ErrorLevel#INFO}, {@link ErrorLevel#WARNING}, and {@link ErrorLevel#ERROR}) will be displayed, but
	 * {@link ErrorLevel#DEBUG} will not.
	 * @param level The threshold error level at which errors should be displayed to the user.
	 * @throws IllegalStateException If the display threshold is set lower than the exit threshold.
	 */
	void setErrorDisplayThreshold(@NotNull ErrorLevel level);

	/**
	 * Returns the threshold error level at which errors will be displayed to the user.
	 * @return the error display threshold
	 */
	@NotNull ModifyRecord<@NotNull ErrorLevel> getErrorDisplayThreshold();
}
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
	@NotNull List<@NotNull T> getErrorsUnderExitLevel();

	/**
	 * Returns a list of all the errors under the display level defined in this error container.
	 * These are errors that would be displayed to the user.
	 * @return a list of all the errors under the display level
	 */
	@NotNull List<@NotNull T> getErrorsUnderDisplayLevel();

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
	 * The minimum error level that will cause the program to exit.
	 * All errors with a level equal to or higher than this will cause the program to exit.
	 * For example, if this is set to {@link ErrorLevel#WARNING}, then all errors with a
	 * level of {@link ErrorLevel#WARNING} or {@link ErrorLevel#ERROR} will cause the program to exit.
	 * @param level The minimum error level that will cause the program to exit.
	 * @throws IllegalStateException If the minimum exit error level is higher than the minimum display error level.
	 */
	void setMinimumExitErrorLevel(@NotNull ErrorLevel level);

	/**
	 * Returns the minimum level that errors should have in order to cause the program to exit.
	 * @return the minimum exit error level
	 */
	@NotNull ModifyRecord<@NotNull ErrorLevel> getMinimumExitErrorLevel();

	/**
	 * The minimum error level that will be displayed to the user.
	 * All errors with a level lower than this will be ignored.
	 * For example, If this is set to {@link ErrorLevel#INFO}, then all errors (including
	 * {@link ErrorLevel#INFO}, {@link ErrorLevel#WARNING}, and {@link ErrorLevel#ERROR}) will be displayed, but
	 * {@link ErrorLevel#DEBUG} will not.
	 * @param level The minimum error level that will be displayed to the user.
	 * @throws IllegalStateException If the minimum exit error level is higher than the minimum display error level.
	 */
	void setMinimumDisplayErrorLevel(@NotNull ErrorLevel level);

	/**
	 * Returns the minimum level that errors should have in order to be displayed to the user.
	 * @return the minimum display error level
	 */
	@NotNull ModifyRecord<@NotNull ErrorLevel> getMinimumDisplayErrorLevel();
}
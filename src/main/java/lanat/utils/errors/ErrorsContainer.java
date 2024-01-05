package lanat.utils.errors;

import org.jetbrains.annotations.NotNull;
import utils.ModifyRecord;

import java.util.List;

/**
 * Represents a container of errors. Methods are provided to get the errors under a certain level, and to check if
 * there are errors under a certain level.
 * @param <T> the type of error level provider
 */
public interface ErrorsContainer<T extends ErrorLevelProvider> {
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
	 * Sets the minimum level that errors should have in order to cause the program to exit.
	 * @param level the minimum exit error level
	 */
	void setMinimumExitErrorLevel(@NotNull ErrorLevel level);

	/**
	 * Returns the minimum level that errors should have in order to cause the program to exit.
	 * @return the minimum exit error level
	 */
	@NotNull ModifyRecord<@NotNull ErrorLevel> getMinimumExitErrorLevel();

	/**
	 * Sets the minimum level that errors should have in order to be displayed to the user.
	 * @param level the minimum display error level
	 */
	void setMinimumDisplayErrorLevel(@NotNull ErrorLevel level);

	/**
	 * Returns the minimum level that errors should have in order to be displayed to the user.
	 * @return the minimum display error level
	 */
	@NotNull ModifyRecord<@NotNull ErrorLevel> getMinimumDisplayErrorLevel();
}
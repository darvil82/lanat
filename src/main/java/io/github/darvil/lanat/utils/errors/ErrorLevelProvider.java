package io.github.darvil.lanat.utils.errors;

import org.jetbrains.annotations.NotNull;

/**
 * Interface for classes that have an error level.
 */
public interface ErrorLevelProvider {
	/**
	 * Returns the error level of the object.
	 * @return The error level of the object.
	 */
	@NotNull ErrorLevel getErrorLevel();
}
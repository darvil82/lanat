package lanat.utils;

import org.jetbrains.annotations.NotNull;

/**
 * A builder for a type.
 * @param <T> The type to build.
 */
public interface Builder<T> {
	/**
	 * Builds the object.
	 * @return the built object
	 */
	@NotNull T build();
}
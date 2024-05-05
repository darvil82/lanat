package lanat.utils;

import org.jetbrains.annotations.NotNull;


/**
 * Interface for objects that inherit properties from another object after creation.
 * @param <T> The type of object to inherit properties from.
 */
public interface PostCreationInheritor<T> {
	/**
	 * Inherits certain properties from another object, only if they are not already set to something.
	 * @param object The object to inherit properties from.
	 */
	void inheritProperties(@NotNull T object);
}
package io.github.darvil.lanat.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an object that has a name and a description.
 */
public interface NamedWithDescription {
	/**
	 * Returns the name of this object.
	 *
	 * @return The name of this object
	 */
	@NotNull String getName();

	/**
	 * Returns the description of this object.
	 *
	 * @return The description of this object
	 */
	@Nullable String getDescription();
}
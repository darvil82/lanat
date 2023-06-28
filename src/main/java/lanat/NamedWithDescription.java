package lanat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

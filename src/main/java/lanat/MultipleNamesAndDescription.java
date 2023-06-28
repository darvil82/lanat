package lanat;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public interface MultipleNamesAndDescription extends NamedWithDescription {
	/**
	 * Add one or more names to this object.
	 *
	 * @param names The names to add
	 */
	void addNames(@NotNull String... names);

	/**
	 * Returns all the names of this object. Will always return at least one.
	 *
	 * @return All the names of this object.
	 */
	@NotNull List<@NotNull String> getNames();

	/**
	 * {@inheritDoc} If multiple names are defined, the longest name will be returned.
	 *
	 * @return The name of this object
	 */
	@Override
	default @NotNull String getName() {
		final var names = this.getNames();
		if (names.size() == 1)
			return names.get(0);

		return new ArrayList<>(names) {{
			this.sort(Comparator.comparingInt(String::length).reversed());
		}}.get(0);
	}

	/**
	 * Checks if this object has the given name.
	 *
	 * @param name The name to check
	 * @return {@code true} if this object has the given name, {@code false} otherwise
	 */
	default boolean hasName(String name) {
		return this.getNames().contains(name);
	}

	/**
	 * Sets the description of this object. The description is used to be displayed in the help message.
	 * @param description The description to set
	 */
	void setDescription(@NotNull String description);
}

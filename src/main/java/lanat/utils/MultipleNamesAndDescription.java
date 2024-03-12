package lanat.utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Represents an object that has multiple names and a description.
 */
public interface MultipleNamesAndDescription extends NamedWithDescription {
	/**
	 * Set the names of this object.
	 * @param names The names to set
	 */
	void setNames(@NotNull List<@NotNull String> names);

	/**
	 * Add one or more names to this object.
	 * @param names The names to add
	 */
	default void addNames(@NotNull String... names) {
		var list = new ArrayList<>(this.getNames());
		list.addAll(Arrays.asList(names));
		this.setNames(list);
	}

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

		var newList = new ArrayList<>(names);
		newList.sort(Comparator.comparingInt(String::length).reversed());
		return newList.get(0);
	}

	/**
	 * Checks if this object has the given name.
	 *
	 * @param name The name to check
	 * @return {@code true} if this object has the given name, {@code false} otherwise
	 */
	default boolean hasName(@NotNull String name) {
		return this.getNames().contains(name);
	}

	/**
	 * Sets the description of this object. The description is used to be displayed in the help message.
	 * @param description The description to set
	 */
	void setDescription(@NotNull String description);
}
package lanat;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface MultipleNamesAndDescription extends NamedWithDescription {
	/**
	 * Add one or more names to this object.
	 * @param names The names to add
	 * */
	void addNames(@NotNull String... names);

	/**
	 * Returns all the names of this object. Will always return at least one.
	 * @return All the names of this object.
	 * */
	@NotNull List<@NotNull String> getNames();

	/**
	 * {@inheritDoc} If multiple names are defined, the longest name will be returned.
	 * @return The name of this object
	 * */
	@Override
	default @NotNull String getName() {
		final var names = this.getNames();
		if (names.size() == 1)
			return names.get(0);

		return new ArrayList<>(this.getNames()) {{
			this.sort((a, b) -> b.length() - a.length());
		}}.get(0);
	}

	/**
	 * Checks if this object has the given name.
	 * @param name The name to check
	 * @return <code>true</code> if this object has the given name, <code>false</code> otherwise
	 * */
	default boolean hasName(String name) {
		return this.getNames().contains(name);
	}
}

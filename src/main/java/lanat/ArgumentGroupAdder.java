package lanat;

import lanat.exceptions.ArgumentGroupAlreadyExistsException;
import lanat.exceptions.ArgumentGroupNotFoundException;
import lanat.utils.UtlMisc;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ArgumentGroupAdder extends NamedWithDescription {
	/**
	 * Adds an argument group to this element.
	 */
	void addGroup(@NotNull ArgumentGroup group);

	@NotNull List<@NotNull ArgumentGroup> getGroups();

	default void checkUniqueGroups() {
		UtlMisc.requireUniqueElements(this.getGroups(), g -> new ArgumentGroupAlreadyExistsException(g, this));
	}

	default boolean hasGroup(@NotNull String name) {
		for (final var group : this.getGroups()) {
			if (group.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	default @NotNull ArgumentGroup getGroup(@NotNull String name) {
		for (final var group : this.getGroups()) {
			if (group.getName().equals(name)) {
				return group;
			}
		}
		throw new ArgumentGroupNotFoundException(name);
	}
}

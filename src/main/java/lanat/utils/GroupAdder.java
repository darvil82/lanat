package lanat.utils;

import lanat.Group;
import lanat.exceptions.GroupAlreadyExistsException;
import lanat.exceptions.GroupNotFoundException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An interface for objects that can add {@link Group}s to themselves.
 */
public interface GroupAdder extends NamedWithDescription {
	/**
	 * Adds an argument group to this container.
	 * @param group the argument group to be added
	 */
	void addGroup(@NotNull Group group);

	/**
	 * Returns a list of the argument groups in this container.
	 * @return an immutable list of the argument groups in this container
	 */
	@NotNull List<@NotNull Group> getGroups();

	/**
	 * Checks that all the argument groups in this container are unique.
	 * @throws GroupAlreadyExistsException if there are two argument groups with the same name
	 */
	default void checkUniqueGroups() {
		UtlMisc.requireUniqueElements(this.getGroups(), g -> new GroupAlreadyExistsException(g, this));
	}

	/**
	 * Checks if this container has an argument group with the given name.
	 * @param name the name of the argument group
	 * @return {@code true} if this container has an argument group with the given name, {@code false} otherwise
	 */
	default boolean hasGroup(@NotNull String name) {
		for (final var group : this.getGroups()) {
			if (group.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the argument group with the given name.
	 * @param name the name of the argument group
	 * @return the argument group with the given name
	 * @throws GroupNotFoundException if there is no argument group with the given name
	 */
	default @NotNull Group getGroup(@NotNull String name) {
		for (final var group : this.getGroups()) {
			if (group.getName().equals(name)) {
				return group;
			}
		}
		throw new GroupNotFoundException(name);
	}
}
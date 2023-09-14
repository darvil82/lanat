package lanat;

import lanat.exceptions.ArgumentGroupAlreadyExistsException;
import lanat.exceptions.ArgumentGroupNotFoundException;
import lanat.utils.UtlMisc;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An interface for objects that can add {@link ArgumentGroup}s to themselves.
 */
public interface ArgumentGroupAdder extends NamedWithDescription {
	/**
	 * Adds an argument group to this container.
	 * @param group the argument group to be added
	 */
	void addGroup(@NotNull ArgumentGroup group);

	/**
	 * Returns a list of the argument groups in this container.
	 * @return an immutable list of the argument groups in this container
	 */
	@NotNull List<@NotNull ArgumentGroup> getGroups();

	/**
	 * Checks that all the argument groups in this container have unique names.
	 * @throws ArgumentGroupAlreadyExistsException if there are two argument groups with the same name
	 */
	default void checkUniqueGroups() {
		UtlMisc.requireUniqueElements(this.getGroups(), g -> new ArgumentGroupAlreadyExistsException(g, this));
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
	 * @throws ArgumentGroupNotFoundException if there is no argument group with the given name
	 */
	default @NotNull ArgumentGroup getGroup(@NotNull String name) {
		for (final var group : this.getGroups()) {
			if (group.getName().equals(name)) {
				return group;
			}
		}
		throw new ArgumentGroupNotFoundException(name);
	}
}

package lanat;

import lanat.exceptions.ArgumentGroupNotFoundException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ArgumentGroupAdder {
	/**
	 * Adds an argument group to this element.
	 */
	void addGroup(@NotNull ArgumentGroup group);

	@NotNull List<@NotNull ArgumentGroup> getGroups();

	default @NotNull ArgumentGroup getGroup(@NotNull String name) {
		for (final var group : this.getGroups()) {
			if (group.getName().equals(name)) {
				return group;
			}
		}
		throw new ArgumentGroupNotFoundException(name);
	}
}

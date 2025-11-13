package io.github.darvil.lanat.exceptions;

import io.github.darvil.lanat.Group;
import org.jetbrains.annotations.NotNull;

/** Thrown when an {@link Group} is not found. */
public class GroupNotFoundException extends ObjectNotFoundException {
	public GroupNotFoundException(@NotNull String name) {
		super("Group", name);
	}

	public GroupNotFoundException(@NotNull Group group) {
		super("Group", group);
	}
}
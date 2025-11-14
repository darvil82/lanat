package io.github.darvil.lanat.exceptions;

import io.github.darvil.lanat.Command;
import io.github.darvil.lanat.utils.NamedWithDescription;
import org.jetbrains.annotations.NotNull;

/** Thrown when a {@link Command} is not found. */
public class CommandNotFoundException extends ObjectNotFoundException {
	public CommandNotFoundException(@NotNull String name) {
		super("Command", name);
	}

	public CommandNotFoundException(@NotNull NamedWithDescription name, @NotNull NamedWithDescription container) {
		super("Command", name, container);
	}

	public CommandNotFoundException(@NotNull NamedWithDescription name) {
		super("Command", name);
	}
}
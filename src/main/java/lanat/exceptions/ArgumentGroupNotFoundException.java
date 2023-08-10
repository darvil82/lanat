package lanat.exceptions;

import lanat.ArgumentGroup;
import org.jetbrains.annotations.NotNull;

/** Thrown when an {@link ArgumentGroup} is not found. */
public class ArgumentGroupNotFoundException extends ObjectNotFoundException {
	public ArgumentGroupNotFoundException(@NotNull String name) {
		super("Group", name);
	}

	public ArgumentGroupNotFoundException(@NotNull ArgumentGroup group) {
		super("Group", group);
	}
}

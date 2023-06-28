package lanat.exceptions;

import lanat.ArgumentGroup;
import org.jetbrains.annotations.NotNull;

public class ArgumentGroupNotFoundException extends ObjectNotFoundException {
	public ArgumentGroupNotFoundException(@NotNull String name) {
		super("Group", name);
	}

	public ArgumentGroupNotFoundException(@NotNull ArgumentGroup group) {
		super("Group", group);
	}
}

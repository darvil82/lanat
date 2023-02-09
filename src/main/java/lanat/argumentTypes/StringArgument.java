package lanat.argumentTypes;

import lanat.ArgumentType;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;

public class StringArgument extends ArgumentType<String> {
	@Override
	public String parseValues(@NotNull String @NotNull [] args) {
		return args[0];
	}

	@Override
	public @NotNull TextFormatter getRepresentation() {
		return new TextFormatter("string");
	}
}

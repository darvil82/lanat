package lanat.argumentTypes;

import lanat.ArgumentType;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringArgumentType extends ArgumentType<String> {
	@Override
	public String parseValues(@NotNull String @NotNull [] args) {
		return args[0];
	}

	@Override
	public @NotNull TextFormatter getRepresentation() {
		return new TextFormatter("string");
	}

	@Override
	public @Nullable String getDescription() {
		return "A string of characters.";
	}
}

package lanat.argumentTypes;

import lanat.ArgumentType;
import org.jetbrains.annotations.NotNull;
import textFormatter.TextFormatter;

/**
 * An argument type that takes a string of characters.
 * @see String
 */
public final class StringArgumentType extends ArgumentType<String> {
	@Override
	public String parseValues(@NotNull String @NotNull [] args) {
		return args[0];
	}

	@Override
	public @NotNull TextFormatter getRepresentation() {
		return new TextFormatter("string");
	}

	@Override
	public @NotNull String getDescription() {
		return "A string of characters.";
	}
}
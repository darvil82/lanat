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
	public String parseValues(@NotNull String @NotNull [] values) {
		return values[0];
	}

	@Override
	public @NotNull TextFormatter getRepresentation() {
		return TextFormatter.of("string");
	}

	@Override
	public @NotNull String getDescription() {
		return "A string of characters.";
	}
}
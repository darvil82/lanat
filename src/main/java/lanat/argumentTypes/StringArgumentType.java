package lanat.argumentTypes;

import io.github.darvil.terminal.textformatter.TextFormatter;
import lanat.ArgumentType;
import org.jetbrains.annotations.NotNull;

/**
 * An argument type that takes a string of characters.
 * @see String
 */
// do not allow extending this type because it has some specific behavior defined in TupleArgumentType
public final class StringArgumentType extends ArgumentType<String> {
	@Override
	public String parseValues(@NotNull String @NotNull [] values) {
		// when used in a TupleArgumentType, this is never called. (see TupleArgumentType)
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
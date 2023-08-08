package lanat.argumentTypes;

import lanat.ArgumentType;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An argument type that takes an integer number.
 */
public class IntegerArgumentType extends ArgumentType<Integer> {
	@Override
	public Integer parseValues(String @NotNull [] args) {
		try {
			return Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			this.addError("Invalid integer value: '" + args[0] + "'.");
			return null;
		}
	}

	@Override
	public @NotNull TextFormatter getRepresentation() {
		return new TextFormatter("int");
	}

	@Override
	public @Nullable String getDescription() {
		return "An integer number.";
	}
}
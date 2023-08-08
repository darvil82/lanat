package lanat.argumentTypes;

import lanat.ArgumentType;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An argument type that takes a short integer number.
 */
public class ShortArgumentType extends ArgumentType<Short> {
	@Override
	public Short parseValues(@NotNull String @NotNull [] args) {
		try {
			return Short.parseShort(args[0]);
		} catch (NumberFormatException e) {
			this.addError("Invalid short value: '" + args[0] + "'.");
			return null;
		}
	}

	@Override
	public @NotNull TextFormatter getRepresentation() {
		return new TextFormatter("short");
	}

	@Override
	public @Nullable String getDescription() {
		return "An integer number (-32,768 to 32,767)";
	}
}
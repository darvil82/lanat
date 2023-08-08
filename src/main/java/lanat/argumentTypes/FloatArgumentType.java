package lanat.argumentTypes;

import lanat.ArgumentType;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An argument type that takes a floating point number.
 */
public class FloatArgumentType extends ArgumentType<Float> {
	@Override
	public Float parseValues(@NotNull String @NotNull [] args) {
		try {
			return Float.parseFloat(args[0]);
		} catch (NumberFormatException e) {
			this.addError("Invalid float value: '" + args[0] + "'.");
			return null;
		}
	}

	@Override
	public @NotNull TextFormatter getRepresentation() {
		return new TextFormatter("float");
	}

	@Override
	public @Nullable String getDescription() {
		return "A floating point number.";
	}
}
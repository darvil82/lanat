package lanat.argumentTypes;

import lanat.ArgumentType;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DoubleArgumentType extends ArgumentType<Double> {
	@Override
	public Double parseValues(@NotNull String @NotNull [] args) {
		try {
			return Double.parseDouble(args[0]);
		} catch (NumberFormatException e) {
			this.addError("Invalid double value: '" + args[0] + "'.");
			return null;
		}
	}

	@Override
	public @NotNull TextFormatter getRepresentation() {
		return new TextFormatter("double");
	}

	@Override
	public @Nullable String getDescription() {
		return "A high precision floating point number.";
	}
}
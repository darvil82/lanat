package lanat.argumentTypes;

import lanat.ArgumentType;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An argument type that takes a long integer number.
 */
public class LongArgumentType extends ArgumentType<Long> {
	@Override
	public Long parseValues(@NotNull String @NotNull [] args) {
		try {
			return Long.parseLong(args[0]);
		} catch (NumberFormatException e) {
			this.addError("Invalid long value: '" + args[0] + "'.");
			return null;
		}
	}

	@Override
	public @NotNull TextFormatter getRepresentation() {
		return new TextFormatter("long");
	}

	@Override
	public @Nullable String getDescription() {
		return "A large integer number.";
	}
}
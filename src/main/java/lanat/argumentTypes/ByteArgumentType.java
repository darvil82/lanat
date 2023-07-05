package lanat.argumentTypes;

import lanat.ArgumentType;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ByteArgumentType extends ArgumentType<Byte> {
	@Override
	public Byte parseValues(@NotNull String @NotNull [] args) {
		try {
			return Byte.parseByte(args[0]);
		} catch (NumberFormatException e) {
			this.addError("Invalid byte value: '" + args[0] + "'.");
			return null;
		}
	}

	@Override
	public @NotNull TextFormatter getRepresentation() {
		return new TextFormatter("byte");
	}

	@Override
	public @Nullable String getDescription() {
		return "A small integer value. (-128 to 127)";
	}
}
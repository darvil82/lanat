package lanat.utils.displayFormatter;

import org.jetbrains.annotations.NotNull;

public enum FormatOption {
	RESET_ALL(0),
	BOLD(1),
	ITALIC(3),
	DIM(2),
	UNDERLINE(4),
	BLINK(5),
	REVERSE(7),
	HIDDEN(8),
	STRIKE_THROUGH(9);

	private final @NotNull Byte value;

	FormatOption(int value) {
		this.value = (byte)value;
	}

	@Override
	public @NotNull String toString() {
		return TextFormatter.getSequence(this.value);
	}

	public @NotNull String toStringReset() {
		return TextFormatter.getSequence(this.value + 20 + (this == BOLD ? 1 : 0));
	}
}
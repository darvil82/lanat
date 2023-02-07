package lanat.utils.displayFormatter;

import org.jetbrains.annotations.NotNull;

public enum Color {
	BLACK(30),
	RED(31),
	GREEN(32),
	YELLOW(33),
	BLUE(34),
	MAGENTA(35),
	CYAN(36),
	WHITE(37),
	GRAY(90),
	BRIGHT_RED(91),
	BRIGHT_GREEN(92),
	BRIGHT_YELLOW(93),
	BRIGHT_BLUE(94),
	BRIGHT_MAGENTA(95),
	BRIGHT_CYAN(96),
	BRIGHT_WHITE(97);

	private final @NotNull Byte value;

	Color(int value) {
		this.value = (byte)value;
	}

	@Override
	public @NotNull String toString() {
		return TextFormatter.getSequence(this.value);
	}

	public @NotNull String toStringBackground() {
		return TextFormatter.getSequence(this.value + 10);
	}

	public static @NotNull Color @NotNull [] getBrightColors() {
		return new Color[] {
			BRIGHT_RED,
			BRIGHT_GREEN,
			BRIGHT_YELLOW,
			BRIGHT_BLUE,
			BRIGHT_MAGENTA,
			BRIGHT_CYAN,
			BRIGHT_WHITE
		};
	}

	public static @NotNull Color @NotNull [] getDarkColors() {
		return new Color[] {
			RED,
			GREEN,
			YELLOW,
			BLUE,
			MAGENTA,
			CYAN,
			WHITE
		};
	}
}
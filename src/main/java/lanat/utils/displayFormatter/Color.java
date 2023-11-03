package lanat.utils.displayFormatter;

import org.jetbrains.annotations.NotNull;

/**
 * Enumerates the ANSI color codes that a terminal can normally display.
 */
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

	private final byte value;

	Color(int value) {
		this.value = (byte)value;
	}

	/**
	 * Returns the ANSI escape sequence for this color for the text foreground.
	 * @return The ANSI escape sequence for this color.
	 */
	public @NotNull String fg() {
		return TextFormatter.getSequence(this.value);
	}

	/**
	 * Returns the ANSI escape sequence for this color for the text background.
	 * @return The ANSI escape sequence for this color.
	 */
	public @NotNull String bg() {
		return TextFormatter.getSequence(this.value + 10);
	}

	/**
	 * Returns the ANSI escape sequence for this color for the text foreground.
	 * @return The ANSI escape sequence for this color.
	 * @see Color#fg()
	 */
	@Override
	public String toString() {
		return this.fg();
	}

	/**
	 * Immutable list of all the dark colors.
	 */
	public static final @NotNull Color[] BRIGHT_COLORS = {
		BRIGHT_RED,
		BRIGHT_GREEN,
		BRIGHT_YELLOW,
		BRIGHT_BLUE,
		BRIGHT_MAGENTA,
		BRIGHT_CYAN,
		BRIGHT_WHITE
	};

	/**
	 * Immutable list of all the bright colors.
	 */
	public static final @NotNull Color[] DARK_COLORS = {
		RED,
		GREEN,
		YELLOW,
		BLUE,
		MAGENTA,
		CYAN,
		WHITE
	};
}
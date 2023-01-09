package argparser.utils.displayFormatter;

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

	private final Byte value;

	Color(int value) {
		this.value = (byte)value;
	}

	@Override
	public String toString() {
		return TextFormatter.ESC + String.format("[%dm", this.value);
	}

	public String toStringBackground() {
		return TextFormatter.ESC + String.format("[%dm", this.value + 10);
	}

	public static Color[] getBrightColors() {
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

	public static Color[] getDarkColors() {
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
package argparser.displayFormatter;

public enum Color {
	Black(30),
	Red(31),
	Green(32),
	Yellow(33),
	Blue(34),
	Magenta(35),
	Cyan(36),
	White(37),
	Gray(90),
	BrightRed(91),
	BrightGreen(92),
	BrightYellow(93),
	BrightBlue(94),
	BrightMagenta(95),
	BrightCyan(96),
	BrightWhite(97);

	private Byte value;

	Color(int value) {
		this.value = (byte)value;
	}

	@Override
	public String toString() {
		return String.format("\033[%dm", this.value);
	}

	public String toStringBackground() {
		return String.format("\033[%dm", this.value + 10);
	}
}
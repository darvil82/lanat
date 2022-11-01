package argparser.displayFormatter;

public enum FormatOption {
	ResetAll(0),
	Bold(1),
	Dim(2),
	Underline(4),
	Blink(5),
	Reverse(7),
	Hidden(8),
	StrikeThrough(9);

	private Byte value;

	FormatOption(int value) {
		this.value = (byte)value;
	}

	@Override
	public String toString() {
		return String.format("\033[%dm", this.value);
	}

	public String toStringReset() {
		return String.format("\033[%dm", this.value + 20 + (this == Bold ? 1 : 0));
	}
}
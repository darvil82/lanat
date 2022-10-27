package argparser.displayFormatter;

public class TerminalDisplayer {
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
		BrightWhite(97),
		None(255);

		public final byte value;

		Color(int value) {
			this.value = (byte)value;
		}

		public byte asBackground() {
			return (byte)(this.value + 10);
		}
	}

	public interface Colorable {
		Color getColor();
	}
}

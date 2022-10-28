package argparser.displayFormatter;

public class TerminalDisplayer {
	public static void display(FormattingProvider value) {
		System.out.println(value.getFormattingSequence());
	}

	public static void display(Iterable<? extends FormattingProvider> values, String separator) {
		FormattingProvider fp = () -> {
			StringBuilder sb = new StringBuilder();
			for (var v : values) {
				sb.append(v.getFormattingSequence()).append(separator);
			}
			return sb.toString();
		};
		TerminalDisplayer.display(fp);
	}

	public enum Color implements FormattingProvider {
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
		None(128);

		private Byte value;

		Color(int value) {
			this.value = (byte)value;
		}

		public Color asBackground() {
			// prevent overflow of the max value
			this.value = (byte)(value == (byte)128 ? 128 : (this.value + 10));
			return this;
		}

		public String getFormattingSequence() {
			return String.format("\033[%dm", this.value);
		}
	}

	public interface Colorable {
		Color getColor();
	}

	public interface FormattingProvider {
		String getFormattingSequence();
	}

	public static final String CLEAR_FORMAT = "\033[0m";
}

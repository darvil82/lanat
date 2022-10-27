package argparser.displayFormatter;

import argparser.Token;
import argparser.utils.UtlString;

import java.util.ArrayList;

public class TerminalDisplayer {
	public static void displayTokens(ArrayList<Token> tokens) {
		String result = String.join(" ",
			tokens.stream()
				.map(t -> {
					var contents = t.contents();
					if (contents.contains(" ")) {
						contents = UtlString.wrap(contents, "'");
					}
					return t.getColorSequence() + contents + TerminalDisplayer.CLEAR_FORMAT;
				})
				.toList()
		);
		System.out.println(result);
	}

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

		public String toSequence() {
			return String.format("\033[%dm", this.value);
		}
	}

	public interface Colorable {
		Color getColor();
	}

	private static final String CLEAR_FORMAT = "\033[0m";
}

package argparser.displayFormatter;

public interface TextFormatter {
	/**
	 * For getting the parsed formatting stuff from normally a TextFormatter
	 */
	interface FormattingProvider {
		/**
		 * Return the parsed terminal sequences
		 */
		String getFormattingSequence();
	}

	FormattingProvider getFormatting();

	static FormattingProvider format(FormattingProvider... formattingProviders) {
		return format("", formattingProviders);
	}

	static FormattingProvider format(String separator, FormattingProvider... formattingProviders) {
		return () -> {
			StringBuilder sb = new StringBuilder();
			for (var fp : formattingProviders) {
				sb.append(fp.getFormattingSequence()).append(separator);
			}
			return sb.toString();
		};
	}

	static FormattingProvider format(Iterable<? extends TextFormatter> formattingProviders) {
		return format("", formattingProviders);
	}

	static FormattingProvider format(String separator, Iterable<? extends TextFormatter> formattingProviders) {
		return () -> {
			StringBuilder sb = new StringBuilder();
			for (var fp : formattingProviders) {
				sb.append(fp.getFormatting().getFormattingSequence()).append(separator);
			}
			return sb.toString();
		};
	}

	static FormattingProvider format(String s) {
		return () -> s;
	}

	static void print(FormattingProvider formattingProvider) {
		System.out.println(format(formattingProvider, FormatOption.ResetAll).getFormattingSequence());
	}

	enum Color implements FormattingProvider {
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

		@Override
		public String getFormattingSequence() {
			return String.format("\033[%dm", this.value);
		}
	}


	enum FormatOption implements FormattingProvider {
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

		public FormatOption reset() {
			this.value = (byte)(this.value + 20);
			return this;
		}

		@Override
		public String getFormattingSequence() {
			return String.format("\033[%dm", this.value);
		}
	}
}
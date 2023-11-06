package lanat.utils.displayFormatter;

import org.jetbrains.annotations.NotNull;

/**
 * Enumerates the ANSI format codes that a terminal can normally display.
 * <p>
 * Please note that compatibility with all terminals may vary.
 * </p>
 */
public enum FormatOption {
	/** Resets all formatting. Including colors. */
	RESET_ALL(0),
	BOLD(1),
	/** Makes the text dimmer. */
	DIM(2),
	ITALIC(3),
	UNDERLINE(4),
	/** Makes the text blink. */
	BLINK(5),
	/** Reverses the foreground and background colors. */
	REVERSE(7),
	/** Hides the text. */
	HIDDEN(8),
	STRIKE_THROUGH(9);

	private final byte value;

	FormatOption(int value) {
		this.value = (byte)value;
	}

	/**
	 * Returns the ANSI escape sequence for this format option.
	 * @return The ANSI escape sequence for this format option.
	 * @see FormatOption#seq()
	 */
	@Override
	public @NotNull String toString() {
		return this.seq();
	}

	/**
	 * Returns the ANSI escape sequence for this format option.
	 * @return The ANSI escape sequence for this format option.
	 */
	public @NotNull String seq() {
		return TextFormatter.getSequence(this.value);
	}

	/**
	 * Returns the ANSI escape sequence which resets the formatting of this option.
	 * @return The ANSI escape sequence which resets the formatting of this option.
	 */
	public @NotNull String reset() {
		// for some reason, bold is 21 instead of 20
		return TextFormatter.getSequence(
			this == RESET_ALL
				? this.value // RESET_ALL should be the same when resetting
				: this.value + 20 + (this == BOLD ? 1 : 0)
		);
	}
}
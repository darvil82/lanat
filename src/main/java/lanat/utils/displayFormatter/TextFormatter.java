package lanat.utils.displayFormatter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextFormatter {
	public static boolean enableSequences = true, debug = false;
	private final @NotNull ArrayList<FormatOption> formatOptions = new ArrayList<>();
	private final @NotNull List<TextFormatter> concatList = new ArrayList<>();
	private @Nullable TextFormatter parent;
	private @Nullable Color foregroundColor;
	private @Nullable Color backgroundColor;
	private @NotNull String contents;

	public TextFormatter(@NotNull String contents) {
		this.contents = contents;
	}

	public TextFormatter(@NotNull String contents, @Nullable Color foreground) {
		this(contents);
		this.foregroundColor = foreground;
	}

	public TextFormatter() {
		this.contents = "";
	}

	public TextFormatter addFormat(@NotNull FormatOption... options) {
		this.formatOptions.addAll(Arrays.asList(options));
		return this;
	}

	public TextFormatter setColor(@Nullable Color foreground) {
		this.foregroundColor = foreground;
		return this;
	}

	public TextFormatter setColor(@Nullable Color foreground, @Nullable Color background) {
		this.foregroundColor = foreground;
		this.backgroundColor = background;
		return this;
	}

	public TextFormatter setBackgroundColor(@Nullable Color background) {
		this.backgroundColor = background;
		return this;
	}

	public TextFormatter setContents(@NotNull String contents) {
		this.contents = contents;
		return this;
	}

	public TextFormatter concat(@NotNull TextFormatter... formatters) {
		for (TextFormatter formatter : formatters) {
			// if it was already added to another formatter, remove it from there
			if (formatter.parent != null) {
				formatter.parent.concatList.remove(formatter);
			}
			formatter.parent = this;
			this.concatList.add(formatter);
		}
		return this;
	}

	public TextFormatter concat(@NotNull String... strings) {
		for (var str : strings) {
			this.concatList.add(new TextFormatter(str));
		}
		return this;
	}

	public boolean isSimple() {
		return (
			this.contents.length() == 0
				|| this.formattingNotDefined()
				|| !enableSequences
		) && this.concatList.size() == 0; // we cant skip if we need to concat stuff!
	}

	public boolean formattingNotDefined() {
		return (
			this.foregroundColor == null
				&& this.backgroundColor == null
				&& this.formatOptions.isEmpty()
		);
	}

	private @NotNull String getStartSequences() {
		if (this.formattingNotDefined() || !TextFormatter.enableSequences) return "";
		final var buffer = new StringBuilder();

		if (this.foregroundColor != null)
			buffer.append(this.foregroundColor);
		if (this.backgroundColor != null)
			buffer.append(this.backgroundColor.toStringBackground());

		for (var option : this.formatOptions) {
			buffer.append(option);
		}

		return buffer.toString();
	}

	private @NotNull String getEndSequences() {
		if (this.formattingNotDefined() || !TextFormatter.enableSequences) return "";
		final var buffer = new StringBuilder();

		if (this.backgroundColor != null) {
			buffer.append(FormatOption.RESET_ALL);
		} else {
			for (var option : this.formatOptions) {
				buffer.append(option.toStringReset());
			}
			if (this.foregroundColor != null) {
				buffer.append(this.getResetColor());
			}
		}

		return buffer.toString();
	}

	/**
	 * Returns the {@link Color} that should properly reset the foreground color. This is determined by looking at the
	 * parent formatters. If no parent formatter has a foreground color, then {@link Color#BRIGHT_WHITE} is returned.
	 */
	private @NotNull Color getResetColor() {
		var parent = this.parent;
		while (parent != null) {
			if (parent.foregroundColor != null) {
				return parent.foregroundColor;
			}
			parent = parent.parent;
		}
		return Color.BRIGHT_WHITE;
	}

	/**
	 * Creates a new {@link String} with the contents and all the formatting applied.
	 */
	@Override
	public @NotNull String toString() {
		if (this.isSimple()) {
			return this.contents;
		}

		final var buffer = new StringBuilder();

		buffer.append(this.getStartSequences());
		buffer.append(this.contents);

		for (TextFormatter subFormatter : this.concatList) {
			buffer.append(subFormatter);
		}

		buffer.append(this.getEndSequences());

		return buffer.toString();
	}

	/** Returns a template for a {@link TextFormatter} that is used for errors */
	public static @NotNull TextFormatter ERROR(@NotNull String msg) {
		return new TextFormatter(msg).setColor(Color.BRIGHT_RED).addFormat(FormatOption.REVERSE, FormatOption.BOLD);
	}

	public static @NotNull String getSequence(int code) {
		if (TextFormatter.debug)
			return "ESC[" + code;
		return "" + ESC + '[' + code + 'm';
	}

	/**
	 * Escape character which represents the start of a terminal sequence
	 */
	public static final char ESC = '\u001B';
}
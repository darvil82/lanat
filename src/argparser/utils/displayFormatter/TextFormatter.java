package argparser.utils.displayFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TextFormatter {
	public static boolean enableSequences = true;
	private final ArrayList<FormatOption> formatOptions = new ArrayList<>();
	private final List<TextFormatter> concatList = new ArrayList<>();
	private Color foregroundColor;
	private Color backgroundColor;
	private String contents;

	public TextFormatter(String contents) {
		this.contents = contents;
	}

	public TextFormatter() {
		this.contents = "";
	}

	public TextFormatter addFormat(FormatOption... options) {
		this.formatOptions.addAll(Arrays.asList(options));
		return this;
	}

	public TextFormatter setColor(Color foreground) {
		this.foregroundColor = foreground;
		return this;
	}

	public TextFormatter setColor(Color foreground, Color background) {
		this.foregroundColor = foreground;
		this.backgroundColor = background;
		return this;
	}

	public TextFormatter setBackgroundColor(Color background) {
		this.backgroundColor = background;
		return this;
	}

	public TextFormatter setContents(String contents) {
		this.contents = contents;
		return this;
	}

	public TextFormatter concat(TextFormatter... formatters) {
		for (TextFormatter formatter : formatters) {
			if (formatter.foregroundColor == null) {
				formatter.foregroundColor = this.foregroundColor;
			}
			this.concatList.add(formatter);
		}
		return this;
	}

	public TextFormatter concat(String... strings) {
		for (var str : strings) {
			this.concatList.add(new TextFormatter(str));
		}
		return this;
	}

	public boolean isSimple() {
		return (
			this.contents.length() == 0
			|| !formattingDefined()
			|| !enableSequences
		) && this.concatList.size() == 0; // we cant skip if we need to concat stuff!
	}

	public boolean formattingDefined() {
		return (
			this.formatOptions.size() > 0
			|| this.foregroundColor != null
			|| this.backgroundColor != null
		);
	}

	private String getStartSequences() {
		if (!formattingDefined()) return "";
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

	private String getEndSequences() {
		if (!formattingDefined()) return "";
		final var buffer = new StringBuilder();

		if (this.backgroundColor != null) {
			buffer.append(FormatOption.RESET_ALL);
		} else {
			for (var option : this.formatOptions) {
				buffer.append(option.toStringReset());
			}
			if (this.foregroundColor != null)
				buffer.append(Color.BRIGHT_WHITE);
		}

		return buffer.toString();
	}

	private void forwardFormattingToChildren() {
		for (var child : this.concatList) {
			if (child.foregroundColor == null) {
				child.foregroundColor = this.foregroundColor;
			}
			if (child.backgroundColor == null) {
				child.backgroundColor = this.backgroundColor;
			}
			if (child.formatOptions.isEmpty()) {
				child.formatOptions.addAll(this.formatOptions);
			}
			child.forwardFormattingToChildren();
		}
	}

	@Override
	public String toString() {
		// TODO:
		//  There's an issue regarding color resetting when there are multiple nesting levels
		//  of formatters. If a formatter has no formatting defined, but its children do, then sequences
		//  will not be reset properly.
		//  For now I just pass the formatting to the children, but this is not ideal.
		this.forwardFormattingToChildren();

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

	public static TextFormatter ERROR(String msg) {
		return new TextFormatter(msg).setColor(Color.BRIGHT_RED).addFormat(FormatOption.REVERSE, FormatOption.BOLD);
	}
}
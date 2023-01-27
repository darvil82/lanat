package argparser.helpRepresentation;

import argparser.Command;
import argparser.utils.UtlString;

import java.util.function.Function;
import java.util.function.Supplier;

public class LayoutItem {
	private byte indentCount = 0;
	private String title;
	private int marginTop, marginBottom;
	private final Function<Command, String> layoutGenerator;

	public LayoutItem(Function<Command, String> layoutGenerator) {
		this.layoutGenerator = layoutGenerator;
	}

	public LayoutItem(Supplier<String> layoutGenerator) {
		this((c) -> layoutGenerator.get());
	}

	public LayoutItem(String content) {
		this((c) -> content);
	}

	public LayoutItem indent(int indent) {
		this.indentCount = (byte) Math.max(indent, 0);
		return this;
	}

	public LayoutItem marginTop(int marginTop) {
		this.marginTop = Math.max(marginTop, 0);
		return this;
	}

	public LayoutItem marginBottom(int marginBottom) {
		this.marginBottom = Math.max(marginBottom, 0);
		return this;
	}

	public LayoutItem margin(int margin) {
		this.marginTop(margin);
		return this.marginBottom(margin);
	}

	public LayoutItem title(String title) {
		this.title = title;
		return this;
	}

	public String generate(HelpFormatter helpFormatter) {
		final var content = this.layoutGenerator.apply(helpFormatter.parentCmd);
		return content == null ? null : (
			"\n".repeat(this.marginTop)
				+ (this.title == null ? "" : this.title + "\n\n")
				+ UtlString.indent(UtlString.trim(content), this.indentCount * helpFormatter.getIndentSize())
				+ "\n".repeat(this.marginBottom)
		);

	}
}
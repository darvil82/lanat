package argparser.helpRepresentation;

import argparser.Command;
import argparser.utils.UtlString;

import java.util.function.Function;

public class LayoutItem {
	private byte indentCount = 0;
	private int marginTop, marginBottom;
	private final Function<Command, String> layoutGenerator;

	public LayoutItem(Function<Command, String> layoutGenerator) {
		this.layoutGenerator = layoutGenerator;
	}

	public LayoutItem indent(int indent) {
		this.indentCount = (byte)Math.max(indent, 0);
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

	public String generate(HelpFormatter helpFormatter) {
		return "\n".repeat(this.marginTop)
				+ UtlString.indent(
				UtlString.trim(this.layoutGenerator.apply(helpFormatter.parentCmd)),
				this.indentCount * helpFormatter.getIndentSize()
		)
				+ "\n".repeat(this.marginBottom);
	}
}
package argparser;

import argparser.displayFormatter.FormatOption;
import argparser.displayFormatter.TextFormatter;
import argparser.utils.UtlString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class HelpFormatter {
	private Command parentCmd;
	private byte indentSize = 3;
	public static short lineWrapMax = 150;
	private ArrayList<LayoutItem> layout = new ArrayList<>();
	public static boolean debugLayout = false;

	HelpFormatter(Command parentCmd) {
		this.parentCmd = parentCmd;
		this.setLayout();
	}

	// the user can create a helpFormatter, though, the parentCmd should be applied later (otherwise stuff will fail)
	public HelpFormatter() {
		this.setLayout();
	}

	HelpFormatter(HelpFormatter other) {
		this.parentCmd = other.parentCmd;
		this.indentSize = other.indentSize;
		this.layout.addAll(other.layout);
	}

	void setParentCmd(Command parentCmd) {
		this.parentCmd = parentCmd;
	}

	protected List<LayoutItem> getLayout() {
		return layout;
	}

	public void setLayout() {
		this.changeLayout(
			new LayoutItem(LayoutGenerators::title).marginBottom(1),
			new LayoutItem(LayoutGenerators::argumentsUsage).indent(1)
		);
	}

	protected final void addToLayout(LayoutItem... layoutItems) {
		this.layout.addAll(Arrays.asList(layoutItems));
	}

	protected final void addToLayout(int after, LayoutItem... layoutItems) {
		this.layout.addAll(after, Arrays.asList(layoutItems));
	}

	protected final void changeLayout(LayoutItem... layoutItems) {
		this.layout = new ArrayList<>(Arrays.asList(layoutItems));
	}


	protected static class LayoutGenerators {
		public static String title(Command cmd) {
			return cmd.name + (cmd.description == null ? "" : ": " + cmd.description);
		}

		public static String argumentsUsage(Command cmd) {
			var args = cmd.getArguments();
			if (args.isEmpty()) return "";
			var buffer = new StringBuilder();
			for (var arg : args) {
				var repr = arg.argType.getRepresentation();
				if (repr != null)
					buffer.append(repr).append(' ');
			}
			return buffer.toString();
		}
	}

	public class LayoutItem {
		private int indent = 0;
		private int maxTextLineLength = HelpFormatter.lineWrapMax;
		private int marginTop, marginBottom;
		private final Function<Command, String> layoutGenerator;

		public LayoutItem(Function<Command, String> layoutGenerator) {
			this.layoutGenerator = layoutGenerator;
		}

		public LayoutItem indent(int indent) {
			this.indent = indent;
			return this;
		}

		public LayoutItem lineWrapAt(int maxTextLineLength) {
			this.maxTextLineLength = maxTextLineLength;
			return this;
		}

		public LayoutItem marginTop(int marginTop) {
			this.marginTop = marginTop;
			return this;
		}

		public LayoutItem marginBottom(int marginBottom) {
			this.marginBottom = marginBottom;
			return this;
		}

		public LayoutItem margin(int margin) {
			this.marginTop = margin;
			this.marginBottom = margin;
			return this;
		}

		public String generate() {
			return "\n".repeat(this.marginTop) + UtlString.indent(
				UtlString.wrap(
					this.layoutGenerator.apply(HelpFormatter.this.parentCmd),
					this.maxTextLineLength - this.indent
				),
				this.indent * HelpFormatter.this.indentSize
			) + "\n".repeat(this.marginBottom);
		}
	}


	@Override
	public String toString() {
		var buffer = new StringBuilder();

		for (int i = 0; i < this.layout.size(); i++) {
			var generator = this.layout.get(i);
			if (HelpFormatter.debugLayout)
				buffer.append(new TextFormatter("LayoutItem " + i + ":\n").addFormat(FormatOption.UNDERLINE));
			buffer.append(generator.generate()).append('\n');
		}

		return buffer.toString();
	}
}

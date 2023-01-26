package argparser;

import argparser.helpRepresentation.ArgumentGroupRepr;
import argparser.helpRepresentation.ArgumentRepr;
import argparser.utils.displayFormatter.Color;
import argparser.utils.displayFormatter.FormatOption;
import argparser.utils.displayFormatter.TextFormatter;
import argparser.utils.UtlString;

import java.util.*;
import java.util.function.Function;

public class HelpFormatter {
	private Command parentCmd;
	private byte indent = 3;
	public static short lineWrapMax = 110;
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
		this.indent = other.indent;
		this.layout.addAll(other.layout);
	}

	void setParentCmd(Command parentCmd) {
		this.parentCmd = parentCmd;
	}

	public void setIndent(byte indent) {
		this.indent = indent;
	}

	public byte getIndent() {
		return indent;
	}

	protected List<LayoutItem> getLayout() {
		return layout;
	}

	public void setLayout() {
		this.changeLayout(
			new LayoutItem(LayoutGenerators::title),
			new LayoutItem(LayoutGenerators::synopsis).indent(1).margin(1),
			new LayoutItem(LayoutGenerators::argumentDescriptions).indent(1)
		);
	}

	public void moveLayoutItem(int from, int to) {
		if (from < 0 || from >= this.layout.size() || to < 0 || to >= this.layout.size()) {
			throw new IndexOutOfBoundsException("invalid indexes given");
		}

		// same index, nothing to do
		if (from == to)
			return;

		final var item = this.layout.remove(from);
		this.layout.add(to, item);
	}

	protected final void addToLayout(LayoutItem... layoutItems) {
		this.layout.addAll(Arrays.asList(layoutItems));
		Collections.addAll(this.layout, layoutItems);
	}

	protected final void addToLayout(int after, LayoutItem... layoutItems) {
		this.layout.addAll(after, Arrays.asList(layoutItems));
	}

	protected final void changeLayout(LayoutItem... layoutItems) {
		this.layout = new ArrayList<>(Arrays.asList(layoutItems));
	}


	protected static abstract class LayoutGenerators {
		public static String title(Command cmd) {
			return cmd.name + (cmd.description == null ? "" : ": " + cmd.description);
		}

		public static String synopsis(Command cmd, boolean includeHelp) {
			final var args = Argument.sortByPriority(cmd.getArguments());

			if (args.length == 0 && cmd.getSubGroups().length == 0) return "";
			final var buffer = new StringBuilder();

			for (var arg : args) {
				String representation;

				// skip arguments that are in groups (handled later), and help argument if it's not needed
				if (
					arg.getParentGroup() != null
					|| (!includeHelp && arg.isHelpArgument())
					|| (representation = ArgumentRepr.getSynopsisRepresentation(arg)) == null
				)
					continue;

				buffer.append(representation).append(' ');
			}

			for (var group : cmd.getSubGroups()) {
				ArgumentGroupRepr.getRepresentation(group, buffer);
				buffer.append(' ');
			}

			if (!cmd.subCommands.isEmpty()) {
				buffer.append(" {")
					.append(String.join(" | ", cmd.subCommands.stream().map(c -> c.name).toList()))
					.append('}');
			}

			return buffer.toString();
		}

		public static String synopsis(Command cmd) {
			return synopsis(cmd, false);
		}

		public static String heading(String content, char lineChar) {
			return UtlString.center(content, lineWrapMax, lineChar);
		}

		public static String heading(String content) {
			return UtlString.center(content, lineWrapMax);
		}

		public static String argumentDescriptions(Command cmd) {
			final var buff = new StringBuilder();
			final var arguments = Argument.sortByPriority(cmd.getArguments());

			if (arguments.length == 0 && cmd.getSubGroups().length == 0) return "";

			for (var arg : arguments) {
				if (arg.getParentGroup() != null) continue;
				buff.append(ArgumentRepr.getDescriptionRepresentation(arg));
			}

			for (var group : cmd.getSubGroups()) {
				buff.append(ArgumentGroupRepr.getArgumentDescriptions(group));
			}

			return buff.toString();
		}
	}




	public static class LayoutItem {
		private int indent = 0;
		private int lineWrapMax = HelpFormatter.lineWrapMax;
		private int marginTop, marginBottom;
		private final Function<Command, String> layoutGenerator;

		public LayoutItem(Function<Command, String> layoutGenerator) {
			this.layoutGenerator = layoutGenerator;
		}

		public LayoutItem indent(int indent) {
			this.indent = Math.max(indent, 0);
			return this;
		}

		public LayoutItem lineWrapAt(int maxTextLineLength) {
			this.lineWrapMax = Math.max(maxTextLineLength, 0);
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
				this.indent * helpFormatter.indent
				)
				+ "\n".repeat(this.marginBottom);
		}
	}


	@Override
	public String toString() {
		var buffer = new StringBuilder();

		for (int i = 0; i < this.layout.size(); i++) {
			var generator = this.layout.get(i);
			if (HelpFormatter.debugLayout)
				buffer.append(new TextFormatter("LayoutItem " + i + ":\n")
					.addFormat(FormatOption.UNDERLINE)
					.setColor(Color.GREEN)
				);
			buffer.append(UtlString.wrap(generator.generate(this), lineWrapMax)).append('\n');
		}

		return buffer.toString();
	}
}

package argparser.helpRepresentation;

import argparser.Command;
import argparser.ParentCommandGetter;
import argparser.utils.UtlString;
import argparser.utils.displayFormatter.Color;
import argparser.utils.displayFormatter.FormatOption;
import argparser.utils.displayFormatter.TextFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HelpFormatter {
	Command parentCmd;
	private byte indentSize = 3;
	public static short lineWrapMax = 110;
	private ArrayList<LayoutItem> layout = new ArrayList<>();
	public static boolean debugLayout = false;

	public HelpFormatter(Command parentCmd) {
		this.parentCmd = parentCmd;
		this.initLayout();
	}

	// the user can create a helpFormatter, though, the parentCmd should be applied later (otherwise stuff will fail)
	public HelpFormatter() {
		this.initLayout();
	}

	public HelpFormatter(HelpFormatter other) {
		this.parentCmd = other.parentCmd;
		this.indentSize = other.indentSize;
		this.layout.addAll(other.layout);
	}

	public void setParentCmd(Command parentCmd) {
		this.parentCmd = parentCmd;
	}

	public void setIndentSize(int indentSize) {
		this.indentSize = (byte)Math.max(indentSize, 0);
	}

	public byte getIndentSize() {
		return this.indentSize;
	}

	public List<LayoutItem> getLayout() {
		return this.layout;
	}

	protected void initLayout() {
		this.setLayout(
			new LayoutItem(LayoutGenerators::title),
			new LayoutItem(LayoutGenerators::synopsis).indent(1).margin(1),
			new LayoutItem(LayoutGenerators::argumentDescriptions).title("Description:").indent(1),
			new LayoutItem(LayoutGenerators::commandLicense).marginTop(2)
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

	public final void addToLayout(LayoutItem... layoutItems) {
		Collections.addAll(this.layout, layoutItems);
	}

	public final void addToLayout(int at, LayoutItem... layoutItems) {
		this.layout.addAll(at, Arrays.asList(layoutItems));
	}

	public final void setLayout(LayoutItem... layoutItems) {
		this.layout = new ArrayList<>(Arrays.asList(layoutItems));
	}


	@Override
	public String toString() {
		var buffer = new StringBuilder();

		for (int i = 0; i < this.layout.size(); i++) {
			final var generatedContent = this.layout.get(i).generate(this);

			if (generatedContent == null)
				continue;

			if (HelpFormatter.debugLayout)
				buffer.append(new TextFormatter("LayoutItem " + i + ":\n")
					.addFormat(FormatOption.UNDERLINE)
					.setColor(Color.GREEN)
				);

			buffer.append(UtlString.wrap(generatedContent, lineWrapMax)).append('\n');
		}

		return buffer.toString();
	}

	/**
	 * Indents a string by the indent size specified in the {@link HelpFormatter} of the specified {@link Command}.
	 *
	 * @param str the string to indent
	 * @param cmd the {@link Command} that has the {@link HelpFormatter}
	 * @return the indented string
	 */
	public static String indent(String str, Command cmd) {
		return UtlString.indent(str, cmd.getHelpFormatter().getIndentSize());
	}

	/**
	 * Indents a string by the indent size specified in the {@link HelpFormatter}
	 * instance of the {@link Command} this object belongs to.
	 *
	 * @param str the string to indent
	 * @param obj the object that belongs to the {@link Command} that has the {@link HelpFormatter}
	 * @return the indented string
	 */
	public static <T extends ParentCommandGetter> String indent(String str, T obj) {
		return HelpFormatter.indent(str, obj.getParentCommand());
	}
}

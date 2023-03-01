package lanat.helpRepresentation;

import lanat.Command;
import lanat.CommandUser;
import lanat.helpRepresentation.descriptions.Tag;
import lanat.utils.UtlString;
import lanat.utils.displayFormatter.Color;
import lanat.utils.displayFormatter.FormatOption;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class HelpFormatter {
	Command parentCmd;
	private byte indentSize = 3;
	public static short lineWrapMax = 110;
	private @NotNull ArrayList<@NotNull LayoutItem> layout = new ArrayList<>();
	public static boolean debugLayout = false;

	public HelpFormatter(@Nullable Command parentCmd) {
		this.parentCmd = parentCmd;
		this.initLayout();
		Tag.initTags();
	}

	// the user can create a helpFormatter, though, the parentCmd should be applied later (otherwise stuff will fail)
	public HelpFormatter() {
		this((Command)null);
	}

	public HelpFormatter(@NotNull HelpFormatter other) {
		this.parentCmd = other.parentCmd;
		this.indentSize = other.indentSize;
		this.layout.addAll(other.layout);
	}

	public void setParentCmd(@NotNull Command parentCmd) {
		this.parentCmd = parentCmd;
	}

	public void setIndentSize(int indentSize) {
		this.indentSize = (byte)Math.max(indentSize, 0);
	}

	public byte getIndentSize() {
		return this.indentSize;
	}

	public @NotNull List<@NotNull LayoutItem> getLayout() {
		return this.layout;
	}

	protected void initLayout() {
		this.setLayout(
			LayoutItem.of(LayoutGenerators::title),
			LayoutItem.of(LayoutGenerators::synopsis).indent(1).margin(1),
			LayoutItem.of(LayoutGenerators::argumentDescriptions).title("Description:").indent(1),
			LayoutItem.of(LayoutGenerators::subCommandsDescriptions).title("Sub-Commands:").indent(1).marginTop(1),
			LayoutItem.of(LayoutGenerators::programLicense).marginTop(2)
		);
	}

	public void moveLayoutItem(int from, int to) {
		if (from < 0 || from >= this.layout.size() || to < 0 || to >= this.layout.size()) {
			throw new IndexOutOfBoundsException("invalid indices given");
		}

		// same index, nothing to do
		if (from == to)
			return;

		final var item = this.layout.remove(from);
		this.layout.add(to, item);
	}

	public final void addToLayout(@NotNull LayoutItem... layoutItems) {
		Collections.addAll(this.layout, layoutItems);
	}

	public final void addToLayout(int at, @NotNull LayoutItem... layoutItems) {
		this.layout.addAll(at, Arrays.asList(layoutItems));
	}

	public final void setLayout(@NotNull LayoutItem... layoutItems) {
		this.layout = new ArrayList<>(Arrays.asList(layoutItems));
	}


	@Override
	public @NotNull String toString() {
		final var buffer = new StringBuilder();

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
	public static @NotNull String indent(@NotNull String str, @NotNull Command cmd) {
		return UtlString.indent(str, cmd.getHelpFormatter().getIndentSize());
	}

	/**
	 * Indents a string by the indent size specified in the {@link HelpFormatter} instance of the {@link Command} this
	 * object belongs to.
	 *
	 * @param str the string to indent
	 * @param obj the object that belongs to the {@link Command} that has the {@link HelpFormatter}
	 * @return the indented string
	 */
	public static <T extends CommandUser> @NotNull String indent(@NotNull String str, @NotNull T obj) {
		return HelpFormatter.indent(
			// if obj is a Command, use it, otherwise get the parent command
			str, obj instanceof Command cmd ? cmd : Objects.requireNonNull(obj.getParentCommand())
		);
	}
}

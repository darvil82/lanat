package lanat.helpRepresentation;

import lanat.Command;
import lanat.CommandUser;
import lanat.helpRepresentation.descriptions.Tag;
import org.jetbrains.annotations.NotNull;
import textFormatter.Color;
import textFormatter.FormatOption;
import textFormatter.TextFormatter;
import utils.UtlString;

import java.util.*;

/**
 * Manager for generating the help message of a command. It is possible to customize the layout of the help message by
 * overriding the {@link #initLayout()} method.
 * <p>
 * The layout is a list of {@link LayoutItem} objects, which are used to generate the help message. Each
 * {@link LayoutItem} has a layout generator, which is a function that may take a {@link Command} as parameter and
 * returns a string.
 * </p>
 * <p>
 * To generate the help message, use {@link #generate(Command)}.
 * </p>
 *
 * @see LayoutItem
 */
public class HelpFormatter {
	private byte indentSize = 3;
	public static short lineWrapMax = 110;
	private @NotNull ArrayList<@NotNull LayoutItem> layout = new ArrayList<>();
	public static boolean debugLayout = false;

	static {
		// register the default tags before we start parsing descriptions
		Tag.initTags();
	}

	public HelpFormatter() {
		this.initLayout();
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

	/**
	 * Initializes the layout of the help message.
	 */
	protected void initLayout() {
		this.setLayout(
			LayoutItem.of(LayoutGenerators::titleAndDescription),
			LayoutItem.of(LayoutGenerators::synopsis)
				.indent(1)
				.margin(1),
			LayoutItem.of(LayoutGenerators::argumentDescriptions)
				.title("Description:")
				.indent(1),
			LayoutItem.of(LayoutGenerators::subCommandsDescriptions)
				.title("Sub-Commands:")
				.indent(1)
				.marginTop(1),
			LayoutItem.of(LayoutGenerators::programLicense)
				.marginTop(2)
		);
	}

	/**
	 * Moves a {@link LayoutItem} from one position to another.
	 *
	 * @param from the index of the item to move
	 * @param to the index to move the item to
	 */
	public final void moveLayoutItem(int from, int to) {
		if (from < 0 || from >= this.layout.size() || to < 0 || to >= this.layout.size()) {
			throw new IndexOutOfBoundsException("invalid indices given");
		}

		// same index, nothing to do
		if (from == to)
			return;

		final var item = this.layout.remove(from);
		this.layout.add(to, item);
	}

	/**
	 * Adds one or more {@link LayoutItem} to the layout.
	 *
	 * @param layoutItems the {@link LayoutItem} to add
	 */
	public final void addToLayout(@NotNull LayoutItem... layoutItems) {
		Collections.addAll(this.layout, layoutItems);
	}

	/**
	 * Adds one or more {@link LayoutItem} to the layout at the specified position.
	 *
	 * @param at the position to add the item/s at
	 * @param layoutItems the item/s to add
	 */
	public final void addToLayout(int at, @NotNull LayoutItem... layoutItems) {
		this.layout.addAll(at, Arrays.asList(layoutItems));
	}

	/**
	 * Sets the layout to the specified {@link LayoutItem} objects.
	 *
	 * @param layoutItems the items to set the layout to
	 */
	public final void setLayout(@NotNull LayoutItem... layoutItems) {
		this.layout = new ArrayList<>(Arrays.asList(layoutItems));
	}

	/**
	 * Removes one or more {@link LayoutItem} from the layout.
	 *
	 * @param positions the positions of the items to remove
	 */
	public final void removeFromLayout(int... positions) {
		Arrays.sort(positions);

		for (int i = positions.length - 1; i >= 0; i--) {
			this.layout.remove(positions[i]);
		}
	}


	/**
	 * Generates the help message.
	 *
	 * @return the help message
	 */
	public @NotNull String generate(@NotNull Command cmd) {
		final var buffer = new StringBuilder();

		for (int i = 0; i < this.layout.size(); i++) {
			final var generatedContent = this.layout.get(i).generate(this, cmd);

			if (generatedContent == null)
				continue;

			if (HelpFormatter.debugLayout)
				buffer.append(new TextFormatter("LayoutItem " + i + ":\n")
					.addFormat(FormatOption.UNDERLINE)
					.withForegroundColor(Color.GREEN)
				);

			buffer.append(UtlString.wrap(generatedContent, lineWrapMax)).append('\n');
		}

		// strip() is used here because trim() also removes \022 (escape character)
		return buffer.toString().strip();
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

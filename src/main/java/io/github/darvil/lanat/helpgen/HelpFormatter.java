package io.github.darvil.lanat.helpgen;

import io.github.darvil.lanat.Argument;
import io.github.darvil.lanat.Command;
import io.github.darvil.lanat.Group;
import io.github.darvil.lanat.utils.NamedWithDescription;
import io.github.darvil.terminal.textformatter.FormatOption;
import io.github.darvil.terminal.textformatter.TextFormatter;
import io.github.darvil.terminal.textformatter.color.SimpleColor;
import io.github.darvil.utils.UtlString;
import org.jetbrains.annotations.NotNull;

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
	/** The default maximum length of a line in the help message. */
	public static final byte LINE_WRAP_DEFAULT = 110;
	private static final int LINE_WRAP_MIN = 25;

	private static int lineWrapMax = LINE_WRAP_DEFAULT;
	private static int indentSize = 3;

	/** The layout that defines the structure of the help message. */
	private @NotNull List<@NotNull LayoutItem> layout = new LinkedList<>();

	/** Whether to print debug information about the layout when generating the help message. */
	public static boolean debugLayout = false;

	/**
	 * Creates a new {@link HelpFormatter}, initializing the layout.
	 */
	public HelpFormatter() {
		this.initLayout();
	}

	/**
	 * Sets the indent size to the specified value. The indent size is the number of spaces that are used to indent
	 * lines in the help message. The default value is 3.
	 * <p>
	 * The indent size must be between 0 and 10.
	 * @param indentSize the new indent size
	 */
	public static void setIndentSize(int indentSize) {
		if (indentSize < 0)
			throw new IllegalArgumentException("indentSize must be at least 0");

		HelpFormatter.indentSize = indentSize;
	}

	/**
	 * Returns the indent size.
	 * @return the indent size
	 */
	public static int getIndentSize() {
		return HelpFormatter.indentSize;
	}

	/**
	 * Sets the maximum length of a line in the help message to the specified value.
	 * When a line exceeds this length, it is wrapped to the next line.
	 * @param lineWrapMax the new maximum length of a line
	 */
	public static void setLineWrapMax(int lineWrapMax) {
		if (lineWrapMax < LINE_WRAP_MIN)
			throw new IllegalArgumentException("lineWrapMax must be at least " + LINE_WRAP_MIN);

		HelpFormatter.lineWrapMax = lineWrapMax;
	}

	/**
	 * Returns the maximum length of a line in the help message.
	 * @return the maximum length of a line in the help message
	 */
	public static int getLineWrapMax() {
		return HelpFormatter.lineWrapMax;
	}

	/**
	 * Returns the layout of the help message.
	 * @return the layout of the help message
	 */
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
				.withIndent(1)
				.withMargin(1),
			LayoutItem.of(LayoutGenerators::argumentsDescriptions)
				.withTitle("Description:")
				.withIndent(1),
			LayoutItem.of(LayoutGenerators::subCommandsDescriptions)
				.withTitle("Sub-Commands:")
				.withIndent(1)
				.withMarginTop(1),
			LayoutItem.of(LayoutGenerators::programDetails)
				.withTitle("Program Details:")
				.withIndent(1)
				.withMarginTop(2)
		);
	}

	/**
	 * Moves a {@link LayoutItem} from one position to another and returns the moved item.
	 * @param from the index of the item to move
	 * @param to the index to move the item to
	 * @return the moved item
	 */
	public @NotNull LayoutItem moveLayoutItem(int from, int to) {
		if (from < 0 || from >= this.layout.size() || to < 0 || to >= this.layout.size()) {
			throw new IndexOutOfBoundsException("invalid indices given");
		}

		// same index, nothing to do
		if (from == to)
			return this.layout.get(from);

		final var item = this.layout.remove(from);
		this.layout.add(to, item);
		return item;
	}

	/**
	 * Adds one or more {@link LayoutItem} to the layout.
	 *
	 * @param layoutItems the {@link LayoutItem} to add
	 */
	public final void addLayoutItems(@NotNull LayoutItem... layoutItems) {
		Collections.addAll(this.layout, layoutItems);
	}

	/**
	 * Adds one or more {@link LayoutItem} to the layout at the specified position.
	 *
	 * @param at the position to add the item/s at
	 * @param layoutItems the item/s to add
	 */
	public final void addLayoutItems(int at, @NotNull LayoutItem... layoutItems) {
		this.layout.addAll(at, Arrays.asList(layoutItems));
	}

	/**
	 * Sets the layout to the specified {@link LayoutItem}s.
	 * @param layoutItems the items to set the layout to
	 */
	public final void setLayout(@NotNull LayoutItem... layoutItems) {
		this.setLayout(List.of(layoutItems));
	}

	/**
	 * Sets the layout to the specified {@link LayoutItem}s.
	 * @param layoutItems the items to set the layout to
	 */
	public final void setLayout(@NotNull List<@NotNull LayoutItem> layoutItems) {
		this.layout = new ArrayList<>(layoutItems);
	}

	/**
	 * Removes one or more {@link LayoutItem} from the layout.
	 *
	 * @param indices the indices of the items to remove
	 */
	public final void removeLayoutItems(int... indices) {
		Arrays.sort(indices);

		for (int i = indices.length - 1; i >= 0; i--) {
			this.layout.remove(indices[i]);
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
			final var generatedContent = this.layout.get(i).generate(cmd);

			if (generatedContent == null)
				continue;

			if (HelpFormatter.debugLayout)
				buffer.append(TextFormatter.of("LayoutItem " + i + ":" + System.lineSeparator())
					.addFormat(FormatOption.UNDERLINE)
					.withForegroundColor(SimpleColor.GREEN)
				);

			buffer.append(UtlString.wrap(generatedContent, lineWrapMax)).append(System.lineSeparator());
		}

		// strip() is used here because trim() also removes \022 (escape character)
		return buffer.toString().strip();
	}

	/**
	 * Indents a string by the indent size specified in {@link HelpFormatter#getIndentSize()}.
	 * @param str the string to indent
	 * @return the indented string
	 */
	public static @NotNull String indent(@NotNull String str) {
		return UtlString.indent(str, HelpFormatter.getIndentSize());
	}

	/**
	 * Returns the representation of the object specified.
	 * This will call the appropriate method from the {@link CommandRepr}, {@link ArgumentRepr} or
	 * {@link GroupRepr} classes.
	 * If the object is not a command, argument or argument group, the name of the
	 * object will be returned.
	 * @param obj the object to get the representation from
	 * @return the representation of the object
	 */
	public static @NotNull String getRepresentation(@NotNull NamedWithDescription obj) {
		if (obj instanceof Command cmd)
			return CommandRepr.getRepresentation(cmd);
		else if (obj instanceof Argument<?, ?> arg)
			return ArgumentRepr.getRepresentation(arg, false);
		else if (obj instanceof Group group)
			return GroupRepr.getName(group);

		return obj.getName();
	}
}
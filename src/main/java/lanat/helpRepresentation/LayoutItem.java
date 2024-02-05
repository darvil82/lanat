package lanat.helpRepresentation;

import lanat.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.UtlString;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a layout item in the help message generated by {@link HelpFormatter}. This class is essentially just a
 * builder with some helper utilities for setting a {@link Function} that generates a {@link String} for a given
 * {@link Command}.
 *
 * @see HelpFormatter
 */
public class LayoutItem {
	private byte indentCount = 0;
	private @Nullable String title;
	private int marginTop, marginBottom;
	private final @NotNull Function<@NotNull Command, @Nullable String> layoutGenerator;

	private LayoutItem(@NotNull Function<@NotNull Command, @Nullable String> layoutGenerator) {
		this.layoutGenerator = layoutGenerator;
	}

	/**
	 * Creates a new {@link LayoutItem} with the given {@link Function} that generates a {@link String} for a given
	 * {@link Command}.
	 *
	 * @param layoutGenerator the function that generates the content of the layout item
	 * @return the new LayoutItem
	 */
	public static LayoutItem of(@NotNull Function<@NotNull Command, @Nullable String> layoutGenerator) {
		return new LayoutItem(layoutGenerator);
	}

	/**
	 * Creates a new {@link LayoutItem} with the given {@link Supplier} that generates a {@link String}.
	 *
	 * @param layoutGenerator the supplier that generates the content of the layout item
	 * @return the new LayoutItem
	 */
	public static LayoutItem of(@NotNull Supplier<@Nullable String> layoutGenerator) {
		return new LayoutItem(cmd -> layoutGenerator.get());
	}

	/**
	 * Creates a new {@link LayoutItem} with the given {@link String} as content.
	 *
	 * @param content the content of the layout item
	 * @return the new LayoutItem
	 */
	public static LayoutItem of(@NotNull String content) {
		return new LayoutItem(cmd -> content);
	}


	/**
	 * Sets the indent of the layout item. The indent is the number of indents that are added to the content of the
	 * layout item. The indent size is defined by {@link HelpFormatter#setIndentSize(int)}.
	 *
	 * @param indent the indent of the layout item
	 */
	public LayoutItem withIndent(int indent) {
		this.indentCount = (byte)Math.max(indent, 0);
		return this;
	}

	/**
	 * Sets the margin at the top of the layout item. The margin is the number of newlines that are added before the
	 * content of the layout item.
	 *
	 * @param marginTop the size of the margin at the top of the layout item
	 */
	public LayoutItem withMarginTop(int marginTop) {
		this.marginTop = Math.max(marginTop, 0);
		return this;
	}

	/**
	 * Sets the margin at the bottom of the layout item. The margin is the number of newlines that are added after the
	 * content of the layout item.
	 *
	 * @param marginBottom the size of the margin at the bottom of the layout item
	 */
	public LayoutItem withMarginBottom(int marginBottom) {
		this.marginBottom = Math.max(marginBottom, 0);
		return this;
	}

	/**
	 * Sets the margin at the top and bottom of the layout item. The margin is the number of newlines that are added
	 * before and after the content of the layout item.
	 *
	 * @param margin the size of the margin at the top and bottom of the layout item
	 */
	public LayoutItem withMargin(int margin) {
		this.withMarginTop(margin);
		return this.withMarginBottom(margin);
	}

	/**
	 * Sets the title of the layout item. The title is added before the content of the layout item. The title is not
	 * indented.
	 * <p>
	 * It is shown as:
	 * <pre>
	 * &lt;title&gt;:
	 *    &lt;content&gt;
	 * </pre>
	 * If no content is generated, the title is not shown.
	 *
	 * @param title the title of the layout item
	 */
	public LayoutItem withTitle(String title) {
		this.title = title;
		return this;
	}

	/**
	 * Returns the {@link Function} that generates the content of the layout item.
	 *
	 * @return the layout generator
	 */
	public @NotNull Function<@NotNull Command, @Nullable String> getLayoutGenerator() {
		return this.layoutGenerator;
	}

	/**
	 * Generates the content of the layout item. The reason this method requires a {@link HelpFormatter} is because it
	 * provides the indent size and the parent command.
	 *
	 * @param helpFormatter the help formatter that is generating the help message
	 * @return the content of the layout item
	 */
	public @Nullable String generate(@NotNull HelpFormatter helpFormatter, @NotNull Command cmd) {
		final var content = this.layoutGenerator.apply(cmd);
		return (content == null || content.isEmpty()) ? null : (
			System.lineSeparator().repeat(this.marginTop)
				+ (this.title == null ? "" : this.title + System.lineSeparator().repeat(2))
				// strip() is used here because trim() also removes \022 (escape character)
				+ UtlString.indent(content.strip(), this.indentCount * helpFormatter.getIndentSize())
				+ System.lineSeparator().repeat(this.marginBottom)
		);
	}
}
package lanat.parsing.errors.contexts;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.Pair;
import utils.Range;

/**
 * Class used by error handlers to easily format errors to be displayed to the user.
 */
public class ErrorFormattingContext {
	private @NotNull String content = "";
	private @Nullable HighlightOptions tokensViewOptions;

	/**
	 * Sets the content of the error message.
	 * @param content The content of the error message.
	 */
	public ErrorFormattingContext withContent(@NotNull String content) {
		this.content = content;
		return this;
	}

	/** Indicates the formatter to display the user input. */
	public ErrorFormattingContext showInput() {
		this.tokensViewOptions = new HighlightOptions(null, false);
		return this;
	}

	/**
	 * Indicates the formatter to display the user input.
	 * <p>
	 * The input between the index {@code start} and the {@code offsetEnd} from it will be highlighted. If {@code showArrows}
	 * is {@code true}, two arrows will be placed at the start and end of the highlighted range instead.
	 * </p>
	 * @param start The index of the first input value to highlight.
	 * @param offsetEnd The number of values to highlight after the value at the index {@code start}.
	 * @param showArrows Whether to show arrows instead of highlighting the input.
	 */
	public ErrorFormattingContext showAndHighlightInput(int start, int offsetEnd, boolean showArrows) {
		this.tokensViewOptions = new HighlightOptions(
			Range.from(start).to(start + offsetEnd),
			showArrows
		);
		return this;
	}

	/**
	 * Indicates the formatter to display the user input.
	 * Same as {@link #showAndHighlightInput(int, int, boolean)} but with a pair of indices.
	 * @see #showAndHighlightInput(int, int, boolean)
	 * @param indexAndOffset The pair of indices to highlight.
	 * @param showArrows Whether to show arrows instead of highlighting the input.
	 */
	public ErrorFormattingContext showAndHighlightInput(@NotNull Pair<Integer, Integer> indexAndOffset, boolean showArrows) {
		return this.showAndHighlightInput(indexAndOffset.first(), indexAndOffset.second(), showArrows);
	}

	/**
	 * Indicates the formatter to display the user input. The input at the given index will be highlighted.
	 * @param index The index of the input value to highlight.
	 */
	public ErrorFormattingContext showAndHighlightInput(int index) {
		return this.showAndHighlightInput(index, 0, true);
	}

	/**
	 * Returns the options used to display input.
	 * @return The options used to display input.
	 */
	public @Nullable HighlightOptions getHighlightOptions() {
		return this.tokensViewOptions;
	}

	/**
	 * Returns the content of the error message.
	 * @return The content of the error message.
	 */
	public @NotNull String getContent() {
		return this.content;
	}

	/**
	 * Options used to display the input.
	 * @param range The range of the input to highlight.
	 * @param showArrows Whether to show arrows instead of highlighting the input.
	 */
	public record HighlightOptions(@Nullable Range range, boolean showArrows) {}
}
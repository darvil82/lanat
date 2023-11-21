package lanat.parsing.errors;

import lanat.utils.Range;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class used by error handlers to easily format errors to be displayed to the user.
 */
public class ErrorFormattingContext {
	private @NotNull String content = "";
	private @Nullable HighlightOptions tokensViewOptions;

	/**
	 * Sets the content of the error message.
	 * @param content The content of the error message.
	 * @return This instance.
	 */
	public ErrorFormattingContext withContent(@NotNull String content) {
		this.content = content;
		return this;
	}

	/**
	 * Indicates the generator to display all tokens.
	 * <p>
	 * Tokens between the index {@code start} and the {@code offsetEnd} from it will be highlighted. If {@code showArrows}
	 * is {@code true}, an arrow will be placed at each token index in that range.
	 * </p>
	 * @param start The index of the first token to highlight.
	 * @param offsetEnd The number of tokens to highlight after the token at the index {@code start}. A value of {@code 0}
	 *  may be used to highlight only the token at the index {@code start}.
	 * @param showArrows Whether to place an arrow at each token index in the range.
	 */
	public ErrorFormattingContext highlight(int start, int offsetEnd, boolean showArrows) {
		this.tokensViewOptions = new HighlightOptions(
			Range.from(start).to(start + offsetEnd),
			showArrows
		);
		return this;
	}

	/**
	 * Indicates the generator to display all tokens. Places an error at the token at index {@code index}.
	 * @param index The index of the token to highlight.
	 */
	public ErrorFormattingContext highlight(int index) {
		return this.highlight(index, 0, true);
	}

	public @Nullable HighlightOptions getHighlightOptions() {
		return this.tokensViewOptions;
	}

	public @NotNull String getContent() {
		return this.content;
	}

	/**
	 * Options used to display tokens.
	 */
	public record HighlightOptions(@NotNull Range range, boolean showArrows) {
		public @NotNull HighlightOptions withOffset(int offset) {
			return new HighlightOptions(
				this.range.offset(offset),
				this.showArrows
			);
		}
	}
}

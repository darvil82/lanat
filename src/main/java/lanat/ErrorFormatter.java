package lanat;

import lanat.parsing.errors.ErrorsCollector;
import lanat.utils.Range;
import org.jetbrains.annotations.NotNull;

/**
 * Class used by error handlers to easily format errors to be displayed to the user.
 */
public class ErrorFormatter {
	private @NotNull String content = "";
	private HighlightOptions tokensViewOptions;

	/** Allows this class to provide some proxy instance methods to the {@link ErrorFormatter} instance. */
	private final @NotNull ErrorsCollector errorsCollector;

	/**
	 * Creates a new error formatter
	 * @param errorsCollector The error handler that created this error formatter.
	 * @param level The error level of the error.
	 */
	public ErrorFormatter(@NotNull ErrorsCollector errorsCollector) {
		this.errorsCollector = errorsCollector;
	}

	/**
	 * Sets the content of the error message.
	 * @param content The content of the error message.
	 * @return This instance.
	 */
	public ErrorFormatter withContent(@NotNull String content) {
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
	public ErrorFormatter highlight(int start, int offsetEnd, boolean showArrows) {
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
	public ErrorFormatter highlight(int index) {
		return this.highlight(index, 0, true);
	}


	/**
	 * Options used to display tokens.
	 */
	public record HighlightOptions(@NotNull Range tokensRange, boolean showArrows) { }
}

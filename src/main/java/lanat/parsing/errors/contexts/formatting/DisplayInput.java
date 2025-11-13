package lanat.parsing.errors.contexts.formatting;

import io.github.darvil.utils.Pair;
import io.github.darvil.utils.Range;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Options used to display the input.
 * @param highlight the highlight options to indicate the error formatter to highlight the input
 * @see ErrorFormattingContext
 */
public record DisplayInput(@Nullable Highlight highlight) {
	/** Instantiates a new display input with no highlight options. */
	public DisplayInput() {
		this(null);
	}

	/**
	 * Options used to highlight the input.
	 * @param range the range of the input values to highlight.
	 * @param showArrows whether to display arrows instead of highlighting the input.
	 */
	public record Highlight(@NotNull Range range, boolean showArrows) {
		/**
		 * Instantiates a new highlight options object.
		 * @param index the index of the input value to highlight.
		 * @param offsetEnd the offset from the index to highlight.
		 * @param showArrows whether to display arrows instead of highlighting the input.
		 */
		public Highlight(int index, int offsetEnd, boolean showArrows) {
			this(Range.from(index).to(index + offsetEnd), showArrows);
		}

		/**
		 * Instantiates a new highlight options object.
		 * @param indexAndOffset a pair containing the index of the input value to highlight and the offset from the index.
		 * @param showArrows whether to display arrows instead of highlighting the input.
		 */
		public Highlight(@NotNull Pair<Integer, Integer> indexAndOffset, boolean showArrows) {
			this(indexAndOffset.first(), indexAndOffset.second(), showArrows);
		}

		/**
		 * Instantiates a new highlight options object.
		 * @param index the index of the input value to highlight.
		 * @param showArrows whether to display arrows instead of highlighting the input.
		 */
		public Highlight(int index, boolean showArrows) {
			this(index, 0, showArrows);
		}

		/**
		 * Instantiates a new highlight options object. Displays an arrow instead of highlighting the input.
		 * @param index the index of the input value to highlight.
		 */
		public Highlight(int index) {
			this(index, true);
		}
	}
}
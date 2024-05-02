package lanat.parsing.errors.contexts.formatting;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class used by error handlers to easily format errors to be displayed to the user.
 */
public class ErrorFormattingContext {
	private @NotNull String content = "";
	private @Nullable DisplayInput displayInput;

	/**
	 * Sets the content of the error message.
	 * @param content The content of the error message.
	 */
	public ErrorFormattingContext withContent(@NotNull String content) {
		this.content = content;
		return this;
	}

	/**
	 * Indicates the formatter to display the user input with the specified options.
	 * @param options The options used to display input.
	 */
	public ErrorFormattingContext displayInput(@NotNull DisplayInput options) {
		this.displayInput = options;
		return this;
	}

	/** Indicates the formatter to display the user input. */
	public ErrorFormattingContext displayInput() {
		return this.displayInput(new DisplayInput());
	}

	/**
	 * Indicates the formatter to display the user input with the specified highlight options.
	 * @param highlight The highlight options.
	 */
	public ErrorFormattingContext displayAndHighlightInput(@NotNull DisplayInput.Highlight highlight) {
		return this.displayInput(new DisplayInput(highlight));
	}

	/**
	 * Returns the options used to display input.
	 * @return The options used to display input.
	 */
	public @Nullable DisplayInput getDisplayOptions() {
		return this.displayInput;
	}

	/**
	 * Returns the content of the error message.
	 * @return The content of the error message.
	 */
	public @NotNull String getContent() {
		return this.content;
	}
}
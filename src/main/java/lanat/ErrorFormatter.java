package lanat;

import lanat.errorFormatterGenerators.Pretty;
import lanat.errorFormatterGenerators.Simple;
import lanat.helpRepresentation.HelpFormatter;
import lanat.parsing.Token;
import lanat.parsing.errors.ErrorHandler;
import lanat.utils.ErrorLevelProvider;
import lanat.utils.UtlString;
import lanat.utils.displayFormatter.FormatOption;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Class used by error handlers to easily format errors to be displayed to the user.
 */
public class ErrorFormatter {
	private @NotNull String content = "";
	private DisplayTokensOptions tokensViewOptions;
	private @NotNull ErrorLevel errorLevel;

	/** Allows this class to provide some proxy instance methods to the {@link ErrorFormatter} instance. */
	private final @NotNull ErrorHandler mainErrorHandler;

	/** The generator used to generate the error message. */
	public static @NotNull ErrorFormatter.Generator generator = ErrorFormatter.DefaultGenerators.DEFAULT;

	/**
	 * Creates a new error formatter
	 * @param mainErrorHandler The error handler that created this error formatter.
	 * @param level The error level of the error.
	 */
	public ErrorFormatter(@NotNull ErrorHandler mainErrorHandler, @NotNull ErrorLevel level) {
		this.errorLevel = level;
		this.mainErrorHandler = mainErrorHandler;
		ErrorFormatter.generator.setErrorFormatter(this);
	}

	/**
	 * Sets the content of the error message.
	 * @param content The content of the error message.
	 * @return This instance.
	 */
	public ErrorFormatter setContent(@NotNull String content) {
		this.content = content;
		return this;
	}

	/**
	 * Sets the error level of the error to be displayed. This replaces the default error level provided in the
	 * constructor.
	 * @param errorLevel The error level of the error message.
	 * @return This instance.
	 */
	public ErrorFormatter setErrorLevel(@NotNull ErrorLevel errorLevel) {
		this.errorLevel = errorLevel;
		return this;
	}

	/**
	 * Returns the generated error message.
	 * @return The generated error message.
	 */
	@Override
	public String toString() {
		return ErrorFormatter.generator.generate();
	}

	/**
	 * Indicates the generator to display all tokens.
	 * <p>
	 * Tokens between the index {@code start} and the {@code offset} from it will be highlighted. If {@code placeArrow}
	 * is {@code true}, an arrow will be placed at each token index in that range.
	 * </p>
	 * @param start The index of the first token to highlight.
	 * @param offset The number of tokens to highlight after the token at the index {@code start}. A value of {@code 0}
	 *  may be used to highlight only the token at the index {@code start}.
	 * @param placeArrow Whether to place an arrow at each token index in the range.
	 */
	public ErrorFormatter displayTokens(int start, int offset, boolean placeArrow) {
		this.tokensViewOptions = new DisplayTokensOptions(
			start + this.mainErrorHandler.getAbsoluteCmdTokenIndex(), offset, placeArrow
		);
		return this;
	}

	/**
	 * Indicates the generator to display all tokens. Places an error at the token at index {@code index}.
	 * @param index The index of the token to highlight.
	 */
	public ErrorFormatter displayTokens(int index) {
		return this.displayTokens(index, 0, true);
	}


	/**
	 * Options used to display tokens.
	 */
	public record DisplayTokensOptions(int start, int offset, boolean placeArrow) {
		public DisplayTokensOptions {
			if (start < 0) throw new IllegalArgumentException("start must be positive");
			if (offset < 0) throw new IllegalArgumentException("offset must be positive");
		}
	}


	/**
	 * Class used to generate error messages from an {@link ErrorFormatter} instance.
	 * <p>
	 * Methods {@link #generate()} and {@link #generateTokensView(DisplayTokensOptions)} provide the
	 * functionality to generate the error message. These may be overridden to provide custom error messages.
	 * </p>
	 * <strong>Note:</strong> A single instance of this class is used to generate all messages for the errors.
	 */
	public static abstract class Generator implements ErrorLevelProvider {
		/** The error formatter instance to generate the error message from. */
		private ErrorFormatter errorFormatter;

		/**
		 * Sets the error formatter instance to generate the error message from.
		 * @param errorFormatter The error formatter instance to generate the error message from.
		 */
		private void setErrorFormatter(@NotNull ErrorFormatter errorFormatter) {
			this.errorFormatter = errorFormatter;
		}

		/**
		 * Generates the error message to be displayed to the user.
		 * @return The error message to be displayed to the user.
		 */
		public abstract @NotNull String generate();

		/**
		 * Generates the tokens view to be displayed to the user.
		 * @param options The options to use to generate the tokens view.
		 * @return The tokens view to be displayed to the user.
		 */
		protected abstract @NotNull String generateTokensView(DisplayTokensOptions options);

		/**
		 * Returns the tokens view to be displayed to the user. This is the result of calling
		 * {@link #generateTokensView(DisplayTokensOptions)} with the options provided in the {@link ErrorFormatter}
		 * @return The tokens view to be displayed to the user. An empty string if no tokens view options were provided.
		 */
		protected final @NotNull String getTokensView() {
			final var options = this.errorFormatter.tokensViewOptions;
			if (options == null) return "";

			return this.generateTokensView(options);
		}

		/**
		 * @see ErrorHandler#getAbsoluteCmdTokenIndex()
		 */
		protected final int getAbsoluteCmdTokenIndex() {
			return this.errorFormatter.mainErrorHandler.getAbsoluteCmdTokenIndex();
		}

		/**
		 * Returns the error level of the error currently being formatted.
		 * @return The error level of the error currently being formatted.
		 */
		@Override
		public final @NotNull ErrorLevel getErrorLevel() {
			return this.errorFormatter.errorLevel;
		}

		/**
		 * Returns a {@link TextFormatter} instance using the color and contents of the error
		 * level from {@link #getErrorLevel()}.
		 * @return A {@link TextFormatter} instance using the color and contents of the error level.
		 */
		protected final @NotNull TextFormatter getErrorLevelFormatter() {
			final var errorLevel = this.getErrorLevel();
			return new TextFormatter(errorLevel.toString(), errorLevel.color).addFormat(FormatOption.BOLD);
		}

		/**
		 * Returns the whole list of tokens from the {@link ErrorHandler} instance.
		 * @return The whole list of tokens.
		 */
		protected final @NotNull List<@NotNull Token> getTokens() {
			return this.errorFormatter.mainErrorHandler.tokens;
		}

		/**
		 * Returns the whole list of tokens from the {@link ErrorHandler} instance, each mapped to a
		 * {@link TextFormatter} instance.
		 * @return The whole list of tokens mapped to a {@link TextFormatter} instance.
		 * @see Token#getFormatter()
		 * @see #getTokens()
		 */
		protected final @NotNull List<@NotNull TextFormatter> getTokensFormatters() {
			return this.getTokens().stream().map(Token::getFormatter).toList();
		}

		/**
		 * Returns the text contents of the error message.
		 * @return The text contents of the error message.
		 */
		protected final @NotNull String getContents() {
			return this.errorFormatter.content;
		}

		/**
		 * Returns the text contents of the error message, wrapped to the maximum line length in
		 * {@link HelpFormatter#lineWrapMax}.
		 * @return The text contents of the error message, wrapped to the maximum line length.
		 * @see UtlString#wrap(String, int)
		 */
		protected final @NotNull String getContentsWrapped() {
			return UtlString.wrap(this.getContents(), HelpFormatter.lineWrapMax);
		}

		/**
		 * Returns the text contents of the error message, with all new lines replaced with a single space.
		 * @return The text contents of the error message, with all new lines replaced with a single space.
		 */
		protected final @NotNull String getContentsSingleLine() {
			return this.getContents().replaceAll("\n", " ");
		}

		/**
		 * @see ErrorHandler#getRootCommand()
		 */
		protected final @NotNull Command getRootCommand() {
			return this.errorFormatter.mainErrorHandler.getRootCommand();
		}
	}

	/**
	 * Default generators used to generate error messages.
	 */
	public static class DefaultGenerators {
		public static final @NotNull Generator DEFAULT = new Pretty();
		public static final @NotNull Generator SIMPLE = new Simple();
	}
}

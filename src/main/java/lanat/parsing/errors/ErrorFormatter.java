package lanat.parsing.errors;

import lanat.helpRepresentation.HelpFormatter;
import lanat.parsing.errors.contexts.ErrorContext;
import lanat.parsing.errors.contexts.ParseErrorContext;
import lanat.parsing.errors.contexts.TokenizeErrorContext;
import lanat.parsing.errors.contexts.formatting.DisplayInput;
import lanat.parsing.errors.contexts.formatting.ErrorFormattingContext;
import lanat.parsing.errors.formatGenerators.PrettyErrorFormatter;
import lanat.utils.errors.ErrorLevel;
import lanat.utils.errors.ErrorLevelProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import textFormatter.FormatOption;
import textFormatter.TextFormatter;
import utils.UtlString;

import java.util.Objects;
import java.util.Optional;

/**
 * Base class for error formatters. An error formatter defines how an error should be displayed to the user.
 * <p>
 * To use a custom error formatter, set {@link #errorFormatterClass} to the class of your custom error formatter.
 * This class will be automatically instantiated and used to generate the error message.
 * <p>
 * <strong>NOTE:</strong> the
 * custom error formatter class must <i>always</i> have a constructor that takes a {@link ErrorContext}.
 * <p>
 * An error formatter is instantiated by the {@link ErrorCollector}, only one per Command at most, to switch to the
 * next context. Note that it will only be instantiated if there are errors to display.
 */
public abstract class ErrorFormatter implements ErrorLevelProvider {
	/** The error formatter class used to generate the error messages. */
	public static @NotNull Class<? extends ErrorFormatter> errorFormatterClass = PrettyErrorFormatter.class;

	private final @NotNull ErrorContext currentErrorContext;
	private ErrorFormattingContext formattingContext;
	private ErrorLevel errorLevel;

	/**
	 * Instantiates a new error formatter.
	 * @param currentErrorContext the current error context
	 */
	public ErrorFormatter(@NotNull ErrorContext currentErrorContext) {
		this.currentErrorContext = currentErrorContext;
	}

	/**
	 * Generates the final formatted error message that will be displayed to the user.
	 * @return the formatted error message
	 */
	protected abstract @NotNull String generate();

	/**
	 * Generates the tokens view for a parse error when the error is a {@link lanat.parsing.errors.Error.ParseError}.
	 * <p>
	 * The tokens view is a view of the tokens that were passed to the parser.
	 * <p>
	 * This method should not be called directly, but rather {@link #getGeneratedView()}.
	 * @param ctx the current error context, in this case a parse error context
	 * @return the {@link TextFormatter} representing the tokens view
	 */
	protected abstract @Nullable TextFormatter generateTokensView(@NotNull ParseErrorContext ctx);

	/**
	 * Generates the input view for a tokenize error when the error is a {@link lanat.parsing.errors.Error.TokenizeError}.
	 * <p>
	 * The input view is the raw input string that was passed to the tokenizer.
	 * <p>
	 * This method should not be called directly, but rather {@link #getGeneratedView()}.
	 * @param ctx the current error context, in this case a tokenize error context
	 * @return the {@link TextFormatter} representing the input view
	 */
	protected abstract @Nullable TextFormatter generateInputView(@NotNull TokenizeErrorContext ctx);

	/**
	 * Returns the display options for the current error.
	 * @return the highlight options
	 * @see DisplayInput
	 */
	public final @NotNull Optional<DisplayInput> getDisplayOptions() {
		return Optional.ofNullable(this.formattingContext.getDisplayOptions());
	}

	/**
	 * Returns the highlight options for the current error if the {@link #getDisplayOptions()} are present.
	 * @return the highlight options
	 * @see DisplayInput#highlight()
	 */
	public final @NotNull Optional<DisplayInput.Highlight> getHighlightOptions() {
		return this.getDisplayOptions().map(DisplayInput::highlight);
	}

	/**
	 * Gets the appropriate generated view for the current error, depending on the type of the current error context.
	 * <p>
	 * This will call either {@link #generateTokensView(ParseErrorContext)} or {@link #generateInputView(TokenizeErrorContext)}.
	 * </p>
	 * If no view is generated, an empty {@link TextFormatter} is returned.
	 * @return the generated view.
	 * @see #generateTokensView(ParseErrorContext)
	 * @see #generateInputView(TokenizeErrorContext)
	 */
	protected final @NotNull TextFormatter getGeneratedView() {
		if (this.formattingContext.getDisplayOptions() == null)
			return TextFormatter.create();

		TextFormatter result = null;
		if (this.currentErrorContext instanceof ParseErrorContext parseContext)
			result = this.generateTokensView(parseContext);
		else if (this.currentErrorContext instanceof TokenizeErrorContext tokenizeContext)
			result = this.generateInputView(tokenizeContext);

		return Objects.requireNonNullElse(result, TextFormatter.create());
	}

	@Override
	public @NotNull ErrorLevel getErrorLevel() {
		return this.errorLevel;
	}

	/**
	 * Returns a {@link TextFormatter} with the error level name, colored with the error level color.
	 * @return the error level formatter
	 */
	protected @NotNull TextFormatter getErrorLevelFormatter() {
		return TextFormatter.of(this.errorLevel.name())
			.withForegroundColor(this.errorLevel.color)
			.addFormat(FormatOption.BOLD);
	}

	/**
	 * Returns the contents of the error.
	 * @return the contents of the error
	 */
	protected @NotNull String getContent() {
		return this.formattingContext.getContent();
	}

	/**
	 * Returns the contents of the error, with all newlines replaced with spaces.
	 * @return the contents of the error, with all newlines replaced with spaces
	 */
	protected @NotNull String getContentSingleLine() {
		return this.formattingContext.getContent().replaceAll(System.lineSeparator(), " ");
	}

	/**
	 * Returns the contents of the error, wrapped to fit {@link HelpFormatter#getLineWrapMax()}.
	 * @return the contents of the error, wrapped to fit {@link HelpFormatter#getLineWrapMax()}
	 * @see UtlString#wrap(String, int)
	 */
	protected @NotNull String getContentWrapped() {
		return UtlString.wrap(this.formattingContext.getContent(), HelpFormatter.getLineWrapMax());
	}

	/**
	 * Returns the error context.
	 * @return the error context
	 */
	public @NotNull ErrorContext getCurrentErrorContext() {
		return this.currentErrorContext;
	}

	/**
	 * Generates the formatted error message for the specified error. This method sets the
	 * error level and the formatting context, then calls {@link #generate()}.
	 * @param error the error
	 * @param formattingContext the formatting context
	 * @return the formatted error message
	 */
	public final @NotNull String generateInternal(@NotNull Error<?> error, @NotNull ErrorFormattingContext formattingContext) {
		this.errorLevel = error.getErrorLevel();
		this.formattingContext = formattingContext;
		return this.generate();
	}
}
package lanat.parsing.errors.formatGenerators;

import lanat.ErrorLevel;
import lanat.helpRepresentation.HelpFormatter;
import lanat.parsing.errors.Error;
import lanat.parsing.errors.*;
import lanat.utils.UtlMisc;
import lanat.utils.UtlString;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;

public abstract class BaseErrorFormatter {
	private final @NotNull BaseContext currentErrorContext;
	private ErrorFormattingContext formattingContext;
	private ErrorLevel errorLevel;

	public BaseErrorFormatter(@NotNull BaseContext currentErrorContext) {
		this.currentErrorContext = currentErrorContext;
	}

	protected abstract @NotNull String generate();
	protected abstract @NotNull String generateTokensView(@NotNull ErrorFormattingContext.HighlightOptions options, @NotNull ParseContext ctx);
	protected abstract @NotNull String generateInputView(@NotNull ErrorFormattingContext.HighlightOptions options, @NotNull TokenizeContext ctx);

	protected final @NotNull String getGeneratedView() {
		var highlightOptions = this.formattingContext.getHighlightOptions();

		if (highlightOptions == null)
			return "";

		highlightOptions = highlightOptions.applyOffsetFrom(this.currentErrorContext);

		String result = null;
		if (this.currentErrorContext instanceof ParseContext parseContext)
			result = this.generateTokensView(highlightOptions, parseContext);
		else if (this.currentErrorContext instanceof TokenizeContext tokenizeContext)
			result = this.generateInputView(highlightOptions, tokenizeContext);

		return UtlMisc.nonNullOrElse(result, "");
	}

	protected @NotNull ErrorLevel getErrorLevel() {
		return this.errorLevel;
	}

	protected @NotNull TextFormatter getErrorLevelFormatter() {
		return new TextFormatter(this.errorLevel.name())
			.withForegroundColor(this.errorLevel.color);
	}

	protected @NotNull String getContent() {
		return this.formattingContext.getContent();
	}

	protected @NotNull String getContentSingleLine() {
		return this.formattingContext.getContent().replaceAll("\n", " ");
	}

	protected @NotNull String getContentWrapped() {
		return UtlString.wrap(this.formattingContext.getContent(), HelpFormatter.lineWrapMax);
	}

	public @NotNull BaseContext getCurrentErrorContext() {
		return this.currentErrorContext;
	}

	public final @NotNull String generateInternal(@NotNull Error<?> error, @NotNull ErrorFormattingContext formattingContext) {
		this.errorLevel = error.getErrorLevel();
		this.formattingContext = formattingContext;
		return this.generate();
	}
}

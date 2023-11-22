package lanat.parsing.errors;

import lanat.ErrorLevel;
import lanat.helpRepresentation.HelpFormatter;
import lanat.utils.UtlMisc;
import lanat.utils.UtlString;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseErrorFormatter {
	private final @NotNull BaseContext currentErrorContext;
	private ErrorFormattingContext formattingContext;
	private ErrorLevel errorLevel;

	public BaseErrorFormatter(@NotNull BaseContext currentErrorContext) {
		this.currentErrorContext = currentErrorContext;
	}

	protected abstract @NotNull String generate();
	protected abstract @Nullable String generateTokensView(@NotNull ParseContext ctx);
	protected abstract @Nullable String generateInputView(@NotNull TokenizeContext ctx);

	public final @Nullable ErrorFormattingContext.HighlightOptions getHighlightOptions() {
		return UtlMisc.nullOrElse(
			this.formattingContext.getHighlightOptions(),
			v -> v.withOffset(this.currentErrorContext.getAbsoluteIndex())
		);
	}

	protected final @NotNull String getGeneratedView() {
		String result = null;
		if (this.currentErrorContext instanceof ParseContext parseContext)
			result = this.generateTokensView(parseContext);
		else if (this.currentErrorContext instanceof TokenizeContext tokenizeContext)
			result = this.generateInputView(tokenizeContext);

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

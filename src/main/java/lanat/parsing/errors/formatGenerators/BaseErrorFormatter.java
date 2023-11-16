package lanat.parsing.errors.formatGenerators;

import lanat.parsing.errors.ErrorFormattingContext;
import org.jetbrains.annotations.NotNull;

public abstract class BaseErrorFormatter {


	public abstract @NotNull String generate();
	protected abstract @NotNull String generateTokensView(@NotNull ErrorFormattingContext.HighlightOptions options);
	protected abstract @NotNull String generateInputView(@NotNull ErrorFormattingContext.HighlightOptions options);

	protected final @NotNull String getGeneratedView() {

	}
}

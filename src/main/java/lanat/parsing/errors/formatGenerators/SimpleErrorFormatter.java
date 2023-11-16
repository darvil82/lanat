package lanat.parsing.errors.formatGenerators;

import lanat.parsing.errors.ErrorFormattingContext;
import org.jetbrains.annotations.NotNull;

public class SimpleErrorFormatter extends BaseErrorFormatter {
	@Override
	public @NotNull String generate() {
		final var formatter = this.getErrorLevelFormatter()
			.withContents("[" + this.getErrorLevel() + this.getTokensView() + "]: ");

		return formatter + this.getContentsSingleLine() + '\n';
	}

	@Override
	protected @NotNull String generateTokensView(@NotNull ErrorFormattingContext.HighlightOptions options) {
		final var range = options.tokensRange();

		String rangeRpr = range.isRange() ?
			"s " + range.start() + " to " + range.end()
			: " " + range.start();

		return " (token" + rangeRpr + ")";
	}
}

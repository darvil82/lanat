package lanat.errorFormatterGenerators;

import lanat.ErrorFormatter;
import org.jetbrains.annotations.NotNull;

public class Simple extends ErrorFormatter.Generator {
	@Override
	public @NotNull String generate() {
		final var formatter = this.getErrorLevelFormatter()
			.withContents("[" + this.getErrorLevel() + this.getTokensView() + "]: ");

		return formatter + this.getContentsSingleLine() + '\n';
	}

	@Override
	protected @NotNull String generateTokensView(@NotNull ErrorFormatter.DisplayTokensOptions options) {
		final var range = options.tokensRange();

		String rangeRpr = range.isRange() ?
			"s " + range.start() + " to " + range.end()
			: " " + range.start();

		return " (token" + rangeRpr + ")";
	}
}

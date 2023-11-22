package lanat.parsing.errors.formatGenerators;

import lanat.parsing.errors.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleErrorFormatter extends BaseErrorFormatter {
	public SimpleErrorFormatter(@NotNull BaseContext currentErrorContext) {
		super(currentErrorContext);
	}

	@Override
	protected @NotNull String generate() {
		final var formatter = this.getErrorLevelFormatter()
			.withContents("[" + this.getErrorLevel() + this.getGeneratedView() + "]: ");

		return formatter + this.getContentSingleLine() + '\n';
	}

	@Override
	protected @Nullable String generateTokensView(@NotNull ParseContext ctx) {
		return this.getView("token");
	}

	@Override
	protected @Nullable String generateInputView(@NotNull TokenizeContext ctx) {
		return this.getView("char");
	}

	private @Nullable String getView(@NotNull String name) {
		return this.getHighlightOptions()
			.map(ErrorFormattingContext.HighlightOptions::range)
			.map(range -> {
				String rangeRpr = range.isRange()
					? "s " + range.start() + " to " + range.end()
					: " " + range.start();

				return " (" + name + rangeRpr + ")";
			})
			.orElse(null);
	}
}

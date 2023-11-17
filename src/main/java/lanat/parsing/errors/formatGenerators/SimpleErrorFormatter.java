package lanat.parsing.errors.formatGenerators;

import lanat.parsing.errors.BaseContext;
import lanat.parsing.errors.ErrorFormattingContext;
import lanat.parsing.errors.ParseContext;
import lanat.parsing.errors.TokenizeContext;
import lanat.utils.Range;
import org.jetbrains.annotations.NotNull;

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
	protected @NotNull String generateTokensView(ErrorFormattingContext.@NotNull HighlightOptions options, @NotNull ParseContext ctx) {
		return getView(options.range(), "token");
	}

	@Override
	protected @NotNull String generateInputView(ErrorFormattingContext.@NotNull HighlightOptions options, @NotNull TokenizeContext ctx) {
		return getView(options.range(), "char");
	}

	private static @NotNull String getView(@NotNull Range range, @NotNull String name) {
		String rangeRpr = range.isRange() ?
			"s " + range.start() + " to " + range.end()
			: " " + range.start();

		return " (" + name + rangeRpr + ")";
	}
}

package lanat.parsing.errors.formatGenerators;

import lanat.parsing.errors.ErrorContext;
import lanat.parsing.errors.ErrorFormatter;
import lanat.parsing.errors.ParseErrorContext;
import lanat.parsing.errors.TokenizeErrorContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import textFormatter.FormatOption;
import textFormatter.TextFormatter;
import utils.UtlString;

public class SimpleErrorFormatter extends ErrorFormatter {
	public SimpleErrorFormatter(@NotNull ErrorContext currentErrorContext) {
		super(currentErrorContext);
	}

	@Override
	protected @NotNull String generate() {
		final var formatter = this.getErrorLevelFormatter()
			.withContents("[")
			.concat(this.getErrorLevel().name())
			.concat(this.getGeneratedView())
			.concat("]: ");

		return formatter + this.getContentSingleLine();
	}

	@Override
	protected @Nullable TextFormatter generateTokensView(@NotNull ParseErrorContext ctx) {
		return this.getView(ctx, "token");
	}

	@Override
	protected @Nullable TextFormatter generateInputView(@NotNull TokenizeErrorContext ctx) {
		return this.getView(ctx, "char");
	}

	private @Nullable TextFormatter getView(@NotNull ErrorContext ctx, @NotNull String name) {
		return this.getHighlightOptions()
			.map(opts -> {
				final var range = ctx.applyAbsoluteOffset(opts.range());

				String indicator = null;
				var nearContents = new TextFormatter().addFormat(FormatOption.ITALIC);
				if (ctx instanceof TokenizeErrorContext tokenizeCtx) {
					indicator = "near character " + (range.start() + 1);
					nearContents.concat(UtlString.escapeQuotes(tokenizeCtx.getInputNear(range.start(), 5)));
				} else if (ctx instanceof ParseErrorContext parseCtx) {
					indicator = "at token " + (range.start() + 1);
					nearContents.concat(parseCtx.getTokenAt(range.start()).getFormatter());
				}

				return new TextFormatter(" (" + indicator + ", '")
					.concat(nearContents)
					.concat("')");
			})
			.orElse(null);
	}
}

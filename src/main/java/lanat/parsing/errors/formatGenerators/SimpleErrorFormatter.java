package lanat.parsing.errors.formatGenerators;

import lanat.parsing.errors.BaseContext;
import lanat.parsing.errors.ErrorFormatter;
import lanat.parsing.errors.ParseContext;
import lanat.parsing.errors.TokenizeContext;
import lanat.utils.displayFormatter.FormatOption;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.UtlString;

public class SimpleErrorFormatter extends ErrorFormatter {
	public SimpleErrorFormatter(@NotNull BaseContext currentErrorContext) {
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
	protected @Nullable TextFormatter generateTokensView(@NotNull ParseContext ctx) {
		return this.getView(ctx, "token");
	}

	@Override
	protected @Nullable TextFormatter generateInputView(@NotNull TokenizeContext ctx) {
		return this.getView(ctx, "char");
	}

	private @Nullable TextFormatter getView(@NotNull BaseContext ctx, @NotNull String name) {
		return this.getHighlightOptions()
			.map(opts -> {
				final var range = ctx.applyAbsoluteOffset(opts.range());

				String indicator = null;
				var nearContents = new TextFormatter().addFormat(FormatOption.ITALIC);
				if (ctx instanceof TokenizeContext tokenizeCtx) {
					indicator = "near character " + (range.start() + 1);
					nearContents.concat(UtlString.escapeQuotes(tokenizeCtx.getInputNear(range.start(), 5)));
				} else if (ctx instanceof ParseContext parseCtx) {
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

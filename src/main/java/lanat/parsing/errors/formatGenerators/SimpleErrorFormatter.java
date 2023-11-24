package lanat.parsing.errors.formatGenerators;

import lanat.parsing.errors.BaseContext;
import lanat.parsing.errors.ErrorFormatter;
import lanat.parsing.errors.ParseContext;
import lanat.parsing.errors.TokenizeContext;
import lanat.utils.displayFormatter.FormatOption;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleErrorFormatter extends ErrorFormatter {
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
				final var range = opts.range().offset(1); // +1 because we want the first char to be 1, not 0

				String rangeRpr = range.isRange()
					? "s " + range.start() + "-" + range.end()
					: " " + range.start();

				var nearContents = new TextFormatter().addFormat(FormatOption.ITALIC);
				if (ctx instanceof TokenizeContext tokenizeCtx) {
					nearContents.concat(tokenizeCtx.getInputNear(range.start(), 3));
				} else if (ctx instanceof ParseContext parseCtx) {
					nearContents.concat(parseCtx.getTokenAt(range.start()).contents());
				}

				return new TextFormatter(" (" + name + rangeRpr + ", near '")
					.concat(nearContents)
					.concat("')");
			})
			.orElse(null);
	}
}

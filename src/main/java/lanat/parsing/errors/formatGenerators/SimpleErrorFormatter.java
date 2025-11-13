package lanat.parsing.errors.formatGenerators;

import io.github.darvil.terminal.textformatter.FormatOption;
import io.github.darvil.terminal.textformatter.TextFormatter;
import io.github.darvil.utils.UtlString;
import lanat.parsing.errors.ErrorFormatter;
import lanat.parsing.errors.contexts.ErrorContext;
import lanat.parsing.errors.contexts.ParseErrorContext;
import lanat.parsing.errors.contexts.TokenizeErrorContext;
import lanat.parsing.errors.contexts.formatting.DisplayInput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An error formatter that displays the error in the next format:
 * <pre>
 * [$ERRORLEVEL (at $IN_TYPE $POS, '$INPUT']: $CONTENT
 * </pre>
 * With, the values being:
 * <ul>
 * <li>{@code $ERRORLEVEL}: The error level</li>
 * <li>{@code $IN_TYPE}: The type of the input where the error occurred (either 'token' or 'char')</li>
 * <li>{@code $POS}: The position of the error in the input</li>
 * <li>{@code $INPUT}: The token at the position of the error, or the characters near the position</li>
 * <li>{@code $CONTENT}: The content of the error</li>
 * </ul>
 * The contents inside square brackets are colored according to the error level.
 */
public class SimpleErrorFormatter extends ErrorFormatter {
	public SimpleErrorFormatter(@NotNull ErrorContext currentErrorContext) {
		super(currentErrorContext);
	}

	@Override
	protected @NotNull String generate() {
		final var formatter = this.getErrorLevelFormatter()
			.withContents("[")
			.concat(this.getErrorLevel().name(), this.getGeneratedView(), "]: ");

		return formatter + this.getContentSingleLine();
	}

	@Override
	protected @Nullable TextFormatter generateTokensView(@NotNull ParseErrorContext ctx) {
		return this.getView(ctx, "at token");
	}

	@Override
	protected @Nullable TextFormatter generateInputView(@NotNull TokenizeErrorContext ctx) {
		return this.getView(ctx, "near character");
	}

	/**
	 * Returns the view for the given error context.
	 * @param ctx the current error context
	 * @param indicator the indicator to use for the position
	 * @return the view for the given error context
	 */
	private @Nullable TextFormatter getView(@NotNull ErrorContext ctx, @NotNull String indicator) {
		return this.getHighlightOptions()
			.map(DisplayInput.Highlight::range)
			.map(ctx::applyAbsoluteOffset)
			.map(range -> {
				var nearContents = TextFormatter.create().addFormat(FormatOption.ITALIC);

				if (ctx instanceof TokenizeErrorContext tokenizeCtx)
					nearContents.concat(UtlString.escapeQuotes(tokenizeCtx.getInputNear(range.start(), 5)));
				else if (ctx instanceof ParseErrorContext parseCtx)
					nearContents.concat(parseCtx.getTokenAt(range.start()).getFormatter());

				return TextFormatter.of(" (" + (indicator + " " + (range.start() + 1)) + ", '")
					.concat(nearContents, "')");
			})
			.orElse(null);
	}
}
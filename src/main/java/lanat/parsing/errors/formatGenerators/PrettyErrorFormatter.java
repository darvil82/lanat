package lanat.parsing.errors.formatGenerators;

import lanat.parsing.errors.BaseContext;
import lanat.parsing.errors.ErrorFormattingContext;
import lanat.parsing.errors.ParseContext;
import lanat.parsing.errors.TokenizeContext;
import lanat.utils.Range;
import lanat.utils.UtlString;
import lanat.utils.displayFormatter.FormatOption;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PrettyErrorFormatter extends BaseErrorFormatter {
	public PrettyErrorFormatter(@NotNull BaseContext currentErrorContext) {
		super(currentErrorContext);
	}

	@Override
	protected @NotNull String generate() {
		final var contents = this.getContentWrapped();
		final var formatter = this.getErrorLevelFormatter();
		final String tokensFormatting = this.getGeneratedView();

		final var longestLineLength = UtlString.getLongestLine(contents).length();

		return formatter.withContents(" ┌─%s".formatted(this.getErrorLevel())).toString()
			// only add a new line if there are tokens to display
			+ (tokensFormatting.isEmpty() ? "" : "\n" + tokensFormatting)
			// first insert a vertical bar at the start of each line
			+ contents.replaceAll("^|\\n", formatter.withContents("\n │ ").toString())
			// then insert a horizontal bar at the end, with the length of the longest line approximately
			+ formatter.withContents("\n └" + "─".repeat(Math.max(longestLineLength - 5, 0)) + " ───── ── ─")
			+ '\n';
	}

	@Override
	protected @NotNull String generateTokensView(ErrorFormattingContext.@NotNull HighlightOptions options, @NotNull ParseContext ctx) {
		final var tokensFormatters = new ArrayList<>(ctx.getTokensFormatters(false));
		final int tokensLength = tokensFormatters.size();
		final var tokensRange = options.range();

		// add an arrow at the start or end if the index is out of bounds
		if (options.showArrows() || !TextFormatter.enableSequences)
			this.putArrows(tokensFormatters, tokensRange);
		else
			this.highlightTokens(tokensFormatters, tokensRange);


		for (int i = 0; i < tokensLength; i++) {
			// dim tokens before the command
			if (i < ctx.getAbsoluteIndex()) {
				tokensFormatters.get(i).addFormat(FormatOption.DIM);
			}
		}

		return String.join(" ", tokensFormatters.stream().map(TextFormatter::toString).toList());
	}

	@Override
	protected @NotNull String generateInputView(ErrorFormattingContext.@NotNull HighlightOptions options, @NotNull TokenizeContext ctx) {
		return null;
	}


	private void highlightTokens(@NotNull List<TextFormatter> tokensFormatters, @NotNull Range range) {
		for (int i = range.start(); i <= range.end(); i++) {
			tokensFormatters.get(i)
				.withForegroundColor(this.getErrorLevel().color)
				.addFormat(FormatOption.REVERSE, FormatOption.BOLD);
		}
	}

	private void putArrows(@NotNull List<TextFormatter> tokensFormatters, @NotNull Range range) {
		if (!range.isRange()) {
			if (range.start() >= tokensFormatters.size())
				tokensFormatters.add(this.getArrow(true));
			else
				tokensFormatters.add(range.start() + 1, this.getArrow(true));
			return;
		}

		tokensFormatters.add(range.end() + 1, this.getArrow(true));
		tokensFormatters.add(range.start(), this.getArrow(false));
	}

	private @NotNull TextFormatter getArrow(boolean isLeft) {
		return new TextFormatter(isLeft ? "<-" : "->", this.getErrorLevel().color)
			.addFormat(FormatOption.REVERSE, FormatOption.BOLD);
	}
}
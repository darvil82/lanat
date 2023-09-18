package lanat.errorFormatterGenerators;

import lanat.ErrorFormatter;
import lanat.utils.UtlString;
import lanat.utils.displayFormatter.FormatOption;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Pretty extends ErrorFormatter.Generator {
	@Override
	public @NotNull String generate() {
		// first figure out the length of the longest line
		final var maxLength = UtlString.getLongestLine(this.getContents()).length();
		final var formatter = this.getErrorLevelFormatter();
		final String tokensFormatting = this.getTokensView();

		return formatter.withContents(" ┌─%s".formatted(this.getErrorLevel())).toString()
			// only add a new line if there are tokens to display
			+ (tokensFormatting.isEmpty() ? "" : "\n" + tokensFormatting)
			// first insert a vertical bar at the start of each line
			+ this.getContentsWrapped().replaceAll("^|\\n", formatter.withContents("\n │ ").toString())
			// then insert a horizontal bar at the end, with the length of the longest line approximately
			+ formatter.withContents("\n └" + "─".repeat(Math.max(maxLength - 5, 0)) + " ───── ── ─")
			+ '\n';
	}

	@Override
	protected @NotNull String generateTokensView(@NotNull ErrorFormatter.DisplayTokensOptions options) {
		final var arrow = TextFormatter.ERROR("<-").withForegroundColor(this.getErrorLevel().color);
		final var tokensFormatters = new ArrayList<>(this.getTokensFormatters());
		final int tokensLength = tokensFormatters.size();
		final var tokensRange = options.tokensRange();

		// add an arrow at the start or end if the index is out of bounds
		if (tokensRange.start() < 0) {
			tokensFormatters.add(0, arrow);
		} else if (tokensRange.start() >= tokensLength) {
			tokensFormatters.add(arrow);
		}

		for (int i = 0; i < tokensLength; i++) {
			// dim tokens before the command
			if (i < this.getAbsoluteCmdTokenIndex()) {
				tokensFormatters.get(i).addFormat(FormatOption.DIM);
			}

			// highlight tokens in the range
			if (i >= tokensRange.start() && i < tokensRange.end() + 1) {
				if (options.placeArrow()) {
					tokensFormatters.add(i, arrow);
				} else {
					tokensFormatters.get(i)
						.withForegroundColor(this.getErrorLevel().color)
						.addFormat(FormatOption.REVERSE, FormatOption.BOLD);
				}
			}
		}

		return String.join(" ", tokensFormatters.stream().map(TextFormatter::toString).toList());
	}
}

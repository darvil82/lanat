package lanat.parsing.errors.formatGenerators;

import io.github.darvil.terminal.textformatter.FormatOption;
import io.github.darvil.terminal.textformatter.TextFormatter;
import io.github.darvil.terminal.textformatter.color.SimpleColor;
import io.github.darvil.utils.Range;
import io.github.darvil.utils.UtlString;
import lanat.parsing.Token;
import lanat.parsing.errors.ErrorFormatter;
import lanat.parsing.errors.contexts.ErrorContext;
import lanat.parsing.errors.contexts.ParseErrorContext;
import lanat.parsing.errors.contexts.TokenizeErrorContext;
import lanat.parsing.errors.contexts.formatting.DisplayInput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * An error formatter that displays the error in the next format:
 * <pre>
 *  ┌─$ERRORLEVEL
 * $INPUT
 *  │ $CONTENTS
 *  └─────── ───── ── ─
 * </pre>
 * With, the values being:
 * <ul>
 * <li>{@code $ERRORLEVEL}: The error level</li>
 * <li>{@code $INPUT}: The whole input. Either all the tokens, or the whole input string</li>
 * <li>{@code $CONTENTS}: The content of the error</li>
 * </ul>
 * The generated error is colored according to the error level.
 */
public class PrettyErrorFormatter extends ErrorFormatter {
	public PrettyErrorFormatter(@NotNull ErrorContext currentErrorContext) {
		super(currentErrorContext);
	}

	@Override
	protected @NotNull String generate() {
		final var contents = this.getContentWrapped();
		final var formatter = this.getErrorLevelFormatter();
		final String tokensFormatting = this.getGeneratedView().withConcatGap(" ").toString();

		final var longestLineLength = UtlString.getLengthIgnoreSequences(UtlString.getLongestLine(contents));

		return formatter.withContents(" ┌─%s".formatted(this.getErrorLevel())).toString()
			// only add a new line if there are tokens to display
			+ (tokensFormatting.isEmpty() ? "" : System.lineSeparator() + tokensFormatting)
			// first insert a vertical bar at the start of each line
			+ contents.replaceAll(
				"^|" + System.lineSeparator(),
				formatter.withContents(System.lineSeparator() + " │ ").toString()
			)
			// then insert a horizontal bar at the end, with the length of the longest line approximately
			+ formatter.withContents(
				System.lineSeparator()
					+ " └" + "─".repeat(Math.max(longestLineLength - 5, 0))
					+ " ───── ── ─"
			)
			+ System.lineSeparator();
	}

	@Override
	protected @Nullable TextFormatter generateTokensView(@NotNull ParseErrorContext ctx) {
		final var tokensFormatters = new ArrayList<TextFormatter>();
		tokensFormatters.add(ctx.getRootCommandToken().getFormatter());
		tokensFormatters.addAll(ctx.getTokens(false).stream().map(Token::getFormatter).toList());

		this.getHighlightOptions()
			.map(DisplayInput.Highlight::range)
			.map(range -> ctx.applyAbsoluteOffset(range).offset(1))
			.ifPresent(range -> {
				BiConsumer<List<TextFormatter>, Range> highlighter;

				if (this.getHighlightOptions().get().showArrows())
					highlighter = this::placeTokenArrowsExplicit;
				else if (!TextFormatter.enableSequences)
					highlighter = this::placeTokenArrowsImplicit;
				else
					highlighter = this::highlightTokens;

				highlighter.accept(tokensFormatters, range);
			});

		// dim tokens before the command
		for (int i = 0; i < ctx.getAbsoluteIndex(); i++) {
			tokensFormatters.get(i).addFormat(FormatOption.DIM);
		}

		var formatter = TextFormatter.create();
		tokensFormatters.forEach(formatter::concat);
		return formatter;
	}

	@Override
	protected @Nullable TextFormatter generateInputView(@NotNull TokenizeErrorContext ctx) {
		var cmdName = ctx.getCommand().getRoot().getName();
		var in = cmdName + " " + ctx.getInputString(false);

		return TextFormatter.of(
			this.getHighlightOptions()
				.map(DisplayInput.Highlight::range)
				.map(range -> ctx.applyAbsoluteOffset(range).offset(cmdName.length() + 2))
				.map(range -> {
					if (range.start() > in.length())
						return SimpleColor.BRIGHT_WHITE + in + this.getArrow(false);

					if (this.getHighlightOptions().get().showArrows() || !TextFormatter.enableSequences)
						return this.placeArrows(in, range);

					return this.highlightText(in, range);
				})
				.orElse(SimpleColor.BRIGHT_WHITE + in)
		);
	}


	private void highlightTokens(@NotNull List<@NotNull TextFormatter> tokenFormatters, @NotNull Range range) {
		for (int i : range) {
			this.applyErrorLevelFormat(tokenFormatters.get(i));
		}
	}

	private @NotNull String highlightText(@NotNull String in, @NotNull Range range) {
		return SimpleColor.BRIGHT_WHITE
			+ in.substring(0, range.start() - 1)
			+ this.applyErrorLevelFormat(TextFormatter.of(in.substring(range.start() - 1, range.end())))
			+ in.substring(range.end());
	}

	private void placeArrows(@NotNull List<@NotNull TextFormatter> tokenFormatters, @NotNull Range range, int singleOffset) {
		if (range.isSimple()) {
			if (range.start() >= tokenFormatters.size())
				tokenFormatters.add(this.getArrow(false));
			else
				tokenFormatters.add(range.start() + singleOffset, this.getArrow(false));
			return;
		}

		tokenFormatters.add(range.end() + 1, this.getArrow(false));
		tokenFormatters.add(range.start(), this.getArrow(true));
	}

	private @NotNull String placeArrows(@NotNull String in, @NotNull Range range) {
		return in.substring(0, range.start() - 1)
			+ this.getArrow(true)
			+ (
				TextFormatter.enableSequences
					? this.applyErrorLevelFormat(TextFormatter.of(in.substring(range.start() - 1, range.end())))
					: in.substring(range.start() - 1, range.end())
			)
			+ this.getArrow(false)
			+ in.substring(range.end());
	}

	private void placeTokenArrowsImplicit(@NotNull List<@NotNull TextFormatter> tokenFormatters, @NotNull Range range) {
		this.placeArrows(tokenFormatters, range, 1);
	}

	private void placeTokenArrowsExplicit(@NotNull List<@NotNull TextFormatter> tokenFormatters, @NotNull Range range) {
		this.placeArrows(tokenFormatters, range, 0);
	}

	private @NotNull TextFormatter getArrow(boolean isLeft) {
		return this.applyErrorLevelFormat(TextFormatter.of(isLeft ? "->" : "<-"));
	}

	private @NotNull TextFormatter applyErrorLevelFormat(@NotNull TextFormatter formatter) {
		return formatter.withForegroundColor(this.getErrorLevel().color)
			.addFormat(FormatOption.REVERSE, FormatOption.BOLD);
	}
}
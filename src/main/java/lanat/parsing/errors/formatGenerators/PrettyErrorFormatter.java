package lanat.parsing.errors.formatGenerators;

import lanat.parsing.Token;
import lanat.parsing.TokenType;
import lanat.parsing.errors.BaseContext;
import lanat.parsing.errors.BaseErrorFormatter;
import lanat.parsing.errors.ParseContext;
import lanat.parsing.errors.TokenizeContext;
import lanat.utils.Range;
import lanat.utils.UtlString;
import lanat.utils.displayFormatter.FormatOption;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

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
	protected @NotNull String generateTokensView(@NotNull ParseContext ctx) {
		final var tokensFormatters = new ArrayList<TextFormatter>() {{
			this.add(new Token(TokenType.COMMAND, ctx.getCommand().getRoot().getName()).getFormatter());
			this.addAll(ctx.getTokensFormatters(false));
		}};

		this.getHighlightOptions().ifPresent(opts -> {
			BiConsumer<List<TextFormatter>, Range> highlighter;

			if (opts.showArrows())
				highlighter = this::placeArrowsExplicit;
			else if (!TextFormatter.enableSequences)
				highlighter = this::placeArrowsImplicit;
			else
				highlighter = this::highlightTokens;

			highlighter.accept(tokensFormatters, opts.range());
		});

		// dim tokens before the command
		for (int i = 0; i < ctx.getAbsoluteIndex(); i++) {
			tokensFormatters.get(i).addFormat(FormatOption.DIM);
		}

		return String.join(" ", tokensFormatters.stream().map(TextFormatter::toString).toList());
	}

	@Override
	protected @NotNull String generateInputView(@NotNull TokenizeContext ctx) {
		var cmdName = ctx.getCommand().getName();
		var in = cmdName + " " + ctx.getInputString(false);

		return this.getHighlightOptions()
			.map(opts -> {
				var range = opts.range().offset(cmdName.length() + 2);
				var buff = new StringBuilder();

				if (range.end() > in.length())
					return in + this.getArrow(false);

				buff.append(in.substring(0, range.start() - 1))
					.append(this.applyErrorLevelFormat(new TextFormatter(in.substring(range.start() - 1, range.end()))))
					.append(in.substring(range.end()));

				return buff.toString();
			})
			.orElse(in);
	}


	private void highlightTokens(@NotNull List<@NotNull TextFormatter> tokensFormatters, @NotNull Range range) {
		for (int i : range) {
			this.applyErrorLevelFormat(tokensFormatters.get(i));
		}
	}

	private void placeArrows(@NotNull List<@NotNull TextFormatter> tokensFormatters, @NotNull Range range, int singleOffset) {
		if (!range.isRange()) {
			if (range.start() >= tokensFormatters.size())
				tokensFormatters.add(this.getArrow(false));
			else
				tokensFormatters.add(range.start() + singleOffset, this.getArrow(false));
			return;
		}

		tokensFormatters.add(range.end() + 1, this.getArrow(false));
		tokensFormatters.add(range.start(), this.getArrow(true));
	}

	private void placeArrowsImplicit(@NotNull List<@NotNull TextFormatter> tokensFormatters, @NotNull Range range) {
		this.placeArrows(tokensFormatters, range, 1);
	}

	private void placeArrowsExplicit(@NotNull List<@NotNull TextFormatter> tokensFormatters, @NotNull Range range) {
		this.placeArrows(tokensFormatters, range, 0);
	}

	private @NotNull TextFormatter getArrow(boolean isLeft) {
		return this.applyErrorLevelFormat(new TextFormatter(isLeft ? "->" : "<-"));
	}

	private @NotNull TextFormatter applyErrorLevelFormat(@NotNull TextFormatter formatter) {
		return formatter.withForegroundColor(this.getErrorLevel().color)
			.addFormat(FormatOption.REVERSE, FormatOption.BOLD);
	}
}

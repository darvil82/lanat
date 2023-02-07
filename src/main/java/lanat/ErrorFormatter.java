package lanat;

import lanat.helpRepresentation.HelpFormatter;
import lanat.parsing.errors.ErrorHandler;
import lanat.utils.UtlString;
import lanat.utils.displayFormatter.FormatOption;
import lanat.utils.displayFormatter.TextFormatter;

import java.util.ArrayList;
import java.util.List;

public class ErrorFormatter {
	private String contents = "";
	private DisplayTokensOptions tokensViewOptions;
	private ErrorLevel errorLevel;
	private final ErrorHandler mainErrorHandler;
	public static ErrorFormatterGenerator generator = new ErrorFormatterGenerator();

	public ErrorFormatter(ErrorHandler mainErrorHandler, ErrorLevel level) {
		this.errorLevel = level;
		this.mainErrorHandler = mainErrorHandler;
		ErrorFormatter.generator.setErrorFormatter(this);
	}

	public ErrorFormatter setContents(String contents) {
		this.contents = contents;
		return this;
	}

	public ErrorFormatter setErrorLevel(ErrorLevel errorLevel) {
		this.errorLevel = errorLevel;
		return this;
	}

	@Override
	public String toString() {
		return ErrorFormatter.generator.generate();
	}

	public ErrorFormatter displayTokens(int start, int offset, boolean placeArrow) {
		this.tokensViewOptions = new DisplayTokensOptions(
			start + this.mainErrorHandler.getAbsoluteCmdTokenIndex(), offset, placeArrow
		);
		return this;
	}

	public ErrorFormatter displayTokens(int index) {
		return this.displayTokens(index, 0, true);
	}


	public record DisplayTokensOptions(int start, int offset, boolean placeArrow) {}


	public static class ErrorFormatterGenerator {
		private ErrorFormatter errorFormatter;

		private void setErrorFormatter(ErrorFormatter errorFormatter) {
			this.errorFormatter = errorFormatter;
		}

		public String generate() {
			// first figure out the length of the longest line
			final var maxLength = UtlString.getLongestLine(this.getContents()).length();
			final var formatter = this.getErrorLevelFormatter();
			final String tokensFormatting = this.getTokensViewFormatting();

			return formatter.setContents(" ┌─%s%s".formatted(this.getErrorLevel(), !tokensFormatting.isEmpty() ? "\n" : "")).toString()
				+ tokensFormatting
				// first insert a vertical bar at the start of each line
				+ this.getContentsWrapped().replaceAll("^|\\n", formatter.setContents("\n │ ").toString())
				// then insert a horizontal bar at the end, with the length of the longest line approximately
				+ formatter.setContents("\n └" + "─".repeat(Math.max(maxLength - 5, 0)) + " ───── ── ─")
				+ '\n';
		}

		protected String generateTokensViewFormatting(DisplayTokensOptions options) {
			final var arrow = TextFormatter.ERROR("<-").setColor(this.getErrorLevel().color);
			final var tokensFormatters = new ArrayList<>(this.getTokensFormatters());
			final int tokensLength = tokensFormatters.size();

			if (options.start < 0) {
				tokensFormatters.add(0, arrow);
			} else if (options.start >= tokensLength) {
				tokensFormatters.add(arrow);
			}

			for (int i = 0; i < tokensLength; i++) {
				if (i < this.getAbsoluteCmdTokenIndex()) {
					tokensFormatters.get(i).addFormat(FormatOption.DIM);
				}

				if (i >= options.start && i < options.start + options.offset + 1) {
					if (options.placeArrow) {
						tokensFormatters.add(i, arrow);
					} else {
						tokensFormatters.get(i)
							.setColor(this.getErrorLevel().color)
							.addFormat(FormatOption.REVERSE, FormatOption.BOLD);
					}
				}
			}

			return String.join(" ", tokensFormatters.stream().map(TextFormatter::toString).toList());
		}

		protected final String getTokensViewFormatting() {
			final var options = this.errorFormatter.tokensViewOptions;
			if (options == null) return "";

			final var fmt = this.generateTokensViewFormatting(options);
			if (fmt == null) return "";

			return fmt;
		}

		protected final int getAbsoluteCmdTokenIndex() {
			return this.errorFormatter.mainErrorHandler.getAbsoluteCmdTokenIndex();
		}

		protected final ErrorLevel getErrorLevel() {
			return this.errorFormatter.errorLevel;
		}

		protected final TextFormatter getErrorLevelFormatter() {
			final var formatter = this.getErrorLevel();
			return new TextFormatter(formatter.toString(), formatter.color).addFormat(FormatOption.BOLD);
		}

		protected final List<Token> getTokens() {
			return this.errorFormatter.mainErrorHandler.tokens;
		}

		protected final List<TextFormatter> getTokensFormatters() {
			return this.getTokens().stream().map(Token::getFormatter).toList();
		}

		protected final String getContents() {
			return this.errorFormatter.contents;
		}

		protected final String getContentsWrapped() {
			return UtlString.wrap(this.getContents(), HelpFormatter.lineWrapMax);
		}

		protected final String getContentsSingleLine() {
			return this.getContents().replaceAll("\n", " ");
		}

		protected final Command getRootCommand() {
			return this.errorFormatter.mainErrorHandler.getRootCmd();
		}
	}
}

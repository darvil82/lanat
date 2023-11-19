package lanat.parsing.errors;

import lanat.Argument;
import lanat.ArgumentGroup;
import lanat.ArgumentParser;
import lanat.ErrorLevel;
import lanat.utils.UtlString;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public abstract class ParseErrors {
	private ParseErrors() {}


	public record IncorrectValueNumberError(
		int index,
		@NotNull Argument<?, ?> argument,
		int valueCount,
		boolean isInArgNameList,
		boolean isInTuple
	) implements Error.ParseError
	{
		@Override
		public void handle(@NotNull ErrorFormattingContext fmt, @NotNull ParseContext ctx) {
			fmt
				.withContent("Incorrect number of values for argument '%s'.%nExpected %s, but got %d."
					.formatted(
						this.argument.getName(), this.argument.argType.getRequiredArgValueCount().getMessage("value"),
						this.valueCount
					)
				);

			// special case for when the error is caused by an argument name list
			if (this.isInArgNameList) {
				fmt.highlight(this.index, 0, false);
				return;
			}

			// if the tuple is empty, highlight both tuple tokens
			if (this.isInTuple && this.valueCount == 0) {
				fmt.highlight(this.index, 1, false);
				return;
			}

			fmt.highlight(
				this.index + (this.isInTuple ? 1 : 0),
				Math.max(0, this.valueCount - 1), // only offset if the value count is greater than 1
				this.valueCount == 0
			);
		}
	}

	public record IncorrectUsagesCountError(
		int index,
		@NotNull Argument<?, ?> argument,
		int usageCount
	) implements Error.ParseError
	{
		@Override
		public void handle(@NotNull ErrorFormattingContext fmt, @NotNull ParseContext ctx) {
			fmt
				.withContent("Argument '%s' was used an incorrect amount of times.%nExpected %s, but was used %s."
					.formatted(
						this.argument.getName(), this.argument.argType.getRequiredUsageCount().getMessage("usage"),
						UtlString.plural("time", this.argument.getUsageCount())
					)
				)
				.highlight(this.index, this.usageCount, false);
		}
	}

	public record RequiredArgumentNotUsedError(
		int index,
		@NotNull Argument<?, ?> argument
	) implements Error.ParseError
	{
		@Override
		public void handle(@NotNull ErrorFormattingContext fmt, @NotNull ParseContext ctx) {
			final var argCmd = this.argument.getParentCommand();

			fmt
				.withContent(
					argCmd instanceof ArgumentParser
						? "Required argument '" + this.argument.getName() + "' not used."
						: "Required argument '%s' for command '%s' not used.".formatted(this.argument.getName(), argCmd.getName())
				)
				.highlight(this.index);
		}
	}

	public record UnmatchedTokenError(int index) implements Error.ParseError {
		@Override
		public void handle(@NotNull ErrorFormattingContext fmt, @NotNull ParseContext ctx) {
			fmt
				.withContent(
					"Token '"
						+ ctx.getTokenAt(this.index).contents()
						+ "' does not correspond with a valid argument, argument list, value, or command."
				)
				.highlight(this.index, 0, false);
		}

		@Override
		public @NotNull ErrorLevel getErrorLevel() {
			return ErrorLevel.WARNING;
		}
	}

	public record UnmatchedInArgNameListError(
		int index,
		@NotNull Argument<?, ?> argument,
		@NotNull String errorValue
	) implements Error.ParseError
	{
		@Override
		public void handle(@NotNull ErrorFormattingContext fmt, @NotNull ParseContext ctx) {
			fmt
				.withContent(
					"Argument '" + this.argument.getName() + "' does not take any values, but got '"
						+ this.errorValue + "'."
				)
				.highlight(this.index, 0, false);
		}

		@Override
		public @NotNull ErrorLevel getErrorLevel() {
			return ErrorLevel.WARNING;
		}
	}

	public record MultipleArgsInExclusiveGroupUsedError(
		int index,
		@NotNull ArgumentGroup group,
		int valueCount
	) implements Error.ParseError
	{
		@Override
		public void handle(@NotNull ErrorFormattingContext fmt, @NotNull ParseContext ctx) {
			fmt
				.withContent("Multiple arguments in exclusive group '" + this.group.getName() + "' used.")
				.highlight(this.index, this.valueCount, false);
		}
	}

	public record SimilarArgumentError(
		int index,
		@NotNull Argument<?, ?> argument
	) implements Error.ParseError
	{
		@Override
		public void handle(@NotNull ErrorFormattingContext fmt, @NotNull ParseContext ctx) {
			fmt
				.withContent(
					"Found argument with name given, but with a different prefix ("
						+ this.argument.getPrefix().character
						+ ")."
				)
				.highlight(this.index, 0, false);
		}

		@Override
		public @NotNull ErrorLevel getErrorLevel() {
			return ErrorLevel.WARNING;
		}
	}
}

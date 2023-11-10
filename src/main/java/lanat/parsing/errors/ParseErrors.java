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
	) implements ErrorHandler.ParseErrorHandler
	{
		@Override
		public void handle(@NotNull ErrorFormatter fmt, @NotNull Object ctx) {
			// offset to just show the value tokens (we don't want to highlight the argument token as well)
			final var inTupleOffset = this.isInTuple ? 1 : 0;

			fmt
				.withContent("Incorrect number of values for argument '%s'.%nExpected %s, but got %d."
					.formatted(
						this.argument.getName(), this.argument.argType.getRequiredArgValueCount().getMessage("value"),
						this.valueCount
					)
				);

			if (this.isInArgNameList)
				// special case for when the error is caused by an argument name list
				fmt.highlight(this.index, 0, false);
			else
				fmt.highlight(
					this.index + inTupleOffset,
					this.valueCount - inTupleOffset,
					this.valueCount == 0
				);
		}
	}

	public record IncorrectUsagesCountError(
		int index,
		@NotNull Argument<?, ?> argument,
		int usageCount
	) implements ErrorHandler.ParseErrorHandler
	{
		@Override
		public void handle(@NotNull ErrorFormatter fmt, @NotNull Object ctx) {
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
	) implements ErrorHandler.ParseErrorHandler
	{
		@Override
		public void handle(@NotNull ErrorFormatter fmt, @NotNull Object ctx) {
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

	public record UnmatchedTokenError(int index) implements ErrorHandler.ParseErrorHandler {
		@Override
		public void handle(@NotNull ErrorFormatter fmt, @NotNull Object ctx) {
			fmt
				.withContent(
					"Token '"
						+ this.getCurrentToken().contents()
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
	) implements ErrorHandler.ParseErrorHandler
	{
		@Override
		public void handle(@NotNull ErrorFormatter fmt, @NotNull Object ctx) {
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
	) implements ErrorHandler.ParseErrorHandler
	{
		@Override
		public void handle(@NotNull ErrorFormatter fmt, @NotNull Object ctx) {
			fmt
				.withContent("Multiple arguments in exclusive group '" + this.group.getName() + "' used.")
				.highlight(this.index, this.valueCount, false);
		}
	}
}

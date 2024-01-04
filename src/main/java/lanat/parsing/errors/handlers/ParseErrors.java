package lanat.parsing.errors.handlers;

import lanat.Argument;
import lanat.ArgumentGroup;
import lanat.ArgumentParser;
import lanat.ErrorLevel;
import lanat.parsing.errors.Error;
import lanat.parsing.errors.contexts.ErrorFormattingContext;
import lanat.parsing.errors.contexts.ParseErrorContext;
import org.jetbrains.annotations.NotNull;
import utils.Pair;
import utils.UtlString;

/** Contains all the errors definitions for errors that occur during parsing. */
public abstract class ParseErrors {
	private ParseErrors() {}

	/**
	 * Error that occurs when an argument receives an incorrect number of values.
	 * @param index The index of the token that caused the error.
	 * @param argument The argument that received the incorrect number of values.
	 * @param receivedValueCount The number of values received by the argument.
	 * @param isInArgNameList Whether the error is caused by an argument name list.
	 * @param isInTuple Whether the error is caused by a tuple.
	 */
	public record IncorrectValueNumberError(
		int index,
		@NotNull Argument<?, ?> argument,
		int receivedValueCount,
		boolean isInArgNameList,
		boolean isInTuple
	) implements Error.ParseError
	{
		@Override
		public void handle(@NotNull ErrorFormattingContext fmt, @NotNull ParseErrorContext ctx) {
			fmt
				.withContent("Incorrect number of values for argument '%s'.%nExpected %s, but got %d."
					.formatted(
						this.argument.getName(), this.argument.argType.getRequiredArgValueCount().getMessage("value"),
						this.receivedValueCount
					)
				);

			// special case for when the error is caused by an argument name list
			if (this.isInArgNameList) {
				fmt.highlight(this.index, 0, false);
				return;
			}

			// if the tuple is empty, highlight both tuple tokens
			if (this.isInTuple && this.receivedValueCount == 0) {
				fmt.highlight(this.index, 1, false);
				return;
			}

			fmt.highlight(
				this.index + (this.isInTuple ? 1 : 0),
				Math.max(0, this.receivedValueCount - 1), // only offset if the value count is greater than 1
				this.receivedValueCount == 0
			);
		}
	}

	/**
	 * Error that occurs when an argument is used an incorrect amount of times.
	 * @param indicesPair The indices of the tokens that caused the error. (start, end)
	 * @param argument The argument that was used an incorrect amount of times.
	 */
	public record IncorrectUsagesCountError(
		@NotNull Pair<Integer, Integer> indicesPair,
		@NotNull Argument<?, ?> argument
	) implements Error.ParseError
	{
		@Override
		public void handle(@NotNull ErrorFormattingContext fmt, @NotNull ParseErrorContext ctx) {
			fmt
				.withContent("Argument '%s' was used an incorrect amount of times.%nExpected %s, but was used %s."
					.formatted(
						this.argument.getName(), this.argument.argType.getRequiredUsageCount().getMessage("usage"),
						UtlString.plural("time", this.argument.getUsageCount())
					)
				)
				.highlight(this.indicesPair.first(), this.indicesPair.second(), false);
		}
	}

	/**
	 * Error that occurs when an argument that is required is not used.
	 * @param argument The argument that is required but not used.
	 */
	public record RequiredArgumentNotUsedError(@NotNull Argument<?, ?> argument) implements Error.ParseError {
		@Override
		public void handle(@NotNull ErrorFormattingContext fmt, @NotNull ParseErrorContext ctx) {
			final var argCmd = this.argument.getParentCommand();

			fmt
				.withContent(
					argCmd instanceof ArgumentParser
						? "Required argument '" + this.argument.getName() + "' not used."
						: "Required argument '%s' for command '%s' not used.".formatted(this.argument.getName(), argCmd.getName())
				)
				.highlight(0); // always just highlight at the position of the command token
		}
	}

	/**
	 * Warning that occurs when a token in the input does match any argument, argument list, value, or command.
	 * <br>
	 * @param index The index of the token that caused the error.
	 */
	public record UnmatchedTokenError(int index) implements Error.ParseError {
		@Override
		public void handle(@NotNull ErrorFormattingContext fmt, @NotNull ParseErrorContext ctx) {
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

	/**
	 * Warning that occurs when an argument name list receives a value for an argument that does not take any values.
	 * @param index The index of the token that caused the error.
	 * @param argument The argument that received the value.
	 * @param errorValue The value that was received by the argument.
	 */
	public record UnmatchedInArgNameListError(
		int index,
		@NotNull Argument<?, ?> argument,
		@NotNull String errorValue
	) implements Error.ParseError
	{
		@Override
		public void handle(@NotNull ErrorFormattingContext fmt, @NotNull ParseErrorContext ctx) {
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

	/**
	 * Error that occurs when multiple arguments in a restricted group are used.
	 * @param indicesPair The indices of the tokens that caused the error. (start, end)
	 * @param group The restricted group that contains the arguments.
	 */
	public record MultipleArgsInRestrictedGroupUsedError(
		@NotNull Pair<Integer, Integer> indicesPair,
		@NotNull ArgumentGroup group
	) implements Error.ParseError
	{
		@Override
		public void handle(@NotNull ErrorFormattingContext fmt, @NotNull ParseErrorContext ctx) {
			fmt
				.withContent("Multiple arguments in restricted group '" + this.group.getName() + "' used.")
				.highlight(this.indicesPair.first(), this.indicesPair.second(), false);
		}
	}

	/**
	 * Warning that occurs when the contents of a token are found to be similar to the name of an argument.
	 * @param index The index of the token that caused the error.
	 * @param argument The argument that was found to be similar.
	 */
	public record SimilarArgumentError(
		int index,
		@NotNull Argument<?, ?> argument
	) implements Error.ParseError
	{
		@Override
		public void handle(@NotNull ErrorFormattingContext fmt, @NotNull ParseErrorContext ctx) {
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
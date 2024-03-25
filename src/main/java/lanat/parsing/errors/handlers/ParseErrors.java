package lanat.parsing.errors.handlers;

import lanat.Argument;
import lanat.ArgumentGroup;
import lanat.helpRepresentation.HelpFormatter;
import lanat.parsing.errors.Error;
import lanat.parsing.errors.contexts.ErrorFormattingContext;
import lanat.parsing.errors.contexts.ParseErrorContext;
import lanat.utils.errors.ErrorLevel;
import org.jetbrains.annotations.NotNull;
import utils.Pair;
import utils.UtlString;
import utils.exceptions.DisallowedInstantiationException;

/** Contains all the errors definitions for errors that occur during parsing. */
public abstract class ParseErrors {
	private ParseErrors() {
		throw new DisallowedInstantiationException(ParseErrors.class);
	}

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
				.withContent("Incorrect number of values for argument %s.%nExpected %s, but got %d."
					.formatted(
						HelpFormatter.getRepresentation(this.argument), this.argument.type.getRequiredArgValueCount().getMessage("value"),
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

		@Override
		public boolean shouldRemoveOther(@NotNull Error<?> other) {
			return other instanceof RequiredArgumentNotUsedError requiredArgError
				&& requiredArgError.argument == this.argument;
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
				.withContent("Argument %s was used an incorrect amount of times.%nExpected %s, but was used %s."
					.formatted(
						HelpFormatter.getRepresentation(this.argument), this.argument.type.getRequiredUsageCount().getMessage("usage"),
						UtlString.plural("time", this.argument.getUsageCount())
					)
				)
				.highlight(this.indicesPair, false);
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
			final var argRepr = HelpFormatter.getRepresentation(this.argument);

			fmt
				.withContent(
					argCmd.isRoot()
						? "Required argument " + argRepr + " not used."
						: "Required argument %s for command %s not used.".formatted(argRepr, HelpFormatter.getRepresentation(argCmd))
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
						+ ctx.getTokenAt(ctx.getAbsoluteIndex(this.index)).contents()
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
					"Argument " + HelpFormatter.getRepresentation(this.argument) + " does not take any values, but got '"
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
				.withContent("Multiple arguments in restricted group " + HelpFormatter.getRepresentation(this.group) + " used.")
				.highlight(this.indicesPair, false);
		}
	}

	/**
	 * Warning that occurs when the contents of a token are found to be similar to the name of an argument.
	 * @param index The index of the token that caused the error.
	 * @param argument The argument that was found to be similar.
	 */
	public record SimilarArgumentError(
		int index,
		@NotNull Argument<?, ?> argument,
		boolean isSamePrefix
	) implements Error.ParseError
	{
		@Override
		public void handle(@NotNull ErrorFormattingContext fmt, @NotNull ParseErrorContext ctx) {
			if (this.isSamePrefix)
				fmt.withContent("Found argument with name and prefix given, but token was wrapped in quotes.");
			else
				fmt.withContent(
					"Found argument with name given, but with a different prefix ("
						+ this.argument.getPrefix().getCharacter() + ")."
				);


			fmt.highlight(this.index, 0, false);
		}

		@Override
		public @NotNull ErrorLevel getErrorLevel() {
			return ErrorLevel.WARNING;
		}

		@Override
		public boolean shouldRemoveOther(@NotNull Error<?> other) {
			return other instanceof UnmatchedTokenError unmatchedTokenError
				&& unmatchedTokenError.index == this.index;
		}
	}

	/**
	 * Error that occurs when an argument is used while there's a unique argument that has been used.
	 * @param indicesPair The indices of the tokens that caused the error. (start, end)
	 * @param argument The argument that thrown the error.
	 */
	public record UniqueArgumentUsedError(
		@NotNull Pair<Integer, Integer> indicesPair,
		@NotNull Argument<?, ?> argument
	) implements Error.ParseError
	{
		@Override
		public void handle(@NotNull ErrorFormattingContext fmt, @NotNull ParseErrorContext ctx) {
			fmt
				.withContent(
					"Argument " + HelpFormatter.getRepresentation(this.argument)
						+ " may not be used while "
						+ (this.argument.isUnique() ? "another" : "a")
						+ " unique argument is used."
				)
				.highlight(this.indicesPair, false);
		}

		@Override
		public @NotNull ErrorLevel getErrorLevel() {
			return ErrorLevel.WARNING;
		}
	}
}
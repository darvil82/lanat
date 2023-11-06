package lanat.parsing.errors;

import lanat.Argument;
import lanat.ArgumentGroup;
import lanat.ArgumentParser;
import lanat.ErrorLevel;
import lanat.utils.ErrorLevelProvider;
import lanat.utils.UtlString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class ParseError extends ParseStateErrorBase<ParseError.ParseErrorType> {
	public final @Nullable Argument<?, ?> argument;
	private final int valueCount;
	private boolean isInTuple = false;
	private boolean isInArgNameList = false;
	private ArgumentGroup argumentGroup;

	public enum ParseErrorType implements ErrorLevelProvider {
		REQUIRED_ARGUMENT_NOT_USED,
		UNMATCHED_TOKEN(ErrorLevel.WARNING),
		UNMATCHED_IN_ARG_NAME_LIST(ErrorLevel.WARNING),
		ARG_INCORRECT_VALUE_NUMBER,
		ARG_INCORRECT_USAGES_COUNT,
		MULTIPLE_ARGS_IN_EXCLUSIVE_GROUP_USED;

		private final @NotNull ErrorLevel level;

		ParseErrorType() {
			this.level = ErrorLevel.ERROR;
		}

		ParseErrorType(@NotNull ErrorLevel level) {
			this.level = level;
		}

		@Override
		public @NotNull ErrorLevel getErrorLevel() {
			return this.level;
		}
	}

	public ParseError(@NotNull ParseErrorType type, int index, @Nullable Argument<?, ?> argument, int valueCount) {
		super(type, index);
		this.argument = argument;
		this.valueCount = valueCount;
	}

	public void setArgumentGroup(@NotNull ArgumentGroup argumentGroup) {
		this.argumentGroup = argumentGroup;
	}

	/**
	 * Sets whether the error was caused while parsing values in a tuple.
	 * @param isInTuple whether the error was caused while parsing values in a tuple
	 */
	public void setIsInTuple(boolean isInTuple) {
		 this.isInTuple = isInTuple;
	}

	/**
	 * Sets whether the error was caused by an argument name list.
	 * @param isInArgNameList whether the error was caused by an argument name list
	 */
	public void setIsInArgNameList(boolean isInArgNameList) {
		this.isInArgNameList = isInArgNameList;
	}

	/**
	 * Returns the offset from the token index to the value tokens. Adds 2 if the error was caused while parsing values
	 * in a tuple.
	 * @return the offset from the token index to the value tokens
	 */
	private int getValueTokensOffset() {
		return this.valueCount + (this.isInTuple ? 2 : 0); // 2 for the tuple tokens
	}

	@Handler("ARG_INCORRECT_VALUE_NUMBER")
	protected void handleIncorrectValueNumber() {
		assert this.argument != null;

		// offset to just show the value tokens (we don't want to highlight the argument token as well)
		final var inTupleOffset = this.isInTuple ? 1 : 0;

		this.fmt()
			.setContent("Incorrect number of values for argument '%s'.%nExpected %s, but got %d."
				.formatted(
					this.argument.getName(), this.argument.argType.getRequiredArgValueCount().getMessage("value"),
					this.valueCount
				)
			);

		if (this.isInArgNameList)
			// special case for when the error is caused by an argument name list
			this.fmt().displayTokens(this.tokenIndex, 0, false);
		else
			this.fmt().displayTokens(
				this.tokenIndex + inTupleOffset,
				this.getValueTokensOffset() - inTupleOffset,
				this.getValueTokensOffset() == 0
			);
	}

	@Handler("ARG_INCORRECT_USAGES_COUNT")
	protected void handleIncorrectUsagesCount() {
		assert this.argument != null;

		this.fmt()
			.setContent("Argument '%s' was used an incorrect amount of times.%nExpected %s, but was used %s."
				.formatted(
					this.argument.getName(), this.argument.argType.getRequiredUsageCount().getMessage("usage"),
					UtlString.plural("time", this.argument.getUsageCount())
				)
			)
			.displayTokens(this.tokenIndex, this.getValueTokensOffset(), false);
	}

	@Handler("REQUIRED_ARGUMENT_NOT_USED")
	protected void handleRequiredArgumentNotUsed() {
		assert this.argument != null;
		final var argCmd = this.argument.getParentCommand();

		this.fmt()
			.setContent(
				argCmd instanceof ArgumentParser
					? "Required argument '" + this.argument.getName() + "' not used."
					: "Required argument '%s' for command '%s' not used.".formatted(this.argument.getName(), argCmd.getName())
			)
			.displayTokens(this.tokenIndex);
	}

	@Handler("UNMATCHED_TOKEN")
	protected void handleUnmatchedToken() {
		this.fmt()
			.setContent(
				"Token '"
					+ this.getCurrentToken().contents()
					+ "' does not correspond with a valid argument, argument list, value, or command."
			)
			.displayTokens(this.tokenIndex, 0, false);
	}

	// here we use valueCount as the offset to the unmatched token, to substr the token contents
	@Handler("UNMATCHED_IN_ARG_NAME_LIST")
	protected void handleUnmatchedInArgNameList() {
		assert this.argument != null;

		this.fmt()
			.setContent(
				"Argument '" + this.argument.getName() + "' does not take any values, but got '"
					+ this.getCurrentToken().contents().substring(this.valueCount) + "'."
			)
			.displayTokens(this.tokenIndex, 0, false);
	}

	@Handler("MULTIPLE_ARGS_IN_EXCLUSIVE_GROUP_USED")
	protected void handleMultipleArgsInExclusiveGroupUsed() {
		this.fmt()
			.setContent("Multiple arguments in exclusive group '" + this.argumentGroup.getName() + "' used.")
			.displayTokens(this.tokenIndex, this.getValueTokensOffset(), false);
	}
}

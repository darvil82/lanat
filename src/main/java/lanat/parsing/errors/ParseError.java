package lanat.parsing.errors;

import lanat.Argument;
import lanat.ArgumentGroup;
import lanat.ArgumentParser;
import lanat.ErrorLevel;
import lanat.utils.ErrorLevelProvider;
import lanat.utils.UtlString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class ParseError extends ParseStateErrorBase<ParseError.ParseErrorType> {
	public final Argument<?, ?> argument;
	public final int valueCount;
	private ArgumentGroup argumentGroup;

	public enum ParseErrorType implements ErrorLevelProvider {
		OBLIGATORY_ARGUMENT_NOT_USED,
		UNMATCHED_TOKEN(ErrorLevel.WARNING),
		ARG_INCORRECT_VALUE_NUMBER,
		ARG_INCORRECT_USAGES_COUNT,
		MULTIPLE_ARGS_IN_EXCLUSIVE_GROUP_USED;

		public final @NotNull ErrorLevel level;

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

	public static @NotNull List<@NotNull ParseError> filter(@NotNull List<@NotNull ParseError> errors) {
		final var newList = new ArrayList<>(errors);

		for (final var err : errors) {
			/* if we are going to show an error about an argument being incorrectly used, and that argument is defined
			 * as obligatory, we don't need to show the obligatory error since its obvious that the user knows that
			 * the argument is obligatory */
			if (err.errorType == ParseErrorType.ARG_INCORRECT_VALUE_NUMBER) {
				newList.removeIf(e ->
					e.argument != null
						&& e.argument.equals(err.argument)
						&& e.errorType == ParseErrorType.OBLIGATORY_ARGUMENT_NOT_USED
				);
			}
		}

		return newList;
	}

	@Handler("ARG_INCORRECT_VALUE_NUMBER")
	protected void handleIncorrectValueNumber() {
		assert this.argument != null;

		this.fmt()
			.setContent("Incorrect number of values for argument '%s'.%nExpected %s, but got %d."
				.formatted(
					this.argument.getName(), this.argument.argType.getRequiredArgValueCount().getMessage("value"),
					Math.max(this.valueCount - 1, 0) // this is done because if there are tuples, the end token is counted as a value (maybe a bit hacky?)
				)
			)
			.displayTokens(this.tokenIndex + 1, this.valueCount, this.valueCount == 0);
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
			.displayTokens(this.tokenIndex + 1, this.valueCount, this.valueCount == 0);
	}

	@Handler("OBLIGATORY_ARGUMENT_NOT_USED")
	protected void handleObligatoryArgumentNotUsed() {
		assert this.argument != null;
		final var argCmd = this.argument.getParentCommand();

		this.fmt()
			.setContent(
				argCmd instanceof ArgumentParser
					? "Obligatory argument '%s' not used.".formatted(this.argument.getName())
					: "Obligatory argument '%s' for command '%s' not used.".formatted(this.argument.getName(), argCmd.getName())
			)
			.displayTokens(this.tokenIndex + 1);
	}

	@Handler("UNMATCHED_TOKEN")
	protected void handleUnmatchedToken() {
		this.fmt()
			.setContent("Token '%s' does not correspond with a valid argument, value, or command."
				.formatted(this.getCurrentToken().contents())
			)
			.displayTokens(this.tokenIndex, this.valueCount, false);
	}

	@Handler("MULTIPLE_ARGS_IN_EXCLUSIVE_GROUP_USED")
	protected void handleMultipleArgsInExclusiveGroupUsed() {
		this.fmt()
			.setContent("Multiple arguments in exclusive group '%s' used."
				.formatted(this.argumentGroup.name)
			)
			.displayTokens(this.tokenIndex, this.valueCount, false);
	}
}

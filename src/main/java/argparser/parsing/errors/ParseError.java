package argparser.parsing.errors;

import argparser.Argument;
import argparser.ArgumentGroup;
import argparser.ErrorLevel;
import argparser.utils.ErrorLevelProvider;

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
		MULTIPLE_ARGS_IN_EXCLUSIVE_GROUP_USED;

		public final ErrorLevel level;

		ParseErrorType() {
			this.level = ErrorLevel.ERROR;
		}

		ParseErrorType(ErrorLevel level) {
			this.level = level;
		}

		@Override
		public ErrorLevel getErrorLevel() {
			return this.level;
		}
	}

	public ParseError(ParseErrorType type, int index, Argument<?, ?> argument, int valueCount) {
		super(type, index);
		this.argument = argument;
		this.valueCount = valueCount;
	}

	public void setArgumentGroup(ArgumentGroup argumentGroup) {
		this.argumentGroup = argumentGroup;
	}

	public static List<ParseError> filter(List<ParseError> errors) {
		final var newList = new ArrayList<>(errors);

		for (final var err : errors) {
			/* if we are going to show an error about an argument being incorrectly used, and that argument is defined
			 * as obligatory, we don't need to show the obligatory error since its obvious that the user knows that
			 * the argument is obligatory */
			if (err.errorsEnum == ParseErrorType.ARG_INCORRECT_VALUE_NUMBER) {
				newList.removeIf(e ->
					e.argument != null
						&& e.argument.equals(err.argument)
						&& e.errorsEnum == ParseErrorType.OBLIGATORY_ARGUMENT_NOT_USED
				);
			}
		}

		return newList;
	}

	@Handler("ARG_INCORRECT_VALUE_NUMBER")
	protected void handleIncorrectValueNumber() {
		this.fmt()
			.setContents("Incorrect number of values for argument '%s'.%nExpected %s, but got %d."
				.formatted(
					argument.getName(), argument.argType.getNumberOfArgValues().getMessage(),
					Math.max(this.valueCount - 1, 0)
				)
			)
			.displayTokens(this.tokenIndex + 1, this.valueCount, this.valueCount == 0);
	}

	@Handler("OBLIGATORY_ARGUMENT_NOT_USED")
	protected void handleObligatoryArgumentNotUsed() {
		final var argCmd = argument.getParentCommand();

		this.fmt()
			.setContents(
				argCmd.isRootCommand()
					? "Obligatory argument '%s' not used.".formatted(argument.getName())
					: "Obligatory argument '%s' for command '%s' not used.".formatted(argument.getName(), argCmd.name)
			)
			.displayTokens(this.tokenIndex + 1);
	}

	@Handler("UNMATCHED_TOKEN")
	protected void handleUnmatchedToken() {
		this.fmt()
			.setContents("Token '%s' does not correspond with a valid argument, value, or command."
				.formatted(this.getCurrentToken().contents())
			)
			.displayTokens(this.tokenIndex, this.valueCount, false);
	}

	@Handler("MULTIPLE_ARGS_IN_EXCLUSIVE_GROUP_USED")
	protected void handleMultipleArgsInExclusiveGroupUsed() {
		this.fmt()
			.setContents("Multiple arguments in exclusive group '%s' used."
				.formatted(this.argumentGroup.name)
			)
			.displayTokens(this.tokenIndex, this.valueCount, false);
	}
}

package lanat.parsing.errors;

import lanat.Argument;
import lanat.ErrorLevel;
import lanat.utils.ErrorLevelProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class TokenizeError extends ParseStateErrorBase<TokenizeError.TokenizeErrorType> {
	private final @Nullable Argument<?, ?> argument;

	public enum TokenizeErrorType implements ErrorLevelProvider {
		TUPLE_ALREADY_OPEN,
		UNEXPECTED_TUPLE_CLOSE,
		TUPLE_NOT_CLOSED,
		STRING_NOT_CLOSED,
		SIMILAR_ARGUMENT(ErrorLevel.INFO);

		private final @NotNull ErrorLevel level;

		TokenizeErrorType() {
			this.level = ErrorLevel.ERROR;
		}

		TokenizeErrorType(@NotNull ErrorLevel level) {
			this.level = level;
		}

		@Override
		public @NotNull ErrorLevel getErrorLevel() {
			return this.level;
		}
	}

	public TokenizeError(@NotNull TokenizeErrorType type, int index, @Nullable Argument<?, ?> argument) {
		super(type, index);
		this.argument = argument;
	}

	@Handler("TUPLE_ALREADY_OPEN")
	protected void handleTupleAlreadyOpen() {
		this.fmt()
			.setContent("Tuple already open.")
			.displayTokens(this.tokenIndex, 0, false);
	}

	@Handler("TUPLE_NOT_CLOSED")
	protected void handleTupleNotClosed() {
		this.fmt()
			.setContent("Tuple not closed.")
			.displayTokens(this.tokenIndex + 1);
	}

	@Handler("UNEXPECTED_TUPLE_CLOSE")
	protected void handleUnexpectedTupleClose() {
		this.fmt()
			.setContent("Unexpected tuple close.")
			.displayTokens(this.tokenIndex, 0, false);
	}

	@Handler("STRING_NOT_CLOSED")
	protected void handleStringNotClosed() {
		this.fmt()
			.setContent("String not closed.")
			.displayTokens(this.tokenIndex + 1);
	}

	@Handler("SIMILAR_ARGUMENT")
	protected void handleSimilarArgument() {
		assert this.argument != null;

		this.fmt()
			.setContent(
				"Found argument with name given, but with a different prefix ("
					+ this.argument.getPrefix().character
					+ ")."
			)
			.displayTokens(this.tokenIndex, 0, false);
	}
}

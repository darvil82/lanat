package lanat.parsing.errors;

import lanat.ErrorLevel;
import lanat.utils.ErrorLevelProvider;

@SuppressWarnings("unused")
public class TokenizeError extends ParseStateErrorBase<TokenizeError.TokenizeErrorType> {
	public enum TokenizeErrorType implements ErrorLevelProvider {
		TUPLE_ALREADY_OPEN,
		UNEXPECTED_TUPLE_CLOSE,
		TUPLE_NOT_CLOSED,
		STRING_NOT_CLOSED;

		@Override
		public ErrorLevel getErrorLevel() {
			return ErrorLevel.ERROR;
		}
	}

	public TokenizeError(TokenizeErrorType type, int index) {
		super(type, index);
	}

	@Handler("TUPLE_ALREADY_OPEN")
	protected void handleTupleAlreadyOpen() {
		this.fmt()
			.setContents("Tuple already open.")
			.displayTokens(this.tokenIndex + 1);
	}

	@Handler("TUPLE_NOT_CLOSED")
	protected void handleTupleNotClosed() {
		this.fmt()
			.setContents("Tuple not closed.")
			.displayTokens(this.tokenIndex + 1);
	}

	@Handler("UNEXPECTED_TUPLE_CLOSE")
	protected void handleUnexpectedTupleClose() {
		this.fmt()
			.setContents("Unexpected tuple close.")
			.displayTokens(this.tokenIndex + 1);
	}

	@Handler("STRING_NOT_CLOSED")
	protected void handleStringNotClosed() {
		this.fmt()
			.setContents("String not closed.")
			.displayTokens(this.tokenIndex + 1);
	}
}

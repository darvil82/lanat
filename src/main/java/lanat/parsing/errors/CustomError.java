package lanat.parsing.errors;

import lanat.ErrorLevel;
import lanat.utils.ErrorLevelProvider;

@SuppressWarnings("unused")
public class CustomError extends ParseStateErrorBase<CustomError.CustomParseErrorType> {
	private final String message;
	private final ErrorLevel level;
	private boolean showTokens = true;

	enum CustomParseErrorType implements ErrorLevelProvider {
		DEFAULT;

		@Override
		public ErrorLevel getErrorLevel() {
			return ErrorLevel.ERROR;
		}
	}

	public CustomError(String message, int index, ErrorLevel level) {
		super(CustomParseErrorType.DEFAULT, index);
		this.message = message;
		this.level = level;
	}

	public CustomError(String message, ErrorLevel level) {
		this(message, -1, level);
		this.showTokens = false;
	}

	@Override
	public ErrorLevel getErrorLevel() {
		return this.level;
	}

	@Handler("DEFAULT")
	protected void handleDefault() {
		this.fmt()
			.setErrorLevel(this.level)
			.setContents(this.message);

		if (this.showTokens)
			this.fmt().displayTokens(this.tokenIndex, 0, false);
	}
}

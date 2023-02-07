package lanat.parsing.errors;

import lanat.ErrorLevel;
import lanat.utils.ErrorLevelProvider;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class CustomError extends ParseStateErrorBase<CustomError.CustomParseErrorType> {
	private final @NotNull String message;
	private final @NotNull ErrorLevel level;
	private boolean showTokens = true;

	enum CustomParseErrorType implements ErrorLevelProvider {
		DEFAULT;

		@Override
		public @NotNull ErrorLevel getErrorLevel() {
			return ErrorLevel.ERROR;
		}
	}

	public CustomError(@NotNull String message, int index, @NotNull ErrorLevel level) {
		super(CustomParseErrorType.DEFAULT, index);
		this.message = message;
		this.level = level;
	}

	public CustomError(@NotNull String message, @NotNull ErrorLevel level) {
		this(message, -1, level);
		this.showTokens = false;
	}

	@Override
	public @NotNull ErrorLevel getErrorLevel() {
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

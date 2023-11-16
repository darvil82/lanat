package lanat.parsing.errors;

import lanat.ErrorLevel;
import org.jetbrains.annotations.NotNull;

public class CustomErrorImpl implements Error.CustomError {
	private final @NotNull String message;
	private final @NotNull ErrorLevel errorLevel;
	private int index;

	public CustomErrorImpl(@NotNull String message, @NotNull ErrorLevel errorLevel, int index) {
		this.message = message;
		this.errorLevel = errorLevel;
		this.index = index;
	}

	@Override
	public void handle(@NotNull ErrorFormatter fmt, @NotNull ParseContext ctx) {
		fmt
			.withContent(this.message)
			.highlight(this.index, 0, false);
	}

	@Override
	public int getIndex() {
		return this.index;
	}

	@Override
	public void offsetIndex(int offset) {
		if (offset < 0)
			throw new IllegalArgumentException("offset must be positive");
		this.index += offset;
	}

	@Override
	public @NotNull ErrorLevel getErrorLevel() {
		return this.errorLevel;
	}
}

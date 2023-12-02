package lanat.parsing.errors;

import lanat.Command;
import org.jetbrains.annotations.NotNull;
import utils.Range;

public sealed abstract class ErrorContext permits ParseErrorContext, TokenizeErrorContext {
	protected final @NotNull Command command;

	public ErrorContext(@NotNull Command command) {
		this.command = command;
	}

	public abstract int getAbsoluteIndex(int index);
	public abstract int getCount();

	public int getAbsoluteIndex() {
		return this.getAbsoluteIndex(0);
	}

	public @NotNull Range applyAbsoluteOffset(@NotNull Range range) {
		return range.offset(this.getAbsoluteIndex());
	}

	public @NotNull Command getCommand() {
		return this.command;
	}
}

package lanat.parsing.errors;

import lanat.Command;
import lanat.utils.Range;
import org.jetbrains.annotations.NotNull;

public sealed abstract class BaseContext permits ParseContext, TokenizeContext {
	protected final @NotNull Command command;

	public BaseContext(@NotNull Command command) {
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

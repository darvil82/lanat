package lanat.parsing.errors;

import lanat.Command;
import org.jetbrains.annotations.NotNull;

public abstract class BaseContext {
	protected final @NotNull Command command;

	public BaseContext(@NotNull Command command) {
		this.command = command;
	}

	public abstract int getAbsoluteIndex(int index);
	public abstract int getCount();
}

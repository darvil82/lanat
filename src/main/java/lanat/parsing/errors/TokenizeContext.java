package lanat.parsing.errors;

import lanat.Command;
import org.jetbrains.annotations.NotNull;

public class TokenizeContext {
	private final @NotNull Command command;

	public TokenizeContext(@NotNull Command command) {
		this.command = command;
	}

	public int getAbsoluteIndex(int index) {
		return this.command.getTokenizer().getNestingOffset() + index;
	}
}

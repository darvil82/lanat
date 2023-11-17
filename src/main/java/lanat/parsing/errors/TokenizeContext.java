package lanat.parsing.errors;

import lanat.Command;
import org.jetbrains.annotations.NotNull;

public final class TokenizeContext extends BaseContext {
	public TokenizeContext(@NotNull Command command) {
		super(command);
	}

	@Override
	public int getCount() {
		return this.command.getTokenizer().getInputString().length();
	}

	@Override
	public int getAbsoluteIndex(int index) {
		return this.command.getTokenizer().getNestingOffset() + index;
	}
}

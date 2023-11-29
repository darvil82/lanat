package lanat.parsing.errors;

import lanat.Command;
import org.jetbrains.annotations.NotNull;

public final class TokenizeErrorContext extends ErrorContext {
	private final @NotNull String inputString;

	public TokenizeErrorContext(@NotNull Command command, @NotNull String inputString) {
		super(command);
		this.inputString = inputString;
	}

	@Override
	public int getCount() {
		return this.command.getTokenizer().getInputString().length();
	}

	@Override
	public int getAbsoluteIndex(int index) {
		return this.command.getTokenizer().getNestingOffset() + index;
	}

	public @NotNull String getInputString(boolean onlyInCurrentCommand) {
		if (!onlyInCurrentCommand)
			return this.inputString;

		return this.inputString.substring(
			this.getAbsoluteIndex(),
			this.getAbsoluteIndex(this.getCount())
		);
	}

	public @NotNull String getInputNear(int index, int length) {
		return this.getInputString(true).substring(
			Math.max(0, index - length),
			Math.min(this.getCount(), index + length)
		);
	}
}

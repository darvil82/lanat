package lanat.parsing.errors.contexts;

import lanat.Command;
import org.jetbrains.annotations.NotNull;

/**
 * Context for tokenize errors. Provides methods to get data in relation to the tokenization state.
 */
public final class TokenizeErrorContext extends ErrorContext {
	/** The input string that the main argument parser received. */
	private final @NotNull String inputString;

	/**
	 * Instantiates a new tokenize error context.
	 * @param command the command that was being parsed when the error occurred
	 * @param inputString the input string that the main argument parser received
	 */
	public TokenizeErrorContext(@NotNull Command command, @NotNull String inputString) {
		super(command);
		this.inputString = inputString;
	}

	@Override
	public int getCount() {
		return this.command.getTokenizer().getInputString().length();
	}

	@Override
	public int getAbsoluteIndex() {
		return this.command.getTokenizer().getNestingOffset();
	}

	/**
	 * Returns the input string.
	 * @param onlyInCurrentCommand whether to return the full input string or only the part that was passed to the
	 * 	current command
	 * @return the input string
	 */
	public @NotNull String getInputString(boolean onlyInCurrentCommand) {
		if (!onlyInCurrentCommand)
			return this.inputString;

		return this.inputString.substring(
			this.getAbsoluteIndex(),
			this.getAbsoluteIndex(this.getCount())
		);
	}

	/**
	 * Returns a substring of the input string, centered around the given index.
	 * @param index the index to center around
	 * @param length the length of the substring to apply at each side of the index
	 * @return the substring
	 */
	public @NotNull String getInputNear(int index, int length) {
		return this.getInputString(true).substring(
			Math.max(0, index - length),
			Math.min(this.getCount(), index + length)
		);
	}
}
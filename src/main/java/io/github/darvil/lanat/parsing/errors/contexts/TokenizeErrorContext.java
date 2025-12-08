package io.github.darvil.lanat.parsing.errors.contexts;

import io.github.darvil.lanat.Command;
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
	 * @param length the positive length of the substring to apply at each side of the index.
	 *  A value of {@code 0} will just return the character at that position.
	 * @return the substring
	 */
	public @NotNull String getInputNear(int index, int length) {
		if (length < 0)
			throw new IllegalArgumentException("length must be greater or equal to 0");

		return this.getInputString(true).substring(
			Math.max(0, index - length),
			Math.min(this.getCount(), index + length + 1)
		);
	}
}
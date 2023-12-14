package lanat.parsing.errors;

import lanat.Command;
import lanat.parsing.Token;
import lanat.parsing.TokenType;
import org.jetbrains.annotations.NotNull;
import utils.Range;

import java.util.Collections;
import java.util.List;

/**
 * Context for parse errors. Provides methods to get data in relation to the parsing state (after tokenization).
 */
public final class ParseErrorContext extends ErrorContext {
	/** The final, full list of tokens that were passed to the parser. */
	private final @NotNull List<@NotNull Token> fullTokenList;

	/**
	 * Instantiates a new parse error context.
	 * @param command the command that was being parsed when the error occurred
	 * @param fullTokenList the final, full list of tokens that were passed to the parser
	 */
	public ParseErrorContext(@NotNull Command command, @NotNull List<@NotNull Token> fullTokenList) {
		super(command);
		this.fullTokenList = fullTokenList;
	}


	@Override
	public int getAbsoluteIndex() {
		return this.command.getParser().getNestingOffset();
	}

	@Override
	public int getCount() {
		return this.command.getTokenizer().getFinalTokens().size();
	}

	/**
	 * Returns the token at the given index. If the index is negative, it will be offset from the end of the list.
	 * <p>
	 * If the index is out of bounds, the first or last token will be returned.
	 * </p>
	 * @param index the index of the token to get
	 * @return the token at the given index
	 */
	public @NotNull Token getTokenAt(int index) {
		return this.fullTokenList.get(
			Math.max(0,
				Math.min(
					this.fullTokenList.size() - 1,
					index < 0
						? this.getCount() + index
						: index
				)
			)
		);
	}

	/**
	 * Returns a list of all the tokens in the given range (inclusive).
	 * @param range the range to get the tokens from
	 * @return a list of all the tokens in the given range (inclusive)
	 */
	public @NotNull List<Token> getTokensInRange(@NotNull Range range) {
		return Collections.unmodifiableList(this.fullTokenList.subList(range.start(), range.end() + 1));
	}

	/**
	 * Returns a list of the tokens.
	 * @param onlyInCurrentCommand whether to only return the tokens in the current command
	 * @return a list of the tokens
	 */
	public @NotNull List<Token> getTokens(boolean onlyInCurrentCommand) {
		if (!onlyInCurrentCommand)
			return this.fullTokenList;

		return this.fullTokenList.subList(
			this.getAbsoluteIndex(),
			this.getAbsoluteIndex(this.getCount())
		);
	}

	/**
	 * Returns a token representing the root command.
	 * @return a token representing the root command
	 */
	public @NotNull Token getRootCommandToken() {
		return new Token(TokenType.COMMAND, this.getCommand().getRoot().getName());
	}
}

package lanat.parsing.errors;

import lanat.Command;
import lanat.parsing.Token;
import lanat.parsing.TokenType;
import org.jetbrains.annotations.NotNull;
import utils.Range;

import java.util.List;

public final class ParseContext extends BaseContext {
	private final @NotNull List<@NotNull Token> fullTokenList;

	public ParseContext(@NotNull Command command, @NotNull List<@NotNull Token> fullTokenList) {
		super(command);
		this.fullTokenList = fullTokenList;
	}


	@Override
	public int getAbsoluteIndex(int index) {
		return this.command.getParser().getNestingOffset() + index;
	}

	@Override
	public int getCount() {
		return this.command.getTokenizer().getFinalTokens().size();
	}

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

	public @NotNull List<Token> getTokensInRange(@NotNull Range range) {
		return this.fullTokenList.subList(range.start(), range.end() + 1);
	}

	public @NotNull List<Token> getTokens(boolean onlyInCurrentCommand) {
		if (!onlyInCurrentCommand)
			return this.fullTokenList;

		return this.fullTokenList.subList(
			this.getAbsoluteIndex(),
			this.getAbsoluteIndex(this.getCount())
		);
	}

	public @NotNull Token getRootCommandToken() {
		return new Token(TokenType.COMMAND, this.getCommand().getRoot().getName());
	}
}

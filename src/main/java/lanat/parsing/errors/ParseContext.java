package lanat.parsing.errors;

import lanat.Command;
import lanat.parsing.Token;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ParseContext extends BaseContext {
	private final @NotNull List<@NotNull Token> fullTokenList;

	public ParseContext(@NotNull List<@NotNull Token> fullTokenList, @NotNull Command command) {
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
		return this.fullTokenList.get(this.getAbsoluteIndex(
			index < 0
				? this.getCount() + index
				: index
		));
	}
}

package lanat.parsing.errors;

import lanat.Command;
import lanat.parsing.Token;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ParseContext {
	private final @NotNull List<@NotNull Token> fullTokenList;
	private final @NotNull Command command;


	public ParseContext(@NotNull List<@NotNull Token> fullTokenList, @NotNull Command command) {
		this.fullTokenList = fullTokenList;
		this.command = command;
	}


	public int getAbsoluteIndex(int index) {
		return this.command.getTokenizer().getNestingOffset() + index;
	}

	public int getTokenCount() {
		return this.command.getTokenizer().getFinalTokens().size();
	}

	public @NotNull Token getTokenAt(int index) {
		return this.fullTokenList.get(
			index < 0
				? this.getAbsoluteIndex(this.getTokenCount() + index)
				: this.getAbsoluteIndex(index)
		);
	}
}

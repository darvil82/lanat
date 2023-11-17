package lanat.parsing.errors;

import lanat.Command;
import lanat.parsing.Token;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class ParseContext extends BaseContext {
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

	public @NotNull List<Token> getTokens(boolean onlyInCurrentCommand) {
		if (!onlyInCurrentCommand)
			return this.fullTokenList;

		return this.fullTokenList.subList(
			this.getAbsoluteIndex(),
			this.getAbsoluteIndex(this.getCount())
		);
	}

	public @NotNull List<TextFormatter> getTokensFormatters(boolean onlyInCurrentCommand) {
		return this.getTokens(onlyInCurrentCommand).stream()
			.map(Token::getFormatter)
			.toList();
	}
}

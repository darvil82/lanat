package lanat.parsing.errors;

import lanat.ArgumentParser;
import lanat.parsing.Token;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TokenizeContext {
	private final @NotNull ArgumentParser argumentParser;
	private final @NotNull List<@NotNull Token> tokens;

	public TokenizeContext(@NotNull ArgumentParser argumentParser) {
		this.argumentParser = argumentParser;
		this.tokens = argumentParser.getFullTokenList();
	}

	public @NotNull Token getTokenAt(int index) {
		return this.tokens.get(index < 0 ? this.tokens.size() + index : index);
	}
}

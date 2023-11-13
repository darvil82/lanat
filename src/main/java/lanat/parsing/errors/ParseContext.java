package lanat.parsing.errors;

import lanat.ArgumentParser;
import lanat.parsing.Token;
import org.jetbrains.annotations.NotNull;

public class ParseContext {
	private final @NotNull ArgumentParser argumentParser;

	public ParseContext(@NotNull ArgumentParser argumentParser) {
		this.argumentParser = argumentParser;
	}

	public @NotNull Token getTokenAt(int index) {
		return this.argumentParser.getFullTokenList().get(index < 0 ? this.argumentParser.getFullTokenList().size() + index : index);
	}
}

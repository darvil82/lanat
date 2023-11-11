package lanat.parsing.errors;

import lanat.ArgumentParser;
import org.jetbrains.annotations.NotNull;

public class TokenizeContext {
	private final @NotNull ArgumentParser argumentParser;

	public TokenizeContext(@NotNull ArgumentParser argumentParser) {
		this.argumentParser = argumentParser;
	}
}

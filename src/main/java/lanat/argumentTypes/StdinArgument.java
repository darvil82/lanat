package lanat.argumentTypes;

import lanat.ArgValueCount;
import lanat.ArgumentType;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class StdinArgument extends ArgumentType<String> {
	private final BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

	@Override
	public @NotNull ArgValueCount getNumberOfArgValues() {
		return ArgValueCount.NONE;
	}

	@Override
	public TextFormatter getRepresentation() {
		return null;
	}

	@Override
	public String parseValues(@NotNull String @NotNull [] args) {
		ArrayList<String> input = new ArrayList<>();

		try {
			String line;
			while ((line = this.systemIn.readLine()) != null)
				input.add(line);
		} catch (IOException ignored) {}

		return String.join("\n", input);
	}
}

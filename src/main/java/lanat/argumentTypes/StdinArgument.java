package lanat.argumentTypes;

import lanat.ArgumentType;
import lanat.utils.Range;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Scanner;

public class StdinArgument extends ArgumentType<String> {
	@Override
	public @NotNull Range getRequiredArgValueCount() {
		return Range.NONE;
	}

	@Override
	public TextFormatter getRepresentation() {
		return null;
	}

	@Override
	public String parseValues(@NotNull String @NotNull [] args) {
		final var input = new StringBuilder();

		try (var scanner = new Scanner(System.in)) {
			while (scanner.hasNextLine()) {
				input.append(scanner.nextLine()).append('\n');
			}
		}

		return input.toString();
	}

	@Override
	public @Nullable String getDescription() {
		return "Accepts input from stdin (Standard Input).";
	}
}

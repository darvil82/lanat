package lanat.argumentTypes;

import lanat.ArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import textFormatter.TextFormatter;
import utils.Range;

import java.util.Scanner;

/**
 * An argument type that takes input from stdin (Standard Input).
 * This waits for the user to input something. May be useful for piping input from other programs.
 */
public class StdinArgumentType extends ArgumentType<String> {
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

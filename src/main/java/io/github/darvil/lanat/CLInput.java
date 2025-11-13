package io.github.darvil.lanat;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Scanner;

/**
 * A class to gather the input from the command line.
 */
public final class CLInput {
	/**
	 * The string of arguments passed to the program.
	 */
	public final @NotNull String args;

	private CLInput(@NotNull String args) {
		this.args = args.trim();
	}

	/**
	 * Constructs a new {@link CLInput} from the given arguments array.
	 * @param args The array of arguments.
	 * @return A new {@link CLInput} from the given arguments array.
	 */
	public static @NotNull CLInput from(@NotNull String @NotNull [] args) {
		return new CLInput(String.join(" ", args));
	}

	/**
	 * Constructs a new {@link CLInput} from the given arguments string.
	 * @param args The arguments string.
	 * @return A new {@link CLInput} from the given arguments string.
	 */
	public static @NotNull CLInput from(@NotNull String args) {
		return new CLInput(args);
	}

	/**
	 * Gets the arguments passed to the program from the system property {@code "sun.java.command"}.
	 * @return A new {@link CLInput} from the system property {@code "sun.java.command"}.
	 */
	public static @NotNull CLInput fromSystemProperty() {
		final var args = System.getProperty("sun.java.command");

		// this is just the program name, so no arguments were passed
		if (!args.contains(" "))
			return new CLInput("");

		// remove first word from args (the program name)
		return new CLInput(args.substring(args.indexOf(' ') + 1));
	}

	/**
	 * Constructs a new {@link CLInput} from the given file. The input will be the contents of the file.
	 * If the file does not exist, the input will be an empty string.
	 * @param file The file to read the input from.
	 * @return A new {@link CLInput} from the given file.
	 */
	public static @NotNull CLInput fromFile(@NotNull File file) {
		try {
			return new CLInput(Files.readString(file.toPath()));
		} catch (IOException e) {
			return new CLInput("");
		}
	}

	/**
	 * Constructs a new {@link CLInput} from the standard input. The input will be the first line read.
	 * @return A new {@link CLInput} from the standard input.
	 */
	public static @NotNull CLInput fromStandardInput() {
		try (var scanner = new Scanner(System.in)) {
			return new CLInput(scanner.nextLine());
		}
	}

	/** Returns {@code true} if no arguments were passed to the program. */
	public boolean isEmpty() {
		return this.args.isEmpty();
	}
}
package lanat;

import org.jetbrains.annotations.NotNull;

/**
 * A class to gather the input from the command line.
 */
public final class CLInput {
	/**
	 * The string of arguments passed to the program.
	 */
	public final @NotNull String args;

	private CLInput(@NotNull String args) {
		this.args = args;
	}

	public static @NotNull CLInput from(@NotNull String @NotNull [] args) {
		return new CLInput(String.join(" ", args));
	}

	public static @NotNull CLInput from(@NotNull String args) {
		return new CLInput(args);
	}

	/**
	 * Gets the arguments passed to the program from the system property "sun.java.command".
	 */
	public static @NotNull CLInput fromSystemProperty() {
		final var args = System.getProperty("sun.java.command");

		// remove first word from args (the program name)
		return new CLInput(args.substring(args.indexOf(' ') + 1));
	}
}
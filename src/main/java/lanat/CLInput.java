package lanat;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public final class CLInput {
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

	public static @NotNull CLInput fromSystemProperty() {
		final var args = System.getProperty("sun.java.command").split(" ");
		return CLInput.from(Arrays.copyOfRange(args, 1, args.length));
	}
}
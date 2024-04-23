package lanat;

import lanat.exceptions.ArgumentNotFoundException;
import lanat.exceptions.CommandNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Container for all the parsed arguments and their respective values.
 */
public class ParseResult {
	/** The parsed arguments. Pairs of each argument and it's respective value */
	private final @NotNull HashMap<@NotNull Argument<?, ?>, @Nullable Object> parsedArgumentValues;

	/** The {@link Command} that this {@link ParseResult} object belongs to */
	private final @NotNull Command cmd;

	/** Whether the command was used or not */
	private final boolean wasUsed;

	/** The other inner {@link ParseResult}s for the sub-commands */
	protected final @NotNull List<@NotNull ParseResult> subResults;


	ParseResult(
		@NotNull Command cmd,
		@NotNull HashMap<@NotNull Argument<?, ?>, @Nullable Object> parsedArgumentValues,
		@NotNull List<@NotNull ParseResult> subResults
	) {
		this.parsedArgumentValues = parsedArgumentValues;
		this.cmd = cmd;
		this.wasUsed = cmd.getTokenizer().hasFinished();
		this.subResults = subResults;
	}


	/**
	 * Returns {@code true} if the command was used, {@code false} otherwise.
	 * @return {@code true} if the command was used, {@code false} otherwise
	 */
	public boolean wasUsed() {
		return this.wasUsed;
	}

	/**
	 * Returns the {@link Command} that this {@link ParseResult} object belongs to. This is the Command
	 * that was used to parse the arguments.
	 * @return The {@link Command} that this {@link ParseResult} object belongs to
	 */
	public @NotNull Command getCommand() {
		return this.cmd;
	}


	/**
	 * Returns the parsed value of the argument with the given name.
	 * @param arg The argument to get the value of
	 * @param <T> The type of the value of the argument
	 * @return An {@link Optional} containing the parsed value of the argument with the given name, or
	 *  {@link Optional#empty()} if the argument was not found.
	 */
	@SuppressWarnings("unchecked") // we'll just have to trust the user
	public <T> @NotNull Optional<T> get(@NotNull Argument<?, T> arg) {
		if (!this.parsedArgumentValues.containsKey(arg)) {
			throw new ArgumentNotFoundException(arg);
		}

		return Optional.ofNullable((T)this.parsedArgumentValues.get(arg));
	}

	/**
	 * Specify the route of Sub-Commands for reaching the argument desired.
	 *
	 * <br><br>
	 *
	 * <strong>Example:</strong>
	 * <pre>
	 * {@code var argValue = result.<String>get("rootCommand", "subCommand", "argument")}
	 * </pre>
	 * Returns the parsed value of the argument in the next command hierarchy:
	 * <ul>
	 *     <li>rootCommand
	 *     <ul>
	 *         <li>subCommand
	 *         <ul>
	 *             <li>argument</li>
	 *         </ul>
	 *     </ul>
	 * </ul>
	 *
	 * @throws CommandNotFoundException If the command specified in the route does not exist
	 * @throws ArgumentNotFoundException If the argument specified in the route does not exist
	 * @return An {@link Optional} containing the parsed value of the argument with the given name, or
	 *  {@link Optional#empty()} if the argument was not found.
	 * @param <T> The type of the value of the argument.
	 */
	public <T> @NotNull Optional<T> get(@NotNull String... argRoute) {
		if (argRoute.length == 0) {
			throw new IllegalArgumentException("argument route must not be empty");
		}

		return this.get$recursive(0, argRoute);
	}


	/**
	 * Recursive implementation for {@link #get(String...)}.
	 * @param offset The current offset in the route
	 * @param argRoute The route to the argument
	 * @return The parsed value of the argument with the given name
	 * @param <T> The type of the value of the argument
	 */
	@SuppressWarnings("unchecked") // we'll just have to trust the user
	private <T> @NotNull Optional<T> get$recursive(int offset, @NotNull String... argRoute) {
		if (offset == argRoute.length - 1) {
			return (Optional<T>)this.get(this.getArgument(argRoute[offset]));
		}

		return this.getSubResult(argRoute[offset])
			.get$recursive(offset + 1, argRoute);
	}

	/**
	 * Returns the argument in {@link #parsedArgumentValues} with the given name.
	 * @throws ArgumentNotFoundException If no argument with the given name is found
	 */
	private @NotNull Argument<?, ?> getArgument(@NotNull String name) {
		return this.parsedArgumentValues.keySet().stream()
			.filter(a -> a.hasName(name))
			.findFirst()
			.orElseThrow(() -> new ArgumentNotFoundException(name));
	}

	/**
	 * Returns the sub {@link ParseResult} with the given name. If none is found with the given name,
	 * {@link CommandNotFoundException} is thrown.
	 *
	 * @param name The name of the sub command
	 * @throws CommandNotFoundException If no sub command with the given name is found
	 * @return The sub {@link ParseResult} with the given name
	 */
	public @NotNull ParseResult getSubResult(@NotNull String name) {
		return this.subResults.stream()
			.filter(sub -> sub.cmd.hasName(name))
			.findFirst()
			.orElseThrow(() -> new CommandNotFoundException(name));
	}
}
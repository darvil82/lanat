package lanat;

import lanat.exceptions.ArgumentNotFoundException;
import lanat.exceptions.CommandNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.UtlString;

import java.util.Arrays;
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
	 * Returns the parsed value of the argument with the given name. In order to access arguments in sub-commands, use
	 * the {@code .} separator to specify the route to the argument.
	 *
	 * <br><br>
	 *
	 * <strong>Example:</strong>
	 * <pre>
	 * {@code var argValue = result.<String>get("rootcommand.subCommand.argument")}
	 * </pre>
	 * <p>
	 * More info at {@link #get(String...)}
	 *
	 * @param argRoute The route to the argument, separated by the {@code .} character.
	 * @param <T> The type of the value of the argument. This is used to avoid casting. A type that does not match the
	 * 	argument's type will result in a {@link ClassCastException}.
	 * @return An {@link Optional} containing the parsed value of the argument with the given name, or
	 *  {@link Optional#empty()} if the argument was not found.
	 * @throws CommandNotFoundException If the command specified in the route does not exist
	 * @throws ArgumentNotFoundException If the argument specified in the route does not exist
	 */
	public <T> @NotNull Optional<T> get(@NotNull String argRoute) {
		if (!argRoute.contains("."))
			return this.get(new String[] { argRoute });

		return this.get(UtlString.split(argRoute, '.'));
	}


	/**
	 * Specify the route of Sub-Commands for reaching the argument desired.
	 *
	 * <br><br>
	 *
	 * <strong>Example:</strong>
	 * <pre>
	 * {@code var argValue = result.<String>get("rootcommand", "subCommand", "argument")}
	 * </pre>
	 * Returns the parsed value of the argument in the next command hierarchy:
	 * <ul>
	 *     <li>rootcommand
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
	@SuppressWarnings("unchecked") // we'll just have to trust the user
	public <T> @NotNull Optional<T> get(@NotNull String... argRoute) {
		if (argRoute.length == 0) {
			throw new IllegalArgumentException("argument route must not be empty");
		}

		if (argRoute.length == 1) {
			return (Optional<T>)this.get(this.getArgument(argRoute[0]));
		}

		return this.getSubResult(argRoute[0])
			.get(Arrays.copyOfRange(argRoute, 1, argRoute.length));
	}

	/**
	 * Returns the argument in {@link #parsedArgumentValues} with the given name.
	 * @throws ArgumentNotFoundException If no argument with the given name is found
	 */
	private @NotNull Argument<?, ?> getArgument(@NotNull String name) {
		for (var arg : this.parsedArgumentValues.keySet()) {
			if (arg.hasName(name)) {
				return arg;
			}
		}
		throw new ArgumentNotFoundException(name);
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
		for (var sub : this.subResults)
			if (sub.cmd.hasName(name)) return sub;

		throw new CommandNotFoundException(name);
	}
}
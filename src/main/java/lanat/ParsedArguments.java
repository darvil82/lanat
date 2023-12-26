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
public class ParsedArguments {
	private final @NotNull HashMap<@NotNull Argument<?, ?>, @Nullable Object> parsedArgs;
	private final @NotNull Command cmd;
	private final boolean wasUsed;
	private final @NotNull List<@NotNull ParsedArguments> subParsedArguments;

	ParsedArguments(
		@NotNull Command cmd,
		@NotNull HashMap<@NotNull Argument<?, ?>, @Nullable Object> parsedArgs,
		@NotNull List<@NotNull ParsedArguments> subParsedArguments
	)
	{
		this.parsedArgs = parsedArgs;
		this.cmd = cmd;
		this.wasUsed = cmd.getTokenizer().hasFinished();
		this.subParsedArguments = subParsedArguments;
	}

	public boolean wasUsed() {
		return this.wasUsed;
	}

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
		if (!this.parsedArgs.containsKey(arg)) {
			throw new ArgumentNotFoundException(arg);
		}

		return Optional.ofNullable((T)this.parsedArgs.get(arg));
	}

	/**
	 * Returns the parsed value of the argument with the given name. In order to access arguments in sub-commands, use
	 * the <code>.</code> separator to specify the route to the argument.
	 *
	 * <br><br>
	 *
	 * <strong>Example:</strong>
	 * <pre>
	 * {@code var argValue = parsedArguments.<String>get("rootcommand.subCommand.argument")}
	 * </pre>
	 * <p>
	 * More info at {@link #get(String...)}
	 *
	 * @param argRoute The route to the argument, separated by the <code>.</code> character.
	 * @param <T> The type of the value of the argument. This is used to avoid casting. A type that does not match the
	 * 	argument's type will result in a {@link ClassCastException}.
	 * @return An {@link Optional} containing the parsed value of the argument with the given name, or
	 *  {@link Optional#empty()} if the argument was not found.
	 * @throws CommandNotFoundException If the command specified in the route does not exist
	 * @throws ArgumentNotFoundException If the argument specified in the route does not exist
	 */
	public <T> @NotNull Optional<T> get(@NotNull String argRoute) {
		return this.get(UtlString.split(argRoute, '.'));
	}


	/**
	 * Specify the route of Sub-Commands for reaching the argument desired.
	 *
	 * <br><br>
	 *
	 * <strong>Example:</strong>
	 * <pre>
	 * {@code var argValue = parsedArguments.<String>get("rootcommand", "subCommand", "argument")}
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

		return this.getSubParsedArgs(argRoute[0])
			.get(Arrays.copyOfRange(argRoute, 1, argRoute.length));
	}

	/**
	 * Returns the argument in {@link #parsedArgs} with the given name.
	 * @throws ArgumentNotFoundException If no argument with the given name is found
	 */
	private @NotNull Argument<?, ?> getArgument(@NotNull String name) {
		for (var arg : this.parsedArgs.keySet()) {
			if (arg.hasName(name)) {
				return arg;
			}
		}
		throw new ArgumentNotFoundException(name);
	}

	/**
	 * Returns the sub {@link ParsedArguments} with the given name. If none is found with the given name, returns
	 * {@code null}.
	 *
	 * @param name The name of the sub command
	 * @throws CommandNotFoundException If no sub command with the given name is found
	 * @return The sub {@link ParsedArguments} with the given name, or {@code null} if none is found
	 */
	public @NotNull ParsedArguments getSubParsedArgs(@NotNull String name) {
		for (var sub : this.subParsedArguments)
			if (sub.cmd.hasName(name)) return sub;

		throw new CommandNotFoundException(name);
	}
}
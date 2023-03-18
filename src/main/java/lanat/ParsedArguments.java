package lanat;

import lanat.exceptions.ArgumentNotFoundException;
import lanat.exceptions.CommandNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Container for all the parsed arguments and their respective values.
 */
public class ParsedArguments {
	private final @NotNull HashMap<@NotNull Argument<?, ?>, @Nullable Object> parsedArgs;
	private final @NotNull Command cmd;
	private final @NotNull List<@NotNull ParsedArguments> subParsedArguments;
	private static @NotNull String separator = ".";

	ParsedArguments(
		@NotNull Command cmd,
		@NotNull HashMap<Argument<?, ?>, Object> parsedArgs,
		@NotNull List<ParsedArguments> subParsedArguments
	)
	{
		this.parsedArgs = parsedArgs;
		this.cmd = cmd;
		this.subParsedArguments = subParsedArguments;
	}

	/**
	 * Specifies the separator to use when using the {@link #get(String)} method. By default, this is set to
	 * <code>.</code>
	 * @param separator The separator to use
	 */
	public static void setSeparator(@NotNull String separator) {
		if (separator.isEmpty()) {
			throw new IllegalArgumentException("separator cannot be empty");
		}
		ParsedArguments.separator = separator;
	}

	/**
	 * Returns the parsed value of the argument with the given name.
	 * @param arg The argument to get the value of
	 * @param <T> The type of the value of the argument
	 */
	@SuppressWarnings("unchecked") // we'll just have to trust the user
	public <T> @NotNull ParsedArgument<T> get(@NotNull Argument<?, T> arg) {
		if (!this.parsedArgs.containsKey(arg)) {
			throw new ArgumentNotFoundException(arg);
		}

		return new ParsedArgument<>((T)this.parsedArgs.get(arg));
	}

	/**
	 * Returns the parsed value of the argument with the given name. In order to access arguments in sub-commands, use
	 * the separator specified by {@link #setSeparator(String)}. (By default, this is <code>.</code>)
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
	 * @param argRoute The route to the argument, separated by a separator set by {@link #setSeparator(String)}
	 * 	(default is <code>.</code>)
	 * @param <T> The type of the value of the argument. This is used to avoid casting. A type that does not match the
	 *  argument's type will result in a {@link ClassCastException}.
	 * @throws CommandNotFoundException If the command specified in the route does not exist
	 * @throws ArgumentNotFoundException If the argument specified in the route does not exist
	 */
	public <T> @NotNull ParsedArgument<T> get(@NotNull String argRoute) {
		return this.get(argRoute.split(" *" + Pattern.quote(ParsedArguments.separator) + " *"));
	}


	/**
	 * Specify the route of Sub-Commands for reaching the argument desired. This method will return an {@link Object}
	 * that can be cast to the desired type. However, it is recommended to use the type parameter instead, to avoid
	 * casting.
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
	 *         <li>Sub-Command
	 *         <ul>
	 *             <li>argument</li>
	 *         </ul>
	 *     </ul>
	 * </ul>
	 * @throws CommandNotFoundException If the command specified in the route does not exist
	 */
	@SuppressWarnings("unchecked") // we'll just have to trust the user
	public <T> @NotNull ParsedArgument<T> get(@NotNull String... argRoute) {
		if (argRoute.length == 0) {
			throw new IllegalArgumentException("argument route must not be empty");
		}

		ParsedArguments matchedParsedArgs;

		if (argRoute.length == 1) {
			return (ParsedArgument<T>)this.get(this.getArgument(argRoute[0]));
		} else if ((matchedParsedArgs = this.getSubParsedArgs(argRoute[0])) != null) {
			return matchedParsedArgs.get(Arrays.copyOfRange(argRoute, 1, argRoute.length));
		} else {
			throw new CommandNotFoundException(argRoute[0]);
		}
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
	 * <code>null</code>.
	 * @param name The name of the sub command
	 * @return The sub {@link ParsedArguments} with the given name, or <code>null</code> if none is found
	 */
	public ParsedArguments getSubParsedArgs(@NotNull String name) {
		for (var sub : this.subParsedArguments)
			if (sub.cmd.hasName(name)) return sub;
		return null;
	}


	/**
	 * Container for a parsed argument value.
	 * @param <T> The type of the argument value.
	 */
	public static class ParsedArgument<T> {
		private final @Nullable T value;

		ParsedArgument(@Nullable T value) {
			this.value = value;
		}

		/**
		 * @return The parsed value of the argument, or <code>null</code> if the argument was not parsed.
		 */
		public @Nullable T get() {
			return this.value;
		}

		/**
		 * @return <code>true</code> if the argument was parsed, <code>false</code> otherwise.
		 */
		public boolean defined() {
			return this.value != null;
		}

		/**
		 * Specifies a function to run if the argument was parsed.
		 *
		 * @param onDefined The function to run if the argument was parsed. This function will receive the parsed
		 * 	value.
		 */
		public @NotNull ParsedArgument<T> defined(@NotNull Consumer<T> onDefined) {
			if (this.defined()) onDefined.accept(this.value);
			return this;
		}

		/**
		 * Returns <code>true</code> if the argument was not parsed, <code>false</code> otherwise. If a single value array is passed, and the
		 * argument was parsed, this will set the first value of the array to the parsed value.
		 * @param value A single value array to set the parsed value to if the argument was parsed.
		 * @return <code>true</code> if the argument was parsed, <code>false</code> otherwise.
		 * @throws IllegalArgumentException If the value array is not of length 1
		 */
		public boolean defined(@Nullable T @NotNull [] value) {
			if (value.length != 1) {
				throw new IllegalArgumentException("value must be an array of length 1");
			}

			if (this.defined()) {
				value[0] = this.value;
				return true;
			}

			return false;
		}

		/**
		 * @return <code>true</code> if the argument was not parsed, <code>false</code> otherwise.
		 */
		public boolean undefined() {
			return this.value == null;
		}

		/**
		 * Returns the supplied fallback value if the argument was not parsed, otherwise returns the parsed value.
		 *
		 * @param fallbackValue The fallback value to return if the argument was not parsed.
		 * @return The parsed value if the argument was parsed, otherwise the fallback value.
		 */
		public T undefined(@NotNull T fallbackValue) {
			return this.defined() ? this.value : fallbackValue;
		}

		/**
		 * Specifies a supplier function that will be called when the argument is not parsed. The supplier will be
		 * called and its return value will be returned if so.
		 *
		 * @param fallbackCb The supplier function to call if the argument was not parsed.
		 * @return The parsed value if the argument was parsed, otherwise the value returned by the supplier.
		 */
		public T undefined(@NotNull Supplier<@NotNull T> fallbackCb) {
			return this.defined() ? this.value : fallbackCb.get();
		}

		/**
		 * Specifies a function to run if the argument was not parsed.
		 * @param onUndefined The function to run if the argument was not parsed.
		 */
		public @NotNull ParsedArgument<T> undefined(@NotNull Runnable onUndefined) {
			if (this.undefined()) onUndefined.run();
			return this;
		}

		/**
		 * Returns <code><code>true</code></code> if the argument was parsed and the value matches the given predicate, <code>false</code>
		 * otherwise.
		 *
		 * @param predicate The predicate to test the value against (if the argument was parsed). This predicate will
		 * 	never receive a <code>null</code> value.
		 * @return <code><code>true</code></code> if the argument was parsed and the value matches the given predicate, <code>false</code> otherwise.
		 */
		public boolean matches(@NotNull Predicate<@Nullable T> predicate) {
			return this.defined() && predicate.test(this.value);
		}

		/**
		 * @return A {@link Optional} containing the parsed value if the argument was parsed, or an empty {@link Optional}
		 * 	otherwise.
		 */
		public Optional<T> asOptional() {
			return Optional.ofNullable(this.value);
		}
	}
}
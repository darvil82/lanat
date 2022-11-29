package argparser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Container for all the parsed arguments and their respective values.
 */
public class ParsedArguments {
	private final HashMap<Argument<?, ?>, Object> parsedArgs;
	private final String name;
	private final ParsedArguments[] subArgs;
	private static String separator = ".";

	ParsedArguments(String name, HashMap<Argument<?, ?>, Object> parsedArgs, ParsedArguments[] subArgs) {
		this.parsedArgs = parsedArgs;
		this.name = name;
		this.subArgs = subArgs;
	}

	/**
	 * Specifies the separator to use when using the {@link #get(String)} method.
	 * By default, this is set to <code>.</code>
	 */
	public static void setSeparator(String separator) {
		if (Objects.requireNonNull(separator).isEmpty()) {
			throw new IllegalArgumentException("separator cannot be empty");
		}
		ParsedArguments.separator = separator;
	}

	/**
	 * Returns the parsed value of the argument with the given name.
	 */
	@SuppressWarnings("unchecked") // we'll just have to trust the user
	public <T> ParsedArgument<T> get(Argument<?, T> arg) {
		Objects.requireNonNull(arg);

		if (!this.parsedArgs.containsKey(arg)) {
			throw new IllegalArgumentException("argument '" + arg.getAlias() + "' not found");
		}

		return new ParsedArgument<>((T)this.parsedArgs.get(arg));
	}

	/**
	 * Returns the parsed value of the argument with the given name. In order to access arguments in
	 * sub-commands, use the separator specified by {@link #setSeparator(String)}. (By default, this is <code>.</code>)
	 * <hr>
	 * <h3>For example:</h2>
	 * <pre>
	 * {@code var argValue = parsedArguments.<String>get("rootcommand.subcommand.argument")}
	 * </pre>
	 * 
	 * More info at {@link #get(String...)}
	 *
	 * @param argRoute The route to the argument, separated by a separator set by {@link #setSeparator(String)}
	 * (default is <code>.</code>)
	 */
	public <T> ParsedArgument<T> get(String argRoute) {
		return this.get(argRoute.split("\s*" + Pattern.quote(ParsedArguments.separator) + "\s*"));
	}


	/**
	 * Specify the route of subcommands for reaching the argument desired.
	 * This method will return an {@link Object} that can be cast to the desired type. However, it is recommended
	 * to use the type parameter instead, to avoid casting.
	 *
	 * <hr>
	 *
	 * <h3>For example:</h3>
	 * <pre>
	 * {@code var argValue = parsedArguments.<String>get("rootcommand", "subcommand", "argument")}
	 * </pre>
	 * Returns the parsed value of the argument in the next command hierarchy:
	 * <ul>
	 *     <li>rootcommand
	 *     <ul>
	 *         <li>subcommand
	 *         <ul>
	 *             <li>argument</li>
	 *         </ul>
	 *     </ul>
	 * </ul>
	 */
	@SuppressWarnings("unchecked") // we'll just have to trust the user
	public <T> ParsedArgument<T> get(String... argRoute) {
		if (argRoute.length == 0) {
			throw new IllegalArgumentException("argument route must not be empty");
		}

		Optional<ParsedArguments> matchedSubArg;

		if (argRoute.length == 1) {
			return (ParsedArgument<T>)this.get(this.getArgument(argRoute[0]));
		} else if ((matchedSubArg = Arrays.stream(this.subArgs).filter(sub -> sub.name.equals(argRoute[0])).findFirst()).isPresent()) {
			return matchedSubArg.get().get(Arrays.copyOfRange(argRoute, 1, argRoute.length));
		} else {
			throw new IllegalArgumentException("subcommand '" + argRoute[0] + "' not found");
		}
	}

	/**
	 * Returns the argument in {@link #parsedArgs} with the given alias.
	 */
	private Argument<?, ?> getArgument(String name) {
		for (var arg : this.parsedArgs.keySet()) {
			if (arg.getAlias().equals(name)) {
				return arg;
			}
		}
		throw new IllegalArgumentException("argument '" + name + "' not found");
	}


	/**
	 * Manager for a parsed argument value.
	 */
	public static class ParsedArgument<T> {
		private final T value;

		ParsedArgument(T value) {
			this.value = value;
		}

		/**
		 * Returns the parsed value of the argument. If the argument was not parsed, this will return <code>null</code>.
		 */
		public T get() {
			return this.value;
		}

		/**
		 * Returns true if the argument was parsed, false otherwise.
		 */
		public boolean defined() {
			return this.value != null;
		}

		/**
		 * Returns true if the argument was not parsed, false otherwise.
		 */
		public boolean undefined() {
			return this.value == null;
		}

		public boolean matches(Predicate<T> predicate) {
			return this.defined() && predicate.test(this.value);
		}

		public T undefined(T fallbackValue) {
			return this.defined() ? this.value : fallbackValue;
		}

		public T undefined(Supplier<T> fallbackCb) {
			return this.defined() ? this.value : fallbackCb.get();
		}

		public ParsedArgument<T> undefined(Runnable fallbackCb) {
			if (this.undefined()) fallbackCb.run();
			return this;
		}

		public ParsedArgument<T> defined(Consumer<T> cb) {
			if (this.defined()) cb.accept(this.value);
			return this;
		}
	}
}
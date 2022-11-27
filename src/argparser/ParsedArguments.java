package argparser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class ParsedArguments {
	private final HashMap<Argument<?, ?>, Object> parsedArgs;
	private final String name;
	private final ParsedArguments[] subArgs;
	public static String separator = ".";

	ParsedArguments(String name, HashMap<Argument<?, ?>, Object> parsedArgs, ParsedArguments[] subArgs) {
		this.parsedArgs = parsedArgs;
		this.name = name;
		this.subArgs = subArgs;
	}

	@SuppressWarnings("unchecked") // we'll just have to trust the user
	public <T> ParsedArgument<T> get(Argument<?, T> arg) {
		Objects.requireNonNull(arg);

		if (!this.parsedArgs.containsKey(arg)) {
			throw new IllegalArgumentException("argument '" + arg.getAlias() + "' not found");
		}

		return new ParsedArgument<>((T)this.parsedArgs.get(arg));
	}

	public <T> ParsedArgument<T> get(String argRoute) {
		return this.get(argRoute.split("\s*" + Pattern.quote(ParsedArguments.separator) + "\s*"));
	}

	@SuppressWarnings("unchecked") // we'll just have to trust the user
	public <T> ParsedArgument<T> get(String... argRoute) {
		Optional<ParsedArguments> matchedSubArg;

		if (argRoute.length == 1) {
			return (ParsedArgument<T>)this.get(this.getArgument(argRoute[0]));
		} else if ((matchedSubArg = Arrays.stream(this.subArgs).filter(sub -> sub.name.equals(argRoute[0])).findFirst()).isPresent()) {
			return matchedSubArg.get().get(Arrays.copyOfRange(argRoute, 1, argRoute.length));
		} else {
			throw new IllegalArgumentException("argument '" + argRoute[0] + "' not found");
		}
	}


	private Argument<?, ?> getArgument(String name) {
		for (var arg : this.parsedArgs.keySet()) {
			if (arg.getAlias().equals(name)) {
				return arg;
			}
		}
		throw new IllegalArgumentException("argument '" + name + "' not found");
	}


	public static class ParsedArgument<T> {
		private final T value;

		ParsedArgument(T value) {
			this.value = value;
		}

		public T get() {
			return this.value;
		}

		public boolean defined() {
			return this.value != null;
		}

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
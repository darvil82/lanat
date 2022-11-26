package argparser;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ParsedArguments {
	private final HashMap<Argument<?, ?>, Object> parsedArgs;
	private final String name;
	private final ParsedArguments[] subArgs;

	ParsedArguments(String name, HashMap<Argument<?, ?>, Object> parsedArgs, ParsedArguments[] subArgs) {
		this.parsedArgs = parsedArgs;
		this.name = name;
		this.subArgs = subArgs;
	}

	@SuppressWarnings("unchecked") // we'll just have to trust the user
	public <T> ParsedArgument<T> get(Argument<?, T> arg) {
		return new ParsedArgument<>((T)this.parsedArgs.get(arg));
	}

	@SuppressWarnings("unchecked") // we'll just have to trust the user
	public <T> ParsedArgument<T> get(String arg) {
		return new ParsedArgument<>((T)this.parsedArgs.get(this.getArgument(arg)));
	}


	private <T> Argument<?, ?> getArgument(String name) {
		for (Argument<?, ?> arg : this.parsedArgs.keySet()) {
			if (arg.getAlias().equals(name)) {
				return arg;
			}
		}
		return null;
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
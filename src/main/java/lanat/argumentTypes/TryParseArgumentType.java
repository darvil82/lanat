package lanat.argumentTypes;

import lanat.ArgumentType;
import lanat.exceptions.ArgumentTypeException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;


public class TryParseArgumentType<T> extends ArgumentType<T> {
	private final Function<String, Object> parseMethod;
	private final @NotNull Class<T> type;
	private static final String[] tryParseMethodNames = new String[] { "valueOf", "from", "parse" };


	public TryParseArgumentType(@NotNull Class<T> type) {
		this.type = type;

		if ((this.parseMethod = this.getParseMethod()) == null)
			throw new ArgumentTypeException(
				"Type " + type.getName() + " must have a static valueOf(String), parse(String), "
					+ "or from(String) method, or a constructor that takes a string."
			);
	}

	private static boolean isValidExecutable(Executable executable) {
		return Modifier.isStatic(executable.getModifiers())
			&& executable.getParameterCount() == 1
			&& executable.getParameterTypes()[0] == String.class
			&& Arrays.asList(TryParseArgumentType.tryParseMethodNames).contains(executable.getName());
	}

	private boolean isValidMethod(Method method) {
		return TryParseArgumentType.isValidExecutable(method)
			&& method.getReturnType() == this.type;
	}


	@Override
	protected void addError(@NotNull String value) {
		super.addError("Unable to parse value '" + value + "' as type " + this.type.getSimpleName() + ".");
	}

	private @Nullable Function<String, Object> getParseMethod() {
		// Get a static valueOf(String), a parse(String), or a from(String) method.
		final var method = Stream.of(this.type.getMethods())
			.filter(this::isValidMethod)
			.findFirst();

		// if we found a method, return that.
		if (method.isPresent()) {
			return input -> {
				try {
					return method.get().invoke(null, input);
				} catch (IllegalAccessException | InvocationTargetException exception) {
					this.addError(input);
				}
				return null;
			};
		}

		// Otherwise, try to find a constructor that takes a string.
		final var ctor = Stream.of(this.type.getConstructors())
			.filter(TryParseArgumentType::isValidExecutable)
			.findFirst();

		return ctor.<Function<String, Object>>map(tConstructor -> input -> {
			try {
				return tConstructor.newInstance(input);
			} catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
				this.addError(input);
			}
			return null;
		}).orElse(null);
	}

	@Override
	@SuppressWarnings("unchecked")
	public @Nullable T parseValues(@NotNull String @NotNull [] args) {
		try {
			return (T)this.parseMethod.apply(args[0]);
		} catch (Exception e) {
			throw new ArgumentTypeException("Unable to cast value '" + args[0] + "' to type " + this.type.getSimpleName() + ".", e);
		}
	}

	@Override
	public @Nullable String getDescription() {
		return "A value of type " + this.type.getSimpleName() + ".";
	}
}
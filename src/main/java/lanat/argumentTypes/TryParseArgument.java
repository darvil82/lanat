package lanat.argumentTypes;

import lanat.ArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Function;

public class TryParseArgument<T> extends ArgumentType<T> {
	private final Function<String, Object> parseMethod;
	private final @NotNull Class<T> type;


	public TryParseArgument(@NotNull Class<T> type) {
		this.type = type;

		if ((this.parseMethod = this.getParseMethod()) == null)
			throw new IllegalArgumentException(
				"Type " + type.getName() + " must have a static valueOf(String), parse(String), "
					+ "or from(String) method, or a constructor that takes a string."
			);
	}

	private boolean isValidMethod(Executable executable) {
		return executable.getParameterCount() == 1 && executable.getParameterTypes()[0].equals(String.class);
	}

	private boolean isValidMethod(Method method) {
		return this.isValidMethod((Executable)method) && method.getReturnType() == this.type;
	}

	@Override
	protected void addError(@NotNull String value) {
		super.addError("Unable to parse value '" + value + "' as type " + this.type.getSimpleName() + ".");
	}

	private @Nullable Function<String, Object> getParseMethod() {
		// Get a static valueOf(String), a parse(String), or a from(String) method.
		final var method = Arrays.stream(this.type.getMethods())
			.filter(m -> Modifier.isStatic(m.getModifiers()))
			.filter(this::isValidMethod)
			.filter(m -> (m.getName().equals("valueOf") || m.getName().equals("from") || m.getName().equals("parse")))
			.findFirst();

		// if we found a method, return that.
		if (method.isPresent()) {
			return s -> {
				try {
					return method.get().invoke(null, s);
				} catch (IllegalAccessException | InvocationTargetException e) {
					this.addError(s);
				}
				return null;
			};
		}

		// Otherwise, try to find a constructor that takes a string.
		return Arrays.stream(this.type.getConstructors())
			.filter(this::isValidMethod)
			.findFirst()
			.<Function<String, Object>>map(constructor -> s -> {
				try {
					return constructor.newInstance(s);
				} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
					this.addError(s);
				}
				return null;
			}).orElse(null);
	}

	@Override
	public @Nullable T parseValues(@NotNull String @NotNull [] args) {
		try {
			return this.type.cast(this.parseMethod.apply(args[0]));
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to cast value '" + args[0] + "' to type " + this.type.getSimpleName() + ".", e);
		}
	}
}

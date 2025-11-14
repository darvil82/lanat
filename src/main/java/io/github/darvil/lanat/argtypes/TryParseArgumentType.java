package io.github.darvil.lanat.argtypes;

import io.github.darvil.lanat.ArgumentType;
import io.github.darvil.lanat.ArgumentTypeInfer;
import io.github.darvil.lanat.exceptions.ArgumentTypeException;
import io.github.darvil.lanat.exceptions.ArgumentTypeInferException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * An argument type that attempts to parse a string into the type given in the constructor.
 * <p>
 * The type given must have a static {@code valueOf(String)}, {@code parse(String)}, or {@code from(String)} method,
 * or a constructor that takes a string. If none of these are found, an exception will be thrown.
 * </p>
 * This argument type will first attempt to infer the argument type from the type given in the constructor,
 * and if it fails to do so, it will use the method approach.
 * @param <T> The type to parse the string into.
 */
public class TryParseArgumentType<T> extends ArgumentType<T> {
	/** The method used to parse the string into the type. */
	private final Function<String, Object> parseMethod;

	/** The type to parse the string into. */
	private final @NotNull Class<T> type;

	/** The names of the methods that are used to parse the string into the type. */
	private static final String[] TRY_PARSE_METHOD_NAMES = { "valueOf", "from", "parse" };

	/**
	 * Creates a new argument type that will attempt to parse a string into the type given in the constructor.
	 * @param type The type to parse the string into.
	 */
	public TryParseArgumentType(@NotNull Class<T> type) {
		this.type = type;
		ArgumentType<?> infer = null;

		// first try to infer the type
		try {
			infer = ArgumentTypeInfer.get(type);
		} catch (ArgumentTypeInferException ignored) {}

		if (infer != null) {
			this.registerSubType(infer);
			this.parseMethod = infer::parseValues;
			return;
		}

		// didnt find an inferred type, so use the method approach
		if ((this.parseMethod = this.getParseMethod()) == null)
			throw new ArgumentTypeException(
				"Type " + type.getName() + " must have a static valueOf(String), parse(String), "
					+ "or from(String) method, or a constructor that takes a string."
			);
	}

	/**
	 * Returns {@code true} if the given executable is a valid executable for this argument type.
	 * <p>
	 * A valid executable is:
	 * <ul>
	 * <li>Static</li>
	 * <li>Has one parameter</li>
	 * <li>The parameter is a string</li>
	 * <li>The name of the executable is one of the names in {@link #TRY_PARSE_METHOD_NAMES}</li>
	 * </ul>
	 * @param executable The executable to check
	 * @return {@code true} if the given executable is a valid executable for this argument type
	 */
	private static boolean isValidExecutable(Executable executable) {
		return Modifier.isStatic(executable.getModifiers())
			&& executable.getParameterCount() == 1
			&& executable.getParameterTypes()[0] == String.class
			&& Arrays.asList(TryParseArgumentType.TRY_PARSE_METHOD_NAMES).contains(executable.getName());
	}

	/**
	 * Returns {@code true} if the given method is a valid method for this argument type.
	 * <p>
	 * A valid method is:
	 * <ul>
	 * <li>One that is a valid executable (see {@link #isValidExecutable(Executable)})</li>
	 * <li>Has a return type of the type given in the constructor</li>
	 * </ul>
	 * @param method The method to check
	 * @return {@code true} if the given method is a valid method for this argument type
	 */
	private boolean isValidMethod(Method method) {
		return method.getReturnType() == this.type && TryParseArgumentType.isValidExecutable(method);
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
					this.addError("Unable to parse value '" + input + "' as type " + this.type.getSimpleName() + ".");
				}
				return null;
			};
		}

		// Otherwise, try to find a constructor that takes a string.
		return Stream.of(this.type.getConstructors())
			.filter(TryParseArgumentType::isValidExecutable)
			.findFirst()
			.<Function<String, Object>>map(c -> input -> {
				try {
					return c.newInstance(input);
				} catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
					this.addError(
						"Unable to instantiate type '" + this.type.getSimpleName() + "' with value '" + input + "'."
					);
				}
				return null;
			})
			.orElse(null);
	}

	@Override
	@SuppressWarnings("unchecked")
	public @Nullable T parseValues(@NotNull String @NotNull [] values) {
		try {
			return (T)this.parseMethod.apply(values[0]);
		} catch (ClassCastException e) {
			throw new ArgumentTypeException("Unable to cast value '" + values[0] + "' to type " + this.type.getSimpleName() + ".", e);
		}
	}

	@Override
	public @Nullable String getDescription() {
		return "A value of type " + this.type.getSimpleName() + ".";
	}
}
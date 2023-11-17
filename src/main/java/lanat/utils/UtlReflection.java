package lanat.utils;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public final class UtlReflection {
	private UtlReflection() {}

	/**
	 * Returns the simple name of the given class. If the class is an anonymous class, then the simple name
	 * of the superclass is returned.
	 *
	 * @param clazz The class to get the simple name of.
	 * @return The simple name of the given class.
	 */
	public static @NotNull String getSimpleName(@NotNull Class<?> clazz) {
		String name;

		do {
			name = clazz.getSimpleName();
		} while (name.isEmpty() && ((clazz = clazz.getSuperclass()) != null));

		return name;
	}

	/**
	 * Returns whether the given method has the given parameters in the given order.
	 *
	 * @param method The method to check.
	 * @param parameters The parameters to check.
	 * @return Whether the given method has the given parameters in the given order.
	 */
	public static boolean hasParameters(Method method, Class<?>... parameters) {
		return Arrays.equals(method.getParameterTypes(), parameters);
	}

	/**
	 * Instantiates the given class with a no-argument constructor.
	 * @param clazz The class to instantiate.
	 * @param <T> The type of the class.
	 * @return The instantiated class. If the class could not be instantiated, a {@link RuntimeException} is thrown.
	 */
	public static <T> T instantiate(Class<T> clazz) {
		return UtlReflection.instantiate(clazz, List.of(), List.of());
	}

	/**
	 * Instantiates the given class with the given arguments.
	 * @param clazz The class to instantiate.
	 * @param args The arguments to pass to the constructor.
	 * @param <T> The type of the class.
	 * @return The instantiated class. If the class could not be instantiated, a {@link RuntimeException} is thrown.
	 */
	public static <T> T instantiate(Class<T> clazz, @NotNull List<@NotNull Object> args) {
		return UtlReflection.instantiate(
			clazz,
			args.stream().map(Object::getClass).toList(),
			args
		);
	}


	/**
	 * Instantiates the given class with the given arguments.
	 * @param clazz The class to instantiate.
	 * @param argTypes The types of the arguments to pass to the constructor.
	 * @param args The arguments to pass to the constructor.
	 * @param <T> The type of the class.
	 * @return The instantiated class. If the class could not be instantiated, a {@link RuntimeException} is thrown.
	 */
	public static <T> T instantiate(
		Class<T> clazz,
		@NotNull List<? extends Class<?>> argTypes,
		@NotNull List<Object> args
	) {
		try {
			return clazz.getConstructor(argTypes.toArray(Class[]::new)).newInstance(args.toArray());
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Unable to find a public constructor for the class '" + clazz.getName()
				+ """
			'. Please, make sure:
			  - This class has a public constructor with the parameters: %s
			  - This is a static class. (Not an inner class)""".formatted(argTypes)
			);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(
				"Unable to gain access to the class '" + clazz.getName()
					+ "'. Please, make sure this class is visible to Lanat."
			);
		} catch (InstantiationException e) {
			throw new RuntimeException(
				"Unable to instantiate the class '" + clazz.getName()
					+ "'. Please, make sure this class is not abstract."
			);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns a stream of all methods in the given class.
	 * If the given class is an anonymous class, then the methods of the superclass are returned.
	 * @param clazz The class to get the methods of.
	 * @return A stream of all methods in the given class.
	 */
	public static Stream<Method> getMethods(Class<?> clazz) {
		if (clazz.isAnonymousClass())
			return UtlReflection.getMethods(clazz.getSuperclass());
		return Stream.of(clazz.getDeclaredMethods());
	}
}

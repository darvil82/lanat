package lanat.utils;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
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
	 * Instantiates the given class with the given arguments.
	 *
	 * @param clazz The class to instantiate.
	 * @param args The arguments to pass to the constructor.
	 * @param <T> The type of the class.
	 * @return The instantiated class. If the class could not be instantiated, a {@link RuntimeException} is thrown.
	 */
	public static <T> T instantiate(Class<T> clazz, Object... args) {
		try {
			return clazz.getDeclaredConstructor().newInstance(args);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Unable to find a public constructor for the class '" + clazz.getName()
				+ """
			'. Please, make sure:
			  - This class has a public constructor with no arguments. (Or no constructor at all)
			  - This is a static class. (Not an inner class)"""
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

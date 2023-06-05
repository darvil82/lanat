package lanat.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class UtlReflection {
	private UtlReflection() {}

	/**
	 * This method returns the simple name of the given class. If the class is an anonymous class, then the simple name
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
	 * Returns the superclass of the given class that matches the given predicate.
	 *
	 * @param clazz The class to get the superclass of.
	 * @param pred The predicate to match.
	 * @param inclusive Whether to include the given class in the search.
	 * @param <T> The type of the class.
	 * @return The class that matches the given predicate, or null if none was found.
	 */
	public static <T>
	@Nullable Class<? super T> getSuperclassUntil(
		@NotNull Class<T> clazz,
		@NotNull Predicate<Class<? super T>> pred,
		boolean inclusive
	)
	{
		if (inclusive && pred.test(clazz))
			return clazz;

		var superclass = clazz.getSuperclass();

		while (superclass != null && !pred.test(superclass))
			superclass = superclass.getSuperclass();

		return superclass;
	}

	/**
	 * Returns the superclass of the given class that matches the given predicate.
	 *
	 * @param clazz The class to get the superclass of.
	 * @param pred The predicate to match.
	 * @param <T> The type of the class.
	 * @return The class that matches the given predicate, or null if none was found.
	 */
	public static <T> @Nullable Class<? super T> getSuperclassUntil(
		@NotNull Class<T> clazz,
		@NotNull Predicate<Class<? super T>> pred
	)
	{
		return UtlReflection.getSuperclassUntil(clazz, pred, false);
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
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	public static Stream<Method> getMethods(Class<?> clazz) {
		if (clazz.isAnonymousClass())
			return UtlReflection.getMethods(clazz.getSuperclass());
		return Stream.of(clazz.getDeclaredMethods());
	}

}

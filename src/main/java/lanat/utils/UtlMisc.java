package lanat.utils;

import lanat.CommandUser;
import lanat.MultipleNamesAndDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public final class UtlMisc {
	private UtlMisc() {}

	/**
	 * Checks if the elements in the given list are unique.
	 * @param list The list to check
	 * @param exceptionSupplier A function that takes the duplicate element and returns an exception to throw
	 * @param <T> The type of the elements in the list
	 */
	public static <T> void requireUniqueElements(
		@NotNull List<T> list,
		@NotNull Function<T, RuntimeException> exceptionSupplier
	) {
		for (int i = 0; i < list.size(); i++) {
			final var el = list.get(i);

			for (int j = i + 1; j < list.size(); j++) {
				final var other = list.get(j);

				if (el.equals(other))
					throw exceptionSupplier.apply(other);
			}
		}
	}

	/**
	 * Returns {@code true} if the given objects have the same names and parent command.
	 * @param a The first object
	 * @param b The second object
	 * @return {@code true} if the given objects have the same names and parent command
	 * @param <T> The type of the objects
	 */
	public static <T extends MultipleNamesAndDescription & CommandUser>
	boolean equalsByNamesAndParentCmd(@NotNull T a, @NotNull T b) {
		return a.getParentCommand() == b.getParentCommand() && a.getNames().stream().anyMatch(b::hasName);
	}

	/**
	 * Returns {@code obj} if it is not {@code null}, otherwise returns {@code defaultObj}.
	 * @param obj The object to check
	 * @param defaultObj The object to return if {@code obj} is {@code null}
	 * @return {@code obj} if it is not {@code null}, otherwise returns {@code defaultObj}
	 * @param <T> The type of the objects
	 */
	public static <T> @NotNull T nonNullOrElse(@Nullable T obj, @NotNull T defaultObj) {
		return obj == null ? defaultObj : obj;
	}

	/**
	 * Returns {@code null} if {@code obj} is {@code null}, otherwise returns the result of the given function.
	 * @param obj The object to check
	 * @param defaultObj The function to apply to {@code obj} if it is not {@code null}
	 * @return {@code null} if {@code obj} is {@code null}, otherwise returns the result of the given function
	 * @param <T> The type of the objects
	 * @param <R> The type of the result of the function
	 */
	public static <T, R> @Nullable R nullOrElse(@Nullable T obj, @NotNull Function<@NotNull T, @NotNull R> defaultObj) {
		return obj == null ? null : defaultObj.apply(obj);
	}
}

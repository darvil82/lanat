package lanat.utils;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public final class UtlMisc {
	private UtlMisc() {
		throw new AssertionError("This class should not be instantiated");
	}

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
			for (int j = i + 1; j < list.size(); j++) {
				final var other = list.get(j);

				if (list.get(i).equals(other))
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
	 * Returns the last element of the given list.
	 * @param list The list to get the last element from
	 * @return The last element of the given list
	 * @param <T> The type of the elements in the list
	 */
	public static <T> T last(@NotNull List<T> list) {
		return list.get(list.size() - 1);
	}
}
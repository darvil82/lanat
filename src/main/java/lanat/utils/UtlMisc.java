package lanat.utils;

import lanat.CommandUser;
import lanat.MultipleNamesAndDescription;
import lanat.exceptions.ObjectAlreadyExistsException;
import org.jetbrains.annotations.NotNull;

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
		@NotNull Function<T, ObjectAlreadyExistsException> exceptionSupplier
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
}

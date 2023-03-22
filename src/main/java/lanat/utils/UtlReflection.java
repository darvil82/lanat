package lanat.utils;

import org.jetbrains.annotations.NotNull;

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
}

package lanat.argumentTypes;

import org.jetbrains.annotations.NotNull;

/**
 * An argument type that takes a valid enum value.
 * <p>
 * The user can specify any of the enum values by their names.
 * The names are case-insensitive.
 * </p>
 * @param <T> The enum type.
 */
public class EnumArgumentType<T extends Enum<T>> extends SingleValueListArgumentType<T> {
	/**
	 * Creates a new enum argument type.
	 * @param defaultValue The default value of the enum type. This is also used to infer the type of the enum.
	 */
	public EnumArgumentType(@NotNull T defaultValue) {
		super(defaultValue.getDeclaringClass().getEnumConstants(), defaultValue);
	}

	/**
	 * Creates a new enum argument type.
	 * @param clazz The class of the enum type to use.
	 */
	public EnumArgumentType(@NotNull Class<T> clazz) {
		super(clazz.getEnumConstants());
	}

	@Override
	protected boolean predicate(@NotNull T value, @NotNull String strValue) {
		return value.name().equalsIgnoreCase(strValue);
	}

	@Override
	protected @NotNull String valueToString(@NotNull T value) {
		return value.name();
	}
}
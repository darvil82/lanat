package lanat.argumentTypes;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Optional;

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
	 * @param clazz The class of the enum type to use.
	 */
	public EnumArgumentType(@NotNull Class<T> clazz) {
		super(clazz.getEnumConstants());
		this.setDefault(clazz);
	}

	/**
	 * Sets the default value of the enum type by using the {@link Default} annotation.
	 * @param clazz The class of the enum type to use.
	 */
	private void setDefault(@NotNull Class<T> clazz) {
		var defaultFields = Arrays.stream(clazz.getDeclaredFields())
			.filter(f -> f.isAnnotationPresent(Default.class))
			.toList();

		if (defaultFields.isEmpty())
			return;

		if (defaultFields.size() > 1)
			throw new IllegalArgumentException("Only one default value can be set.");

		this.setInitialValue(
			Arrays.stream(this.listValues)
				.filter(v -> v.name().equals(defaultFields.get(0).getName()))
				.findFirst()
				.orElseThrow()
		);
	}

	@Override
	protected @NotNull String valueToString(@NotNull T value) {
		try {
			return Optional.ofNullable(value.getClass().getField(value.name()).getAnnotation(WithName.class))
				.map(WithName::value)
				.orElseGet(value::name);
		} catch (NoSuchFieldException e) {
			return value.name();
		}
	}

	/** An annotation that specifies the name the user will have to write to select this value. */
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface WithName {
		String value();
	}

	/** An annotation that specifies the default value of the enum type. */
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Default { }
}
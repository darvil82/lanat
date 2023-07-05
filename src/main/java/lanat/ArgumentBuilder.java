package lanat;

import lanat.argumentTypes.*;
import lanat.utils.UtlReflection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Provides easy ways to build an {@link Argument}.
 * @param <Type> the {@link ArgumentType} subclass that will parse the value passed to the argument
 * @param <TInner> the actual type of the value passed to the argument
 */
public class ArgumentBuilder<Type extends ArgumentType<TInner>, TInner> {
	private @NotNull String @Nullable [] names;
	private @Nullable String description;
	private @Nullable Type argType;
	private boolean obligatory = false,
		positional = false,
		allowUnique = false;
	private @Nullable TInner defaultValue;
	private @Nullable Consumer<@NotNull Argument<Type, TInner>> onErrorCallback;
	private @Nullable Consumer<@NotNull TInner> onCorrectCallback;
	private @Nullable Argument.PrefixChar prefixChar = Argument.PrefixChar.defaultPrefix;

	ArgumentBuilder() {}

	// mapping of types to their corresponding argument types
	private static final HashMap<Class<?>, Class<? extends ArgumentType<?>>> INFER_ARGUMENT_TYPES_MAP = new HashMap<>() {
		private void put(
			Class<?> boxed,
			Class<?> primitive,
			Class<? extends ArgumentType<?>> value
		) {
			// I wish I didn't have to do this
			this.put(boxed, value);
			this.put(primitive, value);
		}

		{
			this.put(String.class, StringArgumentType.class);
			this.put(int.class, Integer.class, IntegerArgumentType.class);
			this.put(boolean.class, Boolean.class, BooleanArgumentType.class);
			this.put(float.class, Float.class, FloatArgumentType.class);
			this.put(double.class, Double.class, DoubleArgumentType.class);
			this.put(long.class, Long.class, LongArgumentType.class);
			this.put(short.class, Short.class, ShortArgumentType.class);
			this.put(byte.class, Byte.class, ByteArgumentType.class);
		}
	};

	/**
	 * Returns an {@link ArgumentType} instance based on the specified field. If the annotation specifies a type,
	 * it will be used. Otherwise, the type will be inferred from the field type. If the type cannot be inferred,
	 * null will be returned.
	 * <strong>Note: </strong> Expects the field to be annotated with {@link Argument.Define}
	 *
	 * @param field the field that will be used to build the argument
	 * @return the built argument type
	 */
	private static @Nullable ArgumentType<?> getArgumentTypeFromField(@NotNull Field field) {
		final var annotation = field.getAnnotation(Argument.Define.class);
		assert annotation != null : "The field must have an Argument.Define annotation.";

		// if the type is not a dummy type (it was specified on the annotation), instantiate it and return it
		if (annotation.argType() != DummyArgumentType.class)
			return UtlReflection.instantiate(annotation.argType());

		// try to infer the type from the field type
		var argTypeMap = INFER_ARGUMENT_TYPES_MAP.get(field.getType());

		// if the type was not found, return null
		return argTypeMap == null ? null : UtlReflection.instantiate(argTypeMap);
	}

	/**
	 * Builds an {@link Argument} from the specified field annotated with {@link Argument.Define}.
	 *
	 * @param field the field that will be used to build the argument
	 * @param <Type> the {@link ArgumentType} subclass that will parse the value passed to the argument
	 * @param <TInner> the actual type of the value passed to the argument
	 * @return the built argument
	 */
	@SuppressWarnings("unchecked")
	public static @NotNull <Type extends ArgumentType<TInner>, TInner>
	ArgumentBuilder<Type, TInner> fromField(@NotNull Field field) {
		final var annotation = field.getAnnotation(Argument.Define.class);

		if (annotation == null)
			throw new IllegalArgumentException("The field must have an Argument.Define annotation.");

		final var argumentBuilder = new ArgumentBuilder<Type, TInner>()
			.withNames(ArgumentBuilder.getTemplateFieldNames(field));

		// if the type is not DummyArgumentType, instantiate it
		var argType = ArgumentBuilder.getArgumentTypeFromField(field);
		if (argType != null) argumentBuilder.withArgType((Type)argType);

		argumentBuilder.withPrefix(Argument.PrefixChar.fromCharUnsafe(annotation.prefix()));
		if (!annotation.description().isEmpty()) argumentBuilder.withDescription(annotation.description());
		if (annotation.obligatory()) argumentBuilder.obligatory();
		if (annotation.positional()) argumentBuilder.positional();
		if (annotation.allowUnique()) argumentBuilder.allowsUnique();

		return argumentBuilder;
	}

	/**
	 * Builds an {@link Argument} from the specified field name in the specified {@link CommandTemplate} subclass.
	 *
	 * @param templateClass the {@link CommandTemplate} subclass that contains the field
	 * @param fieldName the name of the field that will be used to build the argument
	 * @param <Type> the {@link ArgumentType} subclass that will parse the value passed to the argument
	 * @param <TInner> the actual type of the value passed to the argument
	 * @return the built argument
	 */
	public static @NotNull <Type extends ArgumentType<TInner>, TInner>
	ArgumentBuilder<Type, TInner> fromField(
		@NotNull Class<? extends CommandTemplate> templateClass,
		@NotNull String fieldName
	)
	{
		return ArgumentBuilder.fromField(Stream.of(templateClass.getDeclaredFields())
			.filter(f -> f.isAnnotationPresent(Argument.Define.class))
			.filter(f -> f.getName().equals(fieldName))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("The field " + fieldName + " does not exist in "
				+ "the template class " + templateClass.getSimpleName())
			)
		);
	}

	/**
	 * Returns the names of the argument, either the ones specified in the {@link Argument.Define} annotation or the
	 * field name if the names are empty.
	 * <strong>Note: </strong> Expects the field to be annotated with {@link Argument.Define}
	 *
	 * @param field the field that will be used to get the names. It must have an {@link Argument.Define} annotation.
	 * @return the names of the argument
	 */
	static @NotNull String[] getTemplateFieldNames(@NotNull Field field) {
		final var annotation = field.getAnnotation(Argument.Define.class);
		assert annotation != null : "The field must have an Argument.Define annotation.";

		final var annotationNames = annotation.names();

		// if the names are empty, use the field name
		return annotationNames.length == 0
			? new String[] { field.getName() }
			: annotationNames;
	}

	/**
	 * Returns {@code true} if the specified name is one of the names of the argument.
	 *
	 * @param name the name that will be checked
	 * @return {@code true} if the specified name is one of the names of the argument
	 */
	boolean hasName(@NotNull String name) {
		return this.names != null && Arrays.asList(this.names).contains(name);
	}

	/** @see Argument#setDescription(String) */
	public ArgumentBuilder<Type, TInner> withDescription(@NotNull String description) {
		this.description = description;
		return this;
	}

	/** @see Argument#setObligatory(boolean) */
	public ArgumentBuilder<Type, TInner> obligatory() {
		this.obligatory = true;
		return this;
	}

	/** @see Argument#setPositional(boolean) */
	public ArgumentBuilder<Type, TInner> positional() {
		this.positional = true;
		return this;
	}

	/** @see Argument#setAllowUnique(boolean) */
	public ArgumentBuilder<Type, TInner> allowsUnique() {
		this.allowUnique = true;
		return this;
	}

	/** @see Argument#setDefaultValue(Object) */
	public ArgumentBuilder<Type, TInner> withDefaultValue(@NotNull TInner defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}

	/** @see Argument#setOnCorrectCallback(Consumer) */
	public ArgumentBuilder<Type, TInner> onOk(@NotNull Consumer<TInner> callback) {
		this.onCorrectCallback = callback;
		return this;
	}

	/** @see Argument#setOnErrorCallback(Consumer) */
	public ArgumentBuilder<Type, TInner> onErr(@NotNull Consumer<Argument<Type, TInner>> callback) {
		this.onErrorCallback = callback;
		return this;
	}

	/** @see Argument#setPrefix(Argument.PrefixChar) */
	public ArgumentBuilder<Type, TInner> withPrefix(@NotNull Argument.PrefixChar prefixChar) {
		this.prefixChar = prefixChar;
		return this;
	}

	/** @see Argument#addNames(String...) */
	public ArgumentBuilder<Type, TInner> withNames(@NotNull String... names) {
		this.names = names;
		return this;
	}

	/**
	 * The Argument Type is the class that will be used to parse the argument value. It handles the conversion from the
	 * input string to the desired type.
	 *
	 * @see ArgumentType
	 * @see Argument#argType
	 */
	public ArgumentBuilder<Type, TInner> withArgType(@NotNull Type argType) {
		this.argType = argType;
		return this;
	}

	/**
	 * Builds the argument.
	 *
	 * @return the built argument
	 */
	public Argument<Type, TInner> build() {
		if (this.names == null || this.names.length == 0)
			throw new IllegalStateException("The argument must have at least one name.");

		if (this.argType == null)
			throw new IllegalStateException("The argument must have a type defined.");

		return new Argument<>(this.argType, this.names) {{
			this.setDescription(ArgumentBuilder.this.description);
			this.setObligatory(ArgumentBuilder.this.obligatory);
			this.setPositional(ArgumentBuilder.this.positional);
			this.setAllowUnique(ArgumentBuilder.this.allowUnique);
			this.setDefaultValue(ArgumentBuilder.this.defaultValue);
			this.setPrefix(ArgumentBuilder.this.prefixChar);
			this.setOnErrorCallback(ArgumentBuilder.this.onErrorCallback);
			this.setOnCorrectCallback(ArgumentBuilder.this.onCorrectCallback);
		}};
	}
}

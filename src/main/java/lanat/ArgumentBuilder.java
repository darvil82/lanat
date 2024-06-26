package lanat;

import lanat.exceptions.ArgumentTypeInferException;
import lanat.exceptions.CommandTemplateException;
import lanat.utils.Builder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import textFormatter.color.Color;
import utils.UtlReflection;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Provides easy ways to build an {@link Argument}.
 * @param <Type> the {@link ArgumentType} subclass that will parse the value passed to the argument
 * @param <TInner> the actual type of the value passed to the argument
 */
public class ArgumentBuilder<Type extends ArgumentType<TInner>, TInner> implements Builder<Argument<Type, TInner>> {
	private @NotNull String @Nullable [] names;
	private @Nullable String description;
	private @Nullable Type type;
	private boolean required = false,
		positional = false,
		unique = false,
		visible = true;
	private @Nullable TInner defaultValue;
	private @Nullable Consumer<@NotNull Argument<Type, TInner>> onErrorCallback;
	private @Nullable Consumer<@NotNull TInner> onCorrectCallback;
	private @Nullable Argument.Prefix prefix = Argument.Prefix.DEFAULT;
	private @Nullable Color representationColor;

	ArgumentBuilder() {}


	/**
	 * Returns an {@link ArgumentType} instance based on the specified field. If the annotation specifies a type,
	 * it will be used. Otherwise, the type will be inferred from the field type. If the type cannot be inferred,
	 * null will be returned.
	 * <strong>Note: </strong> Expects the field to be annotated with {@link Argument.Define}
	 *
	 * @param field the field that will be used to build the argument
	 * @return the built argument type
	 */
	public static @Nullable ArgumentType<?> getArgumentTypeFromField(@NotNull Field field) {
		final var annotation = field.getAnnotation(Argument.Define.class);
		assert annotation != null : "The field must have an @Argument.Define annotation.";

		var annotationType = annotation.type();
		var annotationTypeDefined = annotationType != Void.class;

		// if the type is not a Void, instantiate it or try to infer it if its not an ArgumentType
		if (annotationTypeDefined && ArgumentType.class.isAssignableFrom(annotationType))
			return (ArgumentType<?>)UtlReflection.instantiate(annotationType);

		// try to infer the type from the field type. If it can't be inferred, return null
		try {
			return ArgumentTypeInfer.get(annotationTypeDefined ? annotationType : field.getType());
		} catch (ArgumentTypeInferException e) {
			return null;
		}
	}

	/**
	 * Builds an {@link Argument} from the specified field annotated with {@link Argument.Define}.
	 * Note that this doesn't set the argument type. Use {@link #setTypeFromField(Field)} for that.
	 *
	 * @param field the field that will be used to build the argument
	 * @param <Type> the {@link ArgumentType} subclass that will parse the value passed to the argument
	 * @param <TInner> the actual type of the value passed to the argument
	 * @return the built argument
	 */
	public static @NotNull <Type extends ArgumentType<TInner>, TInner>
	ArgumentBuilder<Type, TInner> fromField(@NotNull Field field) {
		final var annotation = field.getAnnotation(Argument.Define.class);

		if (annotation == null)
			throw new IllegalArgumentException("The field must have an @Argument.Define annotation.");

		final var argumentBuilder = new ArgumentBuilder<Type, TInner>()
			.names(ArgumentBuilder.getTemplateFieldNames(field));

		argumentBuilder.prefix(annotation.prefix());
		if (!annotation.description().isEmpty()) argumentBuilder.description(annotation.description());
		argumentBuilder.required(annotation.required());
		argumentBuilder.positional(annotation.positional());
		argumentBuilder.unique(annotation.unique());
		argumentBuilder.visible(annotation.visible());

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
			.orElseThrow(() -> new CommandTemplateException(
				"No field named '" + fieldName + "' with the @Argument.Define annotation "
				+ "could be found in the template class '" + templateClass.getSimpleName() + "'."
			))
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
	static @NotNull String @NotNull [] getTemplateFieldNames(@NotNull Field field) {
		final var annotation = field.getAnnotation(Argument.Define.class);
		assert annotation != null : "The field must have an @Argument.Define annotation.";

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
	public ArgumentBuilder<Type, TInner> description(@Nullable String description) {
		this.description = description;
		return this;
	}

	/** @see Argument#setRepresentationColor(Color) */
	public ArgumentBuilder<Type, TInner> representationColor(@Nullable Color color) {
		this.representationColor = color;
		return this;
	}

	/** @see Argument#setRequired(boolean) */
	public ArgumentBuilder<Type, TInner> required(boolean required) {
		this.required = required;
		return this;
	}

	/** @see Argument#setPositional(boolean) */
	public ArgumentBuilder<Type, TInner> positional(boolean positional) {
		this.positional = positional;
		return this;
	}

	/** @see Argument#setUnique(boolean) */
	public ArgumentBuilder<Type, TInner> unique(boolean unique) {
		this.unique = unique;
		return this;
	}

	/** @see Argument#setVisible(boolean) */
	public ArgumentBuilder<Type, TInner> visible(boolean visible) {
		this.visible = visible;
		return this;
	}

	/** @see Argument#setDefaultValue(Object) */
	public ArgumentBuilder<Type, TInner> defaultValue(@Nullable TInner defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}

	/** @see Argument#setOnOkCallback(Consumer) */
	public ArgumentBuilder<Type, TInner> onOk(@Nullable Consumer<TInner> callback) {
		this.onCorrectCallback = callback;
		return this;
	}

	/** @see Argument#setOnErrorCallback(Consumer) */
	public ArgumentBuilder<Type, TInner> onErr(@Nullable Consumer<Argument<Type, TInner>> callback) {
		this.onErrorCallback = callback;
		return this;
	}

	/** @see Argument#setPrefix(Argument.Prefix) */
	public ArgumentBuilder<Type, TInner> prefix(@NotNull Argument.Prefix prefix) {
		this.prefix = prefix;
		return this;
	}

	/** @see Argument#setNames(List)  */
	public ArgumentBuilder<Type, TInner> names(@NotNull String... names) {
		this.names = names;
		return this;
	}

	/**
	 * The Argument Type is the class that will be used to parse the argument value. It handles the conversion from the
	 * input string to the desired type.
	 * @param argType the argument type that will be used to parse the argument value
	 * @see ArgumentType
	 * @see Argument#type
	 */
	public ArgumentBuilder<Type, TInner> type(@NotNull Type argType) {
		this.type = argType;
		return this;
	}

	/**
	 * The Argument Type is the class that will be used to parse the argument value. It handles the conversion from the
	 * input string to the desired type.
	 * @param argType the builder that will be used to build the argument type
	 * @see ArgumentType
	 * @see Argument#type
	 */
	public ArgumentBuilder<Type, TInner> type(@NotNull Builder<Type> argType) {
		this.type = argType.build();
		return this;
	}

	/**
	 * Sets the argument type from the specified field. If the argument type is already set, this method does nothing.
	 * If the argument type cannot be inferred from the field, an exception will be thrown.
	 * <strong>Note: </strong> Expects the field to be annotated with {@link Argument.Define}
	 * @param field the field that will be used to infer the argument type
	 * @see #setTypeFromField(Field)
	 * @throws CommandTemplateException if the argument type cannot be inferred from the field
	 */
	@SuppressWarnings("unchecked")
	void setTypeFromField(@NotNull Field field) {
		// if the argType is already set, don't change it
		if (this.type != null) return;

		var argType = ArgumentBuilder.getArgumentTypeFromField(field);

		if (argType != null) {
			this.type((Type)argType);
			return;
		}

		var fieldType = field.getType();
		var exceptionMsg = "Could not infer the argument type from the field '" + field.getName()
			+ "' with type '" + fieldType.getSimpleName() + "'.";

		// if the field is an Optional
		if (fieldType == Optional.class)
			throw new CommandTemplateException(
				exceptionMsg + " Cannot infer the argument type from an Optional wrapper. Please specify the type "
				+ "explicitly as well."
			);

		// if the field is a primitive array
		if (fieldType.isArray() && fieldType.getComponentType().isPrimitive())
			throw new CommandTemplateException(
				exceptionMsg + " Primitive arrays are not supported. "
				+ "Use their wrapper class instead. (e.g. int[] -> Integer[])"
			);

		throw new CommandTemplateException(exceptionMsg);
	}

	/**
	 * Builds the argument.
	 * @return the built argument
	 * @throws IllegalStateException if the argument doesn't have at least one name or if the type is not defined
	 */
	@Override
	public @NotNull Argument<Type, TInner> build() {
		if (this.names == null || this.names.length == 0)
			throw new IllegalStateException("The argument must have at least one name.");

		if (this.type == null)
			throw new IllegalStateException("The argument must have a type defined.");

		var newArg = new Argument<>(this.type, this.names);
		newArg.setDescription(this.description);
		newArg.setRequired(this.required);
		newArg.setPositional(this.positional);
		newArg.setUnique(this.unique);
		newArg.setVisible(this.visible);
		newArg.setDefaultValue(this.defaultValue);
		if (this.prefix != null)
			newArg.setPrefix(this.prefix);
		newArg.setOnErrorCallback(this.onErrorCallback);
		newArg.setOnOkCallback(this.onCorrectCallback);
		if (this.representationColor != null)
			newArg.setRepresentationColor(this.representationColor);
		return newArg;
	}
}
package lanat;

import lanat.argumentTypes.*;
import lanat.exceptions.ArgumentTypeInferException;
import org.jetbrains.annotations.NotNull;
import utils.Range;
import utils.exceptions.DisallowedInstantiationException;

import java.io.File;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * <h2>Argument Type Inferring</h2>
 * <p>
 * Handles inferring argument types for specified types. This is used mostly for defining {@link Argument}s in
 * {@link CommandTemplate}s.
 * </p>
 * <h3>Example:</h3>
 * <p>
 * When defining an {@link Argument}, like this:
 * <pre>{@code
 * @Argument.Define
 * public Double[] numbers;
 * }</pre>
 * <p>
 * In this case, {@link ArgumentTypeInfer#get(Class)} is called with the type {@code Double[]}, which will return a
 * {@link TupleArgumentType} instance ready to be used for that value type:
 * <pre>{@code new TupleArgumentType(Range.AT_LEAST_ONE, new DoubleArgumentType())}.</pre>
 */
public final class ArgumentTypeInfer {
	private ArgumentTypeInfer() {
		throw new DisallowedInstantiationException(ArgumentTypeInfer.class);
	}

	/**
	 * Mapping of types to their corresponding argument types. Used for inferring.
	 * Argument types are stored as suppliers so that we have no shared references.
	 * */
	private static final HashMap<Class<?>, Supplier<? extends ArgumentType<?>>> INFER_ARGUMENT_TYPES_MAP = new HashMap<>();

	/** The default range to use for argument types that accept multiple values. */
	public static final Range DEFAULT_TYPE_RANGE = Range.AT_LEAST_ONE;

	/**
	 * Registers an argument type to be inferred for the specified type.
	 * @param type The argument type to infer.
	 * @param clazz The type to infer the argument type for.
	 */
	public static void register(@NotNull Supplier<? extends ArgumentType<?>> type, @NotNull Class<?> clazz) {
		if (clazz.isArray() && clazz.getComponentType().isPrimitive())
			throw new IllegalArgumentException("Cannot register argument type infer for primitive array type: " + clazz.getName());

		if (ArgumentTypeInfer.INFER_ARGUMENT_TYPES_MAP.containsKey(clazz))
			throw new IllegalArgumentException("Argument type already registered for type: " + clazz.getName());

		ArgumentTypeInfer.INFER_ARGUMENT_TYPES_MAP.put(clazz, type);
	}

	/**
	 * Registers an argument type to be inferred for the specified type, including the primitive form.
	 * @param type The argument type to infer.
	 * @param boxed The boxed type to infer the argument type for.
	 * @param primitive The primitive type to infer the argument type for.
	 */
	public static void registerWithPrimitive(
		@NotNull Supplier<? extends ArgumentType<?>> type,
		@NotNull Class<?> boxed,
		@NotNull Class<?> primitive
	) {
		checkBoxedAndPrimitive(boxed, primitive);

		ArgumentTypeInfer.register(type, boxed);
		ArgumentTypeInfer.register(type, primitive);
	}

	/**
	 * Removes the argument type inference for the specified type.
	 * @param clazz The type to unregister the argument type from.
	 * @throws IllegalArgumentException If no argument type is found for the specified type.
	 */
	public static void unregister(@NotNull Class<?> clazz) {
		if (!ArgumentTypeInfer.INFER_ARGUMENT_TYPES_MAP.containsKey(clazz))
			throw new IllegalArgumentException("No argument type registered for type: " + clazz.getName());

		ArgumentTypeInfer.INFER_ARGUMENT_TYPES_MAP.remove(clazz);
	}

	/**
	 * Removes the argument type inference for the specified type, including the primitive form.
	 * @param boxed The boxed type to unregister the argument type from.
	 * @param primitive The primitive type to unregister the argument type from.
	 */
	public static void unregisterWithPrimitive(
		@NotNull Class<?> boxed,
		@NotNull Class<?> primitive
	) {
		checkBoxedAndPrimitive(boxed, primitive);

		ArgumentTypeInfer.unregister(boxed);
		ArgumentTypeInfer.unregister(primitive);
	}

	/**
	 * Returns a new argument type instance for the specified type.
	 * @param clazz The type to infer the argument type for.
	 * @return The argument type that should be inferred for the specified type.
	 * @throws ArgumentTypeInferException If no argument type is found for the specified type.
	 */
	public static @NotNull ArgumentType<?> get(@NotNull Class<?> clazz) {
		return Optional.ofNullable(ArgumentTypeInfer.INFER_ARGUMENT_TYPES_MAP.get(clazz))
			.map(Supplier::get)
			.orElseThrow(() -> new ArgumentTypeInferException(clazz));
	}


	/**
	 * Checks that {@code boxed} is a non-primitive type and {@code primitive} is a primitive type.
	 * @param boxed The boxed type.
	 * @param primitive The primitive type.
	 * @throws IllegalArgumentException If the types are not as expected.
	 */
	private static void checkBoxedAndPrimitive(
		@NotNull Class<?> boxed,
		@NotNull Class<?> primitive
	) {
		if (!(!boxed.isPrimitive() && primitive.isPrimitive()))
			throw new IllegalArgumentException("Boxed type must be non-primitive and primitive type must be a primitive.");
	}


	/**
	 * Registers a numeric argument type with the specified tuple type as well.
	 * Note that for arrays, only the non-primitive types are inferred.
	 *
	 * @param <Ti> The type of the numeric type.
	 * @param <T> The type of the tuple argument type.
	 * @param type The type of the numeric argument type.
	 * @param primitive The <strong>non-array</strong> types to infer the argument type for.
	 */
	private static <Ti extends Number, T extends NumberArgumentType<Ti>>
	void registerNumericWithTuple(
		@NotNull Supplier<T> type,
		@NotNull Class<?> boxed,
		@NotNull Class<?> primitive
	) {
		// register both the primitive and non-primitive types
		ArgumentTypeInfer.registerWithPrimitive(type, boxed, primitive);

		// register the array type (only the non-primitive type)
		ArgumentTypeInfer.register(() -> new TupleArgumentType<>(DEFAULT_TYPE_RANGE, type.get()), boxed.arrayType());
	}

	// add some default argument types.
	static {
		register(StringArgumentType::new, String.class);
		register(() -> new TupleArgumentType<>(DEFAULT_TYPE_RANGE, new StringArgumentType()), String[].class);

		registerWithPrimitive(BooleanArgumentType::new, Boolean.class, boolean.class);

		register(() -> new FileArgumentType(false), File.class);

		registerNumericWithTuple(IntegerArgumentType::new, Integer.class, int.class);
		registerNumericWithTuple(FloatArgumentType::new, Float.class, float.class);
		registerNumericWithTuple(DoubleArgumentType::new, Double.class, double.class);
		registerNumericWithTuple(LongArgumentType::new, Long.class, long.class);
		registerNumericWithTuple(ShortArgumentType::new, Short.class, short.class);
		registerNumericWithTuple(ByteArgumentType::new, Byte.class, byte.class);
	}
}
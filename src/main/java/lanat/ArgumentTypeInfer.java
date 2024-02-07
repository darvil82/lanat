package lanat;

import lanat.argumentTypes.*;
import lanat.exceptions.ArgumentTypeInferException;
import org.jetbrains.annotations.NotNull;
import utils.Range;

import java.io.File;
import java.util.HashMap;
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
 * <pre>{@code new TupleArgumentType(Range.AT_LEAST_ONE, new DoubleArgumentType()}.</pre>
 */
public class ArgumentTypeInfer {
	/**
	 * Mapping of types to their corresponding argument types. Used for inferring.
	 * Argument types are stored as suppliers so that we have no shared references.
	 * */
	private static final HashMap<Class<?>, Supplier<? extends ArgumentType<?>>> INFER_ARGUMENT_TYPES_MAP = new HashMap<>();

	/** The default range to use for argument types that accept multiple values. */
	public static final Range DEFAULT_TYPE_RANGE = Range.AT_LEAST_ONE;

	/**
	 * Registers an argument type to be inferred for the specified type/s.
	 * @param type The argument type to infer.
	 * @param infer The types to infer the argument type for.
	 */
	public static void register(@NotNull Supplier<? extends ArgumentType<?>> type, @NotNull Class<?>... infer) {
		if (infer.length == 0)
			throw new IllegalArgumentException("Must specify at least one type to infer the argument type for.");

		for (Class<?> clazz : infer) {
			if (clazz.isArray() && clazz.getComponentType().isPrimitive())
				throw new IllegalArgumentException("Cannot register argument type infer for primitive array type: " + clazz.getName());

			if (ArgumentTypeInfer.INFER_ARGUMENT_TYPES_MAP.containsKey(clazz))
				throw new IllegalArgumentException("Argument type already registered for type: " + clazz.getName());

			ArgumentTypeInfer.INFER_ARGUMENT_TYPES_MAP.put(clazz, type);
		}
	}

	/**
	 * Returns a new argument type instance for the specified type.
	 * @param clazz The type to infer the argument type for.
	 * @return The argument type that should be inferred for the specified type.
	 * @throws ArgumentTypeInferException If no argument type is found for the specified type.
	 */
	public static @NotNull ArgumentType<?> get(@NotNull Class<?> clazz) {
		var result = ArgumentTypeInfer.INFER_ARGUMENT_TYPES_MAP.get(clazz);

		if (result == null)
			throw new ArgumentTypeInferException(clazz);

		return result.get();
	}

	/**
	 * Registers a numeric argument type with the specified tuple type as well.
	 * Note that for arrays, only the non-primitive types are inferred.
	 * @param type The type of the numeric argument type.
	 * @param inferPrimitive The <strong>non-array</strong> types to infer the argument type for.
	 * @param <Ti> The type of the numeric type.
	 * @param <T> The type of the tuple argument type.
	 */
	private static <Ti extends Number, T extends NumberArgumentType<Ti>>
	void registerNumericWithTuple(
		@NotNull Supplier<T> type,
		@NotNull Class<?> inferPrimitive,
		@NotNull Class<?> infer
	) {
		assert !infer.isPrimitive() && inferPrimitive.isPrimitive()
			: "Infer must be a non-primitive type and inferPrimitive must be a primitive type.";

		// register both the primitive and non-primitive types
		ArgumentTypeInfer.register(type, inferPrimitive, infer);

		// register the array type (only the non-primitive type)
		ArgumentTypeInfer.register(() -> new TupleArgumentType<>(DEFAULT_TYPE_RANGE, type.get()), infer.arrayType());
	}

	// add some default argument types.
	static {
		register(StringArgumentType::new, String.class);
		register(() -> new TupleArgumentType<>(DEFAULT_TYPE_RANGE, new StringArgumentType()), String[].class);

		register(BooleanArgumentType::new, boolean.class, Boolean.class);

		register(() -> new FileArgumentType(false), File.class);

		// we need to specify the primitives as well... wish there was a better way to do this.
		registerNumericWithTuple(IntegerArgumentType::new, int.class, Integer.class);
		registerNumericWithTuple(FloatArgumentType::new, float.class, Float.class);
		registerNumericWithTuple(DoubleArgumentType::new, double.class, Double.class);
		registerNumericWithTuple(LongArgumentType::new, long.class, Long.class);
		registerNumericWithTuple(ShortArgumentType::new, short.class, Short.class);
		registerNumericWithTuple(ByteArgumentType::new, byte.class, Byte.class);
	}
}
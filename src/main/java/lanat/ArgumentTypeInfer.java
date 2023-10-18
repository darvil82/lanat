package lanat;

import lanat.argumentTypes.*;
import lanat.exceptions.ArgumentTypeInferException;
import lanat.utils.Range;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Supplier;

public class ArgumentTypeInfer {
	/**
	 * Mapping of types to their corresponding argument types. Used for inferring.
	 * Argument types are stored as suppliers so that we have no shared references.
	 * */
	private static final HashMap<Class<?>, Supplier<? extends ArgumentType<?>>> INFER_ARGUMENT_TYPES_MAP = new HashMap<>();
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
	public static ArgumentType<?> get(@NotNull Class<?> clazz) {
		var result = ArgumentTypeInfer.INFER_ARGUMENT_TYPES_MAP.get(clazz);

		if (result == null)
			throw new ArgumentTypeInferException(clazz);

		return result.get();
	}

	/**
	 * Registers a numeric argument type with the specified tuple type as well.
	 * @param type The type of the numeric argument type.
	 * @param array The default value of the numeric argument type.
	 * @param infer The <strong>non-array</strong> types to infer the argument type for.
	 * @param <Ti> The type of the numeric type.
	 * @param <T> The type of the tuple argument type.
	 */
	private static <Ti extends Number, T extends NumberArgumentType<Ti>>
	void registerNumericWithTuple(@NotNull Supplier<T> type, Ti[] array, @NotNull Class<?>... infer) {
		ArgumentTypeInfer.register(type, infer);
		ArgumentTypeInfer.register(
			() -> new MultipleNumbersArgumentType<>(DEFAULT_TYPE_RANGE, array),
			Arrays.stream(infer)
				.map(Class::arrayType)
				.toArray(Class[]::new)
		);
	}

	// add some default argument types.
	static {
		register(StringArgumentType::new, String.class);
		register(() -> new MultipleStringsArgumentType(DEFAULT_TYPE_RANGE), String[].class);

		register(BooleanArgumentType::new, boolean.class, Boolean.class);

		register(() -> new FileArgumentType(false), File.class);

		// we need to specify the primitives as well... wish there was a better way to do this.
		registerNumericWithTuple(IntegerArgumentType::new, new Integer[] {}, int.class, Integer.class);
		registerNumericWithTuple(FloatArgumentType::new, new Float[] {}, float.class, Float.class);
		registerNumericWithTuple(DoubleArgumentType::new, new Double[] {}, double.class, Double.class);
		registerNumericWithTuple(LongArgumentType::new, new Long[] {}, long.class, Long.class);
		registerNumericWithTuple(ShortArgumentType::new, new Short[] {}, short.class, Short.class);
		registerNumericWithTuple(ByteArgumentType::new, new Byte[] {}, byte.class, Byte.class);
	}
}

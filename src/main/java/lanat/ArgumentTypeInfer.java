package lanat;

import lanat.argumentTypes.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.function.Supplier;

public class ArgumentTypeInfer {
	/**
	 * Mapping of types to their corresponding argument types. Used for inferring.
	 * Argument types are stored as suppliers so that they can be instantiated when needed.
	 * */
	private static final HashMap<Class<?>, Supplier<? extends ArgumentType<?>>> INFER_ARGUMENT_TYPES_MAP = new HashMap<>();

	/**
	 * Registers an argument type to be inferred for the specified type/s.
	 * @param type The argument type to infer.
	 * @param infer The types to infer the argument type for.
	 */
	public static void register(@NotNull Supplier<? extends ArgumentType<?>> type, @NotNull Class<?>... infer) {
		for (Class<?> clazz : infer) {
			ArgumentTypeInfer.INFER_ARGUMENT_TYPES_MAP.put(clazz, type);
		}
	}

	/**
	 * Returns the argument type that should be inferred for the specified type.
	 * @param clazz The type to infer the argument type for.
	 * @return The argument type that should be inferred for the specified type. Returns {@code null} if no
	 * valid argument type was found.
	 */
	public static ArgumentType<?> get(@NotNull Class<?> clazz) {
		return ArgumentTypeInfer.INFER_ARGUMENT_TYPES_MAP.get(clazz).get();
	}

	// add some default argument types.
	static {
		// we need to also specify the primitives... wish there was a better way to do this.
		register(StringArgumentType::new, String.class);
		register(MultipleStringsArgumentType::new, String[].class);
		register(IntegerArgumentType::new, int.class, Integer.class);
		register(BooleanArgumentType::new, boolean.class, Boolean.class);
		register(FloatArgumentType::new, float.class, Float.class);
		register(DoubleArgumentType::new, double.class, Double.class);
		register(LongArgumentType::new, long.class, Long.class);
		register(ShortArgumentType::new, short.class, Short.class);
		register(ByteArgumentType::new, byte.class, Byte.class);
		register(() -> new FileArgumentType(false), File.class);
	}
}

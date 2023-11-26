package lanat.argumentTypes;

import lanat.ArgumentType;
import lanat.utils.displayFormatter.Color;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.Range;

/**
 * Provides a base for argument types that take multiple values.
 * Shows a properly formatted description and representation.
 * @param <T> the type of the value that the argument will take
 */
public abstract class TupleArgumentType<T> extends ArgumentType<T[]> {
	private final @NotNull Range argCount;
	private final @NotNull ArgumentType<T> argumentType;

	/**
	 * Creates a new {@link TupleArgumentType} with the specified range and argument type.
	 * @param range The range of values that the argument will take.
	 * @param argumentType The argument type that will be used to parse the values.
	 * @param defaultValue The default value of the argument. This will be used if no values are provided.
	 */
	public TupleArgumentType(@NotNull Range range, @NotNull ArgumentType<T> argumentType, @NotNull T[] defaultValue) {
		super(defaultValue);
		this.argCount = range;
		this.registerSubType(this.argumentType = argumentType);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T @Nullable [] parseValues(@NotNull String... args) {
		var result = new Object[args.length];

		for (int i = 0; i < args.length; i++) {
			result[i] = this.argumentType.parseValues(args[i]);
		}

		return (T[])result;
	}

	@Override
	public @NotNull Range getRequiredArgValueCount() {
		return this.argCount;
	}

	@Override
	public @Nullable TextFormatter getRepresentation() {
		var argTypeRepr = this.argumentType.getRepresentation();
		if (argTypeRepr == null)
			return null;

		return argTypeRepr
			.concat(new TextFormatter(this.argCount.getRegexRange()).withForegroundColor(Color.BRIGHT_YELLOW));
	}

	@Override
	public @Nullable String getDescription() {
		return "Takes " + this.argCount.getMessage("value")
			+ " of type " + this.argumentType.getRepresentation() + ".";
	}
}
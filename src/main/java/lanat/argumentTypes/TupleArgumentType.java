package lanat.argumentTypes;

import lanat.ArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import textFormatter.Color;
import textFormatter.TextFormatter;
import utils.Range;

/**
 * Parses multiple values received from the user by using the argument type provided in the constructor.
 * Shows a properly formatted description and representation.
 * @param <T> the type of the value that the argument type will parse into.
 */
public class TupleArgumentType<T> extends ArgumentType<T[]> {
	private final @NotNull Range valueCount;
	private final @NotNull ArgumentType<T> argumentType;

	/**
	 * Creates a new {@link TupleArgumentType} with the specified range and argument type.
	 * @param valueCount The range of values that the argument will take.
	 * @param argumentType The argument type that will be used to parse the values.
	 */
	public TupleArgumentType(@NotNull Range valueCount, @NotNull ArgumentType<T> argumentType) {
		this.valueCount = valueCount;
		this.registerSubType(this.argumentType = argumentType);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T[] parseValues(@NotNull String @NotNull... values) {
		// quick dirty optimization for string argument types. no need to parse them.
		if (this.argumentType instanceof StringArgumentType)
			return (T[])values;

		var result = new Object[values.length];

		for (int i = 0; i < values.length; i++) {
			result[i] = this.argumentType.parseValues(values[i]);
		}

		return (T[])result;
	}

	@Override
	public @NotNull Range getRequiredArgValueCount() {
		return this.valueCount;
	}

	@Override
	public @Nullable TextFormatter getRepresentation() {
		var argTypeRepr = this.argumentType.getRepresentation();
		if (argTypeRepr == null)
			return null;

		return argTypeRepr
			.concat(TextFormatter.of(this.valueCount.getRepresentation()).withForegroundColor(Color.BRIGHT_YELLOW));
	}

	@Override
	public @Nullable String getDescription() {
		return "Takes " + this.valueCount.getMessage("value")
			+ " of type " + this.argumentType.getRepresentation() + ".";
	}
}
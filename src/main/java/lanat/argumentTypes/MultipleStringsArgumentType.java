package lanat.argumentTypes;

import org.jetbrains.annotations.NotNull;
import utils.Range;

/**
 * An argument type that takes multiple strings.
 */
public class MultipleStringsArgumentType extends TupleArgumentType<String> {
	/**
	 * Creates a new {@link TupleArgumentType} with the specified range of values that the argument will take.
	 * @param valueCount The range of values that the argument will take.
	 */
	public MultipleStringsArgumentType(@NotNull Range valueCount) {
		super(valueCount, new StringArgumentType());
	}

	// no need for anything fancy here, simply return the args
	@Override
	public @NotNull String[] parseValues(@NotNull String @NotNull [] args) {
		return args;
	}
}
package lanat.argumentTypes;

import lanat.ArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import textFormatter.TextFormatter;
import utils.Range;

/**
 * An argument type that counts the number of times it is used.
 */
public class CounterArgumentType extends ArgumentType<Integer> {
	public CounterArgumentType() {
		super(0);
	}

	@Override
	public @NotNull Range getRequiredArgValueCount() {
		return Range.NONE;
	}

	@Override
	public @NotNull Range getRequiredUsageCount() {
		return Range.AT_LEAST_ONE;
	}

	@Override
	public TextFormatter getRepresentation() {
		return null;
	}

	@Override
	public Integer parseValues(String @NotNull [] values) {
		return this.getValue() + 1;
	}

	@Override
	public @Nullable String getDescription() {
		return "Counts the number of times this argument is used.";
	}
}
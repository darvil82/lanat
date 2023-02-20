package lanat.argumentTypes;

import lanat.UsageCountRange;
import lanat.utils.Range;
import lanat.ArgumentType;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;

public class CounterArgument extends ArgumentType<Integer> {
	public CounterArgument() {
		super(0);
	}

	@Override
	public @NotNull Range getRequiredArgValueCount() {
		return Range.NONE;
	}

	@Override
	public @NotNull UsageCountRange getRequiredUsageCount() {
		return UsageCountRange.AT_LEAST_ONE;
	}

	@Override
	public TextFormatter getRepresentation() {
		return null;
	}

	@Override
	public Integer parseValues(String @NotNull [] args) {
		return this.getValue() + 1;
	}
}

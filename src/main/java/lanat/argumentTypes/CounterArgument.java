package lanat.argumentTypes;

import lanat.ArgValueCount;
import lanat.ArgumentType;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;

public class CounterArgument extends ArgumentType<Integer> {
	public CounterArgument() {
		super(0);
	}

	@Override
	public @NotNull ArgValueCount getArgValueCount() {
		return ArgValueCount.NONE;
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

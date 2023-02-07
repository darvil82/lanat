package lanat.argumentTypes;

import lanat.ArgValueCount;
import lanat.ArgumentType;
import lanat.utils.displayFormatter.TextFormatter;

public class CounterArgument extends ArgumentType<Integer> {
	public CounterArgument() {
		super(0);
	}

	@Override
	public ArgValueCount getNumberOfArgValues() {
		return ArgValueCount.NONE;
	}

	@Override
	public TextFormatter getRepresentation() {
		return null;
	}

	@Override
	public Integer parseValues(String[] args) {
		return this.getValue() + 1;
	}
}

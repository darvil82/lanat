package argparser.argumentTypes;

import argparser.ArgValueCount;
import argparser.ArgumentType;
import argparser.utils.displayFormatter.TextFormatter;

public class CounterArgument extends ArgumentType<Integer> {
	// prevent nullptr exceptions
	public CounterArgument() {
		this.setValue(0);
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

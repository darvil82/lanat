package argparser.argumentTypes;

import argparser.ArgValueCount;
import argparser.ArgumentType;

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
	public void parseValues(String[] args) {
		this.setValue(this.getValue() + 1);
	}
}

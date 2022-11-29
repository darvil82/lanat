package argparser.argumentTypes;

import argparser.ArgValueCount;
import argparser.ArgumentType;

public class CounterArgument extends ArgumentType<Short> {
	// prevent nullptr exceptions
	public CounterArgument() {
		this.setValue((short)0);
	}

	@Override
	public ArgValueCount getNumberOfArgValues() {
		return ArgValueCount.NONE;
	}

	@Override
	public void parseValues(String[] args) {
		this.setValue((short)(this.getValue() + 1));
	}
}

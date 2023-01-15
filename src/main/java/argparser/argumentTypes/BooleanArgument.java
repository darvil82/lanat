package argparser.argumentTypes;

import argparser.ArgValueCount;
import argparser.ArgumentType;
import argparser.utils.displayFormatter.TextFormatter;

public class BooleanArgument extends ArgumentType<Boolean> {

	@Override
	public Boolean parseValues(String[] args) {
		return true;
	}

	@Override
	public TextFormatter getRepresentation() {
		return null;
	}

	@Override
	// this is a boolean type. if the arg is present, that's enough.
	public ArgValueCount getNumberOfArgValues() {
		return ArgValueCount.NONE;
	}
}

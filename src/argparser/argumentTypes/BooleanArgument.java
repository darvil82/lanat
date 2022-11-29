package argparser.argumentTypes;

import argparser.ArgValueCount;
import argparser.ArgumentType;

public class BooleanArgument extends ArgumentType<Boolean> {
	@Override
	public void parseValues(String[] arg) {
		this.setValue(true);
	}

	@Override
	public String getRepresentation() {
		return "bool";
	}

	@Override
	// this is a boolean type. if the arg is present, that's enough.
	public ArgValueCount getNumberOfArgValues() {
		return ArgValueCount.NONE;
	}
}

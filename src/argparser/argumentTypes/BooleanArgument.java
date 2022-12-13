package argparser.argumentTypes;

import argparser.ArgValueCount;
import argparser.ArgumentType;
import argparser.Token;

public class BooleanArgument extends ArgumentType<Boolean> {
	@Override
	public void parseValues(String[] arg) {
		this.setValue(true);
	}

	@Override
	public Token[] getRepresentation() {
		return new Token[0];
	}

	@Override
	// this is a boolean type. if the arg is present, that's enough.
	public ArgValueCount getNumberOfArgValues() {
		return ArgValueCount.NONE;
	}
}

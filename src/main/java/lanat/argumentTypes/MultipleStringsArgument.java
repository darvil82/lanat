package lanat.argumentTypes;

import lanat.ArgValueCount;
import lanat.ArgumentType;

public class MultipleStringsArgument extends ArgumentType<String[]> {
	@Override
	public ArgValueCount getNumberOfArgValues() {
		return ArgValueCount.AT_LEAST_ONE;
	}

	@Override
	public String[] parseValues(String[] args) {
		return args;
	}
}

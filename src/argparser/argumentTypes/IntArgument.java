package argparser.argumentTypes;

import argparser.ArgumentType;

public class IntArgument extends ArgumentType<Integer> {
	@Override
	public void parseValues(String[] arg) {
		try {
			this.value = Integer.parseInt(arg[0]);
		} catch (NumberFormatException e) {
			this.addError("Invalid integer value: '" + arg[0] + "'.");
		}
	}

	@Override
	public String getRepresentation() {
		return "int";
	}
}

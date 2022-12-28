package argparser.argumentTypes;

import argparser.ArgumentType;

public class MultipleStringsArgument extends ArgumentType<String[]> {
	@Override
	public void parseValues(String[] args) {
		this.setValue(args);
	}
}

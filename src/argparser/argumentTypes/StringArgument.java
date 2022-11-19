package argparser.argumentTypes;

import argparser.ArgumentType;

public class StringArgument extends ArgumentType<String> {
	@Override
	public void parseValues(String[] args) {
		this.value = args[0];
	}
}

package argparser.argumentTypes;

import argparser.ArgumentType;

public class StringArgument extends ArgumentType<String> {
	@Override
	public String parseValues(String[] args) {
		return args[0];
	}
}

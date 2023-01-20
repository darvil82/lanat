package argparser.argumentTypes;

import argparser.ArgumentType;
import argparser.utils.displayFormatter.FormatOption;
import argparser.utils.displayFormatter.TextFormatter;

public class StringArgument extends ArgumentType<String> {
	@Override
	public String parseValues(String[] args) {
		return args[0];
	}

	@Override
	public TextFormatter getRepresentation() {
		return new TextFormatter("string");
	}
}

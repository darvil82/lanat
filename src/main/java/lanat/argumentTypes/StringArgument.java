package lanat.argumentTypes;

import lanat.ArgumentType;
import lanat.utils.displayFormatter.TextFormatter;

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

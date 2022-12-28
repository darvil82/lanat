package argparser.argumentTypes;

import argparser.ArgumentType;
import argparser.ErrorLevel;
import argparser.utils.displayFormatter.TextFormatter;

public class IntArgument extends ArgumentType<Integer> {
	@Override
	public void parseValues(String[] arg) {
		try {
			this.setValue(Integer.parseInt(arg[0]));
		} catch (NumberFormatException e) {
			this.addError("Invalid integer value: '" + arg[0] + "'.", ErrorLevel.DEBUG);
		}
	}

	@Override
	public TextFormatter getRepresentation() {
		return new TextFormatter("int");
	}
}
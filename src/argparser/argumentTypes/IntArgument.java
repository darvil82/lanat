package argparser.argumentTypes;

import argparser.ArgumentType;
import argparser.Token;
import argparser.TokenType;
import argparser.displayFormatter.TextFormatter;

public class IntArgument extends ArgumentType<Integer> {
	@Override
	public void parseValues(String[] arg) {
		try {
			this.setValue(Integer.parseInt(arg[0]));
		} catch (NumberFormatException e) {
			this.addError("Invalid integer value: '" + arg[0] + "'.");
		}
	}

	@Override
	public TextFormatter getRepresentation() {
		return new TextFormatter("int");
	}
}

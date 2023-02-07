package lanat.argumentTypes;

import lanat.ArgumentType;
import lanat.utils.displayFormatter.TextFormatter;

public class FloatArgument extends ArgumentType<Float> {
	@Override
	public Float parseValues(String[] args) {
		try {
			return Float.parseFloat(args[0]);
		} catch (NumberFormatException e) {
			this.addError("Invalid float value: '" + args[0] + "'.");
			return null;
		}
	}

	@Override
	public TextFormatter getRepresentation() {
		return new TextFormatter("float");
	}
}
package argparser.argumentTypes;

import argparser.ArgumentType;
import argparser.utils.displayFormatter.Color;
import argparser.utils.displayFormatter.FormatOption;
import argparser.utils.displayFormatter.TextFormatter;

public class EnumArgument<T extends Enum<T>> extends ArgumentType<T> {
	private final T[] values;
	private final T defaultValue;

	public EnumArgument(T defaultValue) {
		this.setValue(this.defaultValue = defaultValue);
		this.values = defaultValue.getDeclaringClass().getEnumConstants();
	}

	@Override
	public T parseValues(String[] args) {
		for (var enumValue : this.values) {
			if (enumValue.name().equalsIgnoreCase(args[0])) {
				return enumValue;
			}
		}
		this.addError("Invalid enum value: '" + args[0] + "'.");
		return null;
	}

	@Override
	public TextFormatter getRepresentation() {
		final var fmt = new TextFormatter("(");
		for (var i = 0; i < this.values.length; i++) {
			final var value = this.values[i];

			if (value == this.defaultValue)
				fmt.concat(new TextFormatter(value.name())
					.setColor(Color.YELLOW)
					.addFormat(FormatOption.BOLD)
				);
			else
				fmt.concat(value.name());


			if (i < this.values.length - 1)
				fmt.concat(" | ");
		}
		return fmt.concat(")");
	}
}

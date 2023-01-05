package argparser.argumentTypes;

import argparser.utils.displayFormatter.Color;
import argparser.utils.displayFormatter.TextFormatter;

public class IntRangeArgument extends IntArgument {
	private final int min, max;

	public IntRangeArgument(int min, int max) {
		if (min > max) {
			throw new IllegalArgumentException("min must be less than or equal to max");
		}

		this.min = min;
		this.max = max;
	}

	@Override
	public void parseValues(String[] args) {
		super.parseValues(args);

		final var value = this.getValue();

		if (value < this.min || value > this.max) {
			this.addError("Value must be between " + this.min + " and " + this.max + ".");
		}
	}

	@Override
	public TextFormatter getRepresentation() {
		return super.getRepresentation()
			.concat(new TextFormatter(String.format("[%d-%d]", this.min, this.max)).setColor(Color.YELLOW));
	}
}

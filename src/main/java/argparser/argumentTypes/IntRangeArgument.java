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
	public Integer parseValues(String[] args) {
		var result = super.parseValues(args);

		if (result == null) return null;

		if (result < this.min || result > this.max) {
			this.addError("Value must be between " + this.min + " and " + this.max + ".");
			return null;
		}

		return result;
	}

	@Override
	public TextFormatter getRepresentation() {
		return super.getRepresentation()
			.concat(new TextFormatter("[%d-%d]".formatted(this.min, this.max)).setColor(Color.YELLOW));
	}
}

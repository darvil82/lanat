package lanat.argumentTypes;

import lanat.utils.displayFormatter.Color;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntRangeArgumentType extends IntArgumentType {
	private final int min, max;

	public IntRangeArgumentType(int min, int max) {
		if (min > max) {
			throw new IllegalArgumentException("min must be less than or equal to max");
		}

		this.min = min;
		this.max = max;
	}

	@Override
	public Integer parseValues(String @NotNull [] args) {
		var result = super.parseValues(args);

		if (result == null) return null;

		if (result < this.min || result > this.max) {
			this.addError("Value must be between " + this.min + " and " + this.max + ".");
			return null;
		}

		return result;
	}

	@Override
	public @NotNull TextFormatter getRepresentation() {
		return super.getRepresentation()
			.concat(new TextFormatter("[%d-%d]".formatted(this.min, this.max)).setColor(Color.YELLOW));
	}

	@Override
	public @Nullable String getDescription() {
		return super.getDescription() + " Must be between " + this.min + " and " + this.max + ". (Inclusive)";
	}
}

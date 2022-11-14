package argparser;


/**
 * Used to specify the number values an argument may accept. This is essentially a classic range,
 * but if only one value is specified, both min and max will have that value.
 */
public class ArgValueCount {
	public static final ArgValueCount ANY = new ArgValueCount(0, -1);
	public static final ArgValueCount NONE = new ArgValueCount(0);
	public static final ArgValueCount ONE = new ArgValueCount(1);

	public final short min, max;

	public ArgValueCount(int min, int max) {
		if (min < -1 || max < -1)
			throw new IllegalArgumentException("min and max values can only be positive, or -1 for any");
		if ((min != -1 && max != -1) && (min > max))
			throw new IllegalArgumentException("min value cannot be higher than max");
		this.min = (short)(min == -1 ? Short.MAX_VALUE : min);
		this.max = (short)(max == -1 ? Short.MAX_VALUE : max);
	}

	public ArgValueCount(int value) {
		this(value, value);
	}

	public boolean isRange() {
		return this.min != this.max;
	}

	public boolean isZero() {
		return this.max == 0;
	}

	public String getMessage() {
		return this.isRange()
			? String.format("from %d to %s values", this.min, this.max == Short.MAX_VALUE ? "any number of" : this.max)
			: String.format("%s value%s", this.min, this.min == 1 ? "" : "s");
	}

	public boolean isInRange(int value, boolean checkIndex) {
		return checkIndex
			? value >= 0 && value < this.max
			: value > this.min && value <= this.max;
	}
}
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
		if ((min >= 0 && max >= 0) && (min > max))
			throw new IllegalArgumentException("min value cannot be higher than max");
		this.min = (short)(min < 0 ? Short.MAX_VALUE : min);
		this.max = (short)(max < 0 ? Short.MAX_VALUE : max);
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
		if (this.isRange()) {
			return String.format("from %d to %s values", this.min, this.max == Short.MAX_VALUE ? "any number of" : this.max);
		} else {
			return String.format("%s value%s", this.min, this.min == 1 ? "" : "s");
		}
	}
}
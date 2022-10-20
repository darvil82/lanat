package argparser;


/**
 * Used to specify the number values an argument may accept. This is essentially a classic range,
 * but if only one value is specified, both min and max will have that value.
 */
public class ArgValueCount {
	public static final ArgValueCount ANY = new ArgValueCount(0, Short.MAX_VALUE);
	public static final ArgValueCount NONE = new ArgValueCount(0);
	public static final ArgValueCount ONE = new ArgValueCount(1);

	public final short min, max;

	public ArgValueCount(int min, int max) {
		if (min > max) throw new IllegalArgumentException("min value cannot be higher than max");
		if (min < 0) throw new IllegalArgumentException("value cannot be negative");
		this.min = (short)min;
		this.max = (short)max;
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
}
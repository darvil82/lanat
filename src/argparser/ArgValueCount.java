package argparser;


/**
 * Used to specify the number values an argument may accept. This is essentially a classic range,
 * but if only one value is specified, both min and max will have that value.
 */
public class ArgValueCount {
	public final byte min, max;

	public ArgValueCount(int min, int max) {
		if (min > max) throw new IllegalArgumentException("min value cannot be higher than max");
		this.min = (byte)min;
		this.max = (byte)max;
	}

	public ArgValueCount(int value) {
		this(value, value);
	}
}
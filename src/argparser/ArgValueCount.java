package argparser;

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
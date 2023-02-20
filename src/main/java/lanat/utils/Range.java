package lanat.utils;


import org.jetbrains.annotations.NotNull;

/**
 * Used to specify the number values an argument may accept. This is essentially a classic range, but if only one value
 * is specified, both min and max will have that value.
 */
public class Range {
	public static final Range ANY = new Range(0, -1);
	public static final Range AT_LEAST_ONE = new Range(1, -1);
	public static final Range NONE = new Range(0);
	public static final Range ONE = new Range(1);

	public final short min, max;

	public Range(int min, int max) {
		if (min < -1 || max < -1)
			throw new IllegalArgumentException("min and max values can only be positive, or -1 for any");
		if ((min != -1 && max != -1) && (min > max))
			throw new IllegalArgumentException("min value cannot be higher than max");
		if (min == -1 && max == -1)
			throw new IllegalArgumentException("min and max cannot both be -1");
		this.min = (short)(min == -1 ? Short.MAX_VALUE : min);
		this.max = (short)(max == -1 ? Short.MAX_VALUE : max);
	}

	public Range(int value) {
		this(value, value);
	}

	public boolean isRange() {
		return this.min != this.max;
	}

	public boolean isZero() {
		return this.max == 0;
	}

	/**
	 * Returns a string representation of the range, such as "from 3 to 5 times", or "3 times".
	 * @param kind The kind of thing the range is for, such as "time" or "argument"
	 * @return The string representation
	 */
	public @NotNull String getMessage(String kind) {
		return this.isRange()
			? "from %d to %s %s".formatted(this.min, this.max == Short.MAX_VALUE ? "any number of" : this.max, kind + 's')
			: this.min + " " + UtlString.plural(kind, this.min);
	}

	/**
	 * Returns a string representation of the range, such as <code>"{3, 5}"</code> or <code>"{3}"</code>.
	 * If the max value is {@link Short#MAX_VALUE}, it will be represented as <code>"..."</code>.
	 * @return The string representation
	 */
	public @NotNull String getRegexRange() {
		return this.isRange()
			? "{%d, %s}".formatted(this.min, "" + (this.max == Short.MAX_VALUE ? "..." : this.max))
			: "{%d}".formatted(this.min);
	}

	public boolean isInRange(int value) {
		return value >= this.min && value <= this.max;
	}

	public boolean isIndexInRange(int value) {
		return value >= 0 && value < this.max;
	}
}
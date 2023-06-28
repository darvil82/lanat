package lanat.utils;


import org.jetbrains.annotations.NotNull;

/** A range class to contain a minimum and maximum value, or a single value. */
public class Range {
	public static final Range ANY = Range.from(0).toInfinity();
	public static final Range AT_LEAST_ONE = Range.from(1).toInfinity();
	public static final Range NONE = Range.of(0);
	public static final Range ONE = Range.of(1);

	private final int min, max;
	public boolean isInfinite;

	private Range(int min, int max) {
		this.isInfinite = max == -1;
		if (min < 0)
			throw new IllegalArgumentException("min value cannot be negative");
		if (!this.isInfinite && min > max)
			throw new IllegalArgumentException("min value cannot be higher than max");

		this.min = min;
		this.max = max;
	}

	public static class RangeBuilder {
		private final int min;
		private int max = 0;

		private RangeBuilder(int min) {
			this.min = min;
		}

		/** Sets the maximum value. */
		public @NotNull Range to(int max) {
			this.max = max;
			return this.build();
		}

		/** Sets the maximum value to infinity. */
		public @NotNull Range toInfinity() {
			this.max = -1;
			return this.build();
		}

		/** Builds the range. */
		private @NotNull Range build() {
			return new Range(this.min, this.max);
		}
	}

	/** Creates a new range builder with the given minimum value. */
	public static @NotNull RangeBuilder from(int min) {
		return new RangeBuilder(min);
	}

	/** Creates a new single-value range with the given value. */
	public static @NotNull Range of(int value) {
		return new Range(value, value);
	}

	public boolean isRange() {
		return this.min != this.max;
	}

	/** Returns {@code true} if the range is 0. */
	public boolean isZero() {
		return this.max == 0;
	}

	/** Returns the min value. */
	public int min() {
		return this.min;
	}

	/** Returns the max value, or {@link Integer#MAX_VALUE} if the range is infinite. */
	public int max() {
		return this.isInfinite ? Integer.MAX_VALUE : this.max;
	}

	/**
	 * Returns a string representation of the range, such as "from 3 to 5 times", or "3 times".
	 *
	 * @param kind The kind of thing the range is for, such as "time" or "argument"
	 * @return The string representation
	 */
	public @NotNull String getMessage(String kind) {
		return this.isRange()
			? "from %d to %s %s".formatted(this.min, this.isInfinite ? "any number of" : this.max, kind + 's')
			: UtlString.plural(kind, this.min);
	}

	/**
	 * Returns a string representation of the range, such as <code>"{3, 5}"</code> or <code>"{3}"</code>. If the max
	 * value is {@link Short#MAX_VALUE}, it will be represented as <code>"..."</code>.
	 *
	 * @return The string representation
	 */
	public @NotNull String getRegexRange() {
		return this.isRange()
			? "{%d, %s}".formatted(this.min, "" + (this.max == -1 ? "..." : this.max))
			: "{%d}".formatted(this.min);
	}

	public boolean isInRange(int value) {
		return value >= this.min && (this.isInfinite || value <= this.max);
	}

	public boolean isIndexInRange(int value) {
		return value >= 0 && (this.isInfinite || value < this.max);
	}
}
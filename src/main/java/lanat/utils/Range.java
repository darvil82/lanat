package lanat.utils;


import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

/** A range class to contain a start and end value, or a single value. */
public class Range implements Iterable<Integer> {
	/** A range between 0 and infinity. */
	public static final Range ANY = Range.from(0).toInfinity();
	/** A range between 1 and infinity. */
	public static final Range AT_LEAST_ONE = Range.from(1).toInfinity();
	/** A range of 0. */
	public static final Range NONE = Range.of(0);
	/** A range of 1. */
	public static final Range ONE = Range.of(1);

	private final int start, end;
	/** Whether the range is infinite. */
	public final boolean isInfinite;

	/**
	 * Creates a new range with the given start and end values. If the end value is -1, the range end value
	 * will be infinity.
	 * @param start The start value
	 * @param end The end value, or -1 for infinity
	 */
	private Range(int start, int end) {
		this.isInfinite = end == -1;
		if (start < 0)
			throw new IllegalArgumentException("start value cannot be negative");
		if (!this.isInfinite && start > end)
			throw new IllegalArgumentException("start value cannot be higher than end value");

		this.start = start;
		this.end = end;
	}

	public static class RangeBuilder {
		private final int start;
		private int end;

		private RangeBuilder(int start) {
			this.start = start;
		}

		/** Sets the end value. */
		public @NotNull Range to(int end) {
			if (end < 0)
				throw new IllegalArgumentException("end value cannot be negative");

			this.end = end;
			return this.build();
		}

		/** Sets the end value to infinity. */
		public @NotNull Range toInfinity() {
			this.end = -1;
			return this.build();
		}

		/** Builds the range. */
		private @NotNull Range build() {
			return new Range(this.start, this.end);
		}
	}

	/** Creates a new range builder with the given start value. */
	public static @NotNull RangeBuilder from(int start) {
		return new RangeBuilder(start);
	}

	/** Creates a new single-value range with the given value. */
	public static @NotNull Range of(int value) {
		return new Range(value, value);
	}

	/** Returns {@code true} if this is representing a range, and not a single value. */
	public boolean isRange() {
		return this.start != this.end;
	}

	/** Returns {@code true} if the range is 0. */
	public boolean isZero() {
		return this.end == 0;
	}

	/** Returns the start value. */
	public int start() {
		return this.start;
	}

	/** Returns the end value, or {@link Integer#MAX_VALUE} if the range is infinite. */
	public int end() {
		return this.isInfinite ? Integer.MAX_VALUE : this.end;
	}

	/**
	 * Returns a string representation of the range, such as "from 3 to 5 times", or "3 times".
	 *
	 * @param kind The kind of thing the range is for, such as "time" or "argument"
	 * @return The string representation
	 */
	public @NotNull String getMessage(@NotNull String kind) {
		return this.isRange()
			? "from %d to %s %s".formatted(this.start, this.isInfinite ? "any number of" : this.end, kind + 's')
			: UtlString.plural(kind, this.start);
	}

	/**
	 * Returns a string representation of the range, such as <code>"{3, 5}"</code> or <code>"{3}"</code>. If the end
	 * value is {@link Short#MAX_VALUE}, it will be represented as <code>"..."</code>.
	 *
	 * @return The string representation
	 */
	public @NotNull String getRegexRange() {
		return this.isRange()
			? "{%d, %s}".formatted(this.start, "" + (this.isInfinite ? "..." : this.end))
			: "{%d}".formatted(this.start);
	}

	/**
	 * Returns {@code true} if the given value is in the range.
	 * @param value The value to check
	 * @param startInclusive Whether the start value is inclusive
	 * @param endInclusive Whether the end value is inclusive
	 * @return {@code true} if the value is in the range
	 */
	public boolean contains(int value, boolean startInclusive, boolean endInclusive) {
		if (!this.isRange()) {
			return value == this.start;
		}

		boolean isInStart = startInclusive
			? value >= this.start
			: value > this.start;

		// if the end value is infinity, it will always be below the end value
		boolean isInEnd = this.isInfinite || (endInclusive
			? value <= this.end
			: value < this.end
		);

		return isInStart && isInEnd;
	}

	/**
	 * Returns {@code true} if the given value is in the range, inclusive.
	 * @param value The value to check
	 * @return {@code true} if the value is in the range
	 */
	public boolean containsInclusive(int value) {
		return this.contains(value, true, true);
	}

	/**
	 * Returns {@code true} if the given value is in the range, exclusive.
	 * @param value The value to check
	 * @return {@code true} if the value is in the range
	 */
	public boolean containsExclusive(int value) {
		return this.contains(value, false, false);
	}

	public @NotNull Range offset(int offset) {
		if (offset == 0)
			return this;

		return new Range(this.start + offset, this.isInfinite ? -1 : this.end + offset);
	}

	@Override
	public @NotNull Iterator<Integer> iterator() {
		return this.iterator(true, true);
	}

	public @NotNull Iterator<Integer> iterator(boolean startInclusive, boolean endInclusive) {
		return new Iterator<>() {
			private int index = Range.this.start + (startInclusive ? 0 : 1);

			@Override
			public boolean hasNext() {
				return this.index < Range.this.end + (endInclusive ? 1 : 0);
			}

			@Override
			public Integer next() {
				return this.index++;
			}
		};
	}

	@Override
	public String toString() {
		return this.getRegexRange();
	}
}
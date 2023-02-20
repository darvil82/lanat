package lanat;

import lanat.utils.Range;

public class UsageCountRange extends Range {
	public static final UsageCountRange AT_LEAST_ONE = new UsageCountRange(1, -1);
	public static final UsageCountRange ONE = new UsageCountRange(1);

	public UsageCountRange(int min, int max) {
		super(min, max);
		if (min == 0) throw new IllegalArgumentException("min cannot be 0");
	}

	public UsageCountRange(int value) {
		this(value, value);
	}
}

package lanat.argumentTypes;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OptListArgumentType extends SingleValueListArgumentType<String> {
	public OptListArgumentType(@NotNull List<String> values, @NotNull String initialValue) {
		super(values.toArray(String[]::new), initialValue);

		if (!values.contains(initialValue))
			throw new IllegalArgumentException("Initial value must be one of the values.");
	}

	public OptListArgumentType(@NotNull List<String> values) {
		super(values.toArray(String[]::new));
	}

	public OptListArgumentType(@NotNull String... values) {
		super(values);
	}

	@Override
	protected boolean predicate(@NotNull String value, @NotNull String strValue) {
		return value.equalsIgnoreCase(strValue);
	}
}
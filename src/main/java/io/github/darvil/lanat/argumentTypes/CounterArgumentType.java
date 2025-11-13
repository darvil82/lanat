package io.github.darvil.lanat.argumentTypes;

import io.github.darvil.lanat.ArgumentType;
import io.github.darvil.terminal.textformatter.TextFormatter;
import io.github.darvil.utils.Range;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An argument type that counts the number of times it is used.
 */
public class CounterArgumentType extends ArgumentType<Integer> {
	public CounterArgumentType() {
		super(0);
	}

	@Override
	public @NotNull Range getValueCountBounds() {
		return Range.NONE;
	}

	@Override
	public @NotNull Range getUsageCountBounds() {
		return Range.AT_LEAST_ONE;
	}

	@Override
	public TextFormatter getRepresentation() {
		return null;
	}

	@Override
	public Integer parseValues(String @NotNull [] values) {
		return this.getValue() + 1;
	}

	@Override
	public @Nullable String getDescription() {
		return "Counts the number of times this argument is used.";
	}
}
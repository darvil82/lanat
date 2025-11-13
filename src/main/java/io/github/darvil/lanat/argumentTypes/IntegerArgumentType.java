package io.github.darvil.lanat.argumentTypes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * An argument type that takes an integer number.
 * @see Integer
 */
public class IntegerArgumentType extends NumberArgumentType<Integer> {
	@Override
	protected @NotNull Function<String, Integer> getParseFunction() {
		return Integer::parseInt;
	}

	@Override
	public @Nullable String getDescription() {
		return "An integer number.";
	}
}
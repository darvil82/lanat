package io.github.darvil.lanat.argumentTypes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * An argument type that takes a floating point number.
 * @see Float
 */
public class FloatArgumentType extends NumberArgumentType<Float> {
	@Override
	protected @NotNull Function<@NotNull String, @NotNull Float> getParseFunction() {
		return Float::parseFloat;
	}

	@Override
	public @Nullable String getDescription() {
		return "A floating point number.";
	}
}
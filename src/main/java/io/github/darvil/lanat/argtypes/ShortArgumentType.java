package io.github.darvil.lanat.argtypes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * An argument type that takes a short integer number.
 * @see Short
 */
public class ShortArgumentType extends NumberArgumentType<Short> {
	@Override
	protected @NotNull Function<@NotNull String, @NotNull Short> getParseFunction() {
		return Short::parseShort;
	}

	@Override
	public @Nullable String getDescription() {
		return "An integer number (-32,768 to 32,767)";
	}
}
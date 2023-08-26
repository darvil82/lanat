package lanat.argumentTypes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * An argument type that takes a double precision floating point number.
 */
public class DoubleArgumentType extends NumberArgumentType<Double> {
	@Override
	protected @NotNull Function<@NotNull String, @NotNull Double> getParseFunction() {
		return Double::parseDouble;
	}

	@Override
	public @Nullable String getDescription() {
		return "A high precision floating point number.";
	}
}
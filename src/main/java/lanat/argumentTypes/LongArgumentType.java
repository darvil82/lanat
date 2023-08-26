package lanat.argumentTypes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * An argument type that takes a long integer number.
 */
public class LongArgumentType extends NumberArgumentType<Long> {
	@Override
	protected @NotNull Function<@NotNull String, @NotNull Long> getParseFunction() {
		return Long::parseLong;
	}

	@Override
	public @Nullable String getDescription() {
		return "A large integer number.";
	}
}
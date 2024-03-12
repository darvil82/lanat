package lanat.argumentTypes;

import lanat.ArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This is a dummy argument type that does not parse any values. It cannot be instantiated, and it's only purpose is to
 * be used as a default value for the {@link lanat.Argument.Define} annotation.
 */
public final class DummyArgumentType extends ArgumentType<Void> {
	private DummyArgumentType() {
		throw new AssertionError("This class should not be instantiated");
	}

	@Override
	public @Nullable Void parseValues(@NotNull String @NotNull [] values) {
		return null;
	}
}
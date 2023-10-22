package lanat.argumentTypes;

import lanat.ArgumentType;
import lanat.utils.Range;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An argument type that is set in a true state if the argument was used.
 * @see Boolean
 */
public class BooleanArgumentType extends ArgumentType<Boolean> {
	public BooleanArgumentType() {
		super(false);
	}

	@Override
	public Boolean parseValues(String @NotNull [] args) {
		return true;
	}

	@Override
	public @Nullable TextFormatter getRepresentation() {
		return null;
	}

	@Override
	// this is a boolean type. if the arg is present, that's enough.
	public @NotNull Range getRequiredArgValueCount() {
		return Range.NONE;
	}
}

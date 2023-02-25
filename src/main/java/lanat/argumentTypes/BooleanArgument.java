package lanat.argumentTypes;

import lanat.ArgumentType;
import lanat.utils.Range;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BooleanArgument extends ArgumentType<Boolean> {
	public BooleanArgument() {
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

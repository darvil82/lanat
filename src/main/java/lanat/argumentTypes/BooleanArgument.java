package lanat.argumentTypes;

import lanat.ArgValueCount;
import lanat.ArgumentType;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BooleanArgument extends ArgumentType<Boolean> {

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
	public @NotNull ArgValueCount getArgValueCount() {
		return ArgValueCount.NONE;
	}
}

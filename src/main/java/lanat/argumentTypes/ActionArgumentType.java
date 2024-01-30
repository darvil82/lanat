package lanat.argumentTypes;

import lanat.ArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import textFormatter.TextFormatter;
import utils.Range;

/**
 * An argument type that is set in a true state if the argument was used.
 * @see Boolean
 */
public class ActionArgumentType extends ArgumentType<Boolean> {
	public ActionArgumentType() {
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
	// this is an action type. if the arg is present, that's enough.
	public @NotNull Range getRequiredArgValueCount() {
		return Range.NONE;
	}
}
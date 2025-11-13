package io.github.darvil.lanat.argumentTypes;

import io.github.darvil.lanat.ArgumentType;
import io.github.darvil.terminal.textformatter.TextFormatter;
import io.github.darvil.utils.Range;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An argument type that is set in a true state if the argument was used.
 * @see Boolean
 */
public class ActionArgumentType extends ArgumentType<Boolean> {
	public ActionArgumentType() {
		super(false);
	}

	@Override
	public Boolean parseValues(String @NotNull [] values) {
		return true;
	}

	@Override
	public @Nullable TextFormatter getRepresentation() {
		return null;
	}

	@Override
	// this is an action type. if the arg is present, that's enough.
	public @NotNull Range getValueCountBounds() {
		return Range.NONE;
	}
}
package lanat.argumentTypes;

import io.github.darvil.terminal.textformatter.TextFormatter;
import io.github.darvil.utils.Range;
import lanat.ArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An argument type that is set in a true state if the argument was used.
 * <p>
 * A value may be specified to explicitly set the value to true or false. The following values are valid:
 * <ul>
 * <li>yes</li>
 * <li>true</li>
 * <li>no</li>
 * <li>false</li>
 * <li>1</li>
 * <li>0</li>
 * </ul>
 * @see Boolean
 */
public class BooleanArgumentType extends ArgumentType<Boolean> {
	public BooleanArgumentType() {
		super(false);
	}

	@Override
	public Boolean parseValues(String @NotNull [] values) {
		if (values.length == 0) return true;

		var arg = values[0];

		if (arg.equalsIgnoreCase("false") || arg.equalsIgnoreCase("no") || arg.equals("0"))
			return false;

		if (arg.equalsIgnoreCase("true") || arg.equalsIgnoreCase("yes") || arg.equals("1"))
			return true;

		this.addError("Invalid boolean value: " + arg);
		return null;
	}

	@Override
	public @Nullable TextFormatter getRepresentation() {
		return TextFormatter.of("boolean|empty");
	}

	@Override
	public @Nullable String getDescription() {
		return "A boolean value. If the argument is present, it is set to true. "
			+ "'yes', 'true', 'no', 'false', '1' and '0' are also valid values.";
	}

	@Override
	// this is a boolean type. if the arg is present, that's enough.
	public @NotNull Range getValueCountBounds() {
		return Range.NONE_OR_ONE;
	}
}
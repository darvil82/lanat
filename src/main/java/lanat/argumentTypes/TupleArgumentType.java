package lanat.argumentTypes;

import lanat.ArgumentType;
import lanat.utils.Range;
import lanat.utils.displayFormatter.Color;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides a base for argument types that take multiple values.
 * Shows a properly formatted description and representation.
 * @param <T>
 */
public abstract class TupleArgumentType<T> extends ArgumentType<T> {
	private final @NotNull Range argCount;

	public TupleArgumentType(@NotNull Range range, @NotNull T initialValue) {
		super(initialValue);
		this.argCount = range;
	}

	@Override
	public @NotNull Range getRequiredArgValueCount() {
		return this.argCount;
	}

	@Override
	public @NotNull TextFormatter getRepresentation() {
		return new TextFormatter(this.getValue().getClass().getSimpleName())
			.concat(new TextFormatter(this.argCount.getRegexRange()).setColor(Color.BRIGHT_YELLOW));
	}

	@Override
	public @Nullable String getDescription() {
		return "Takes " + this.argCount.getMessage("value")
			+ " of type " + this.getInitialValue().getClass().getSimpleName() + ".";
	}
}
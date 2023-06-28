package lanat.argumentTypes;

import lanat.ArgumentType;
import lanat.utils.Range;
import lanat.utils.displayFormatter.Color;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public abstract class TupleArgumentTypeType<T> extends ArgumentType<T> {
	private final @NotNull Range argCount;

	public TupleArgumentTypeType(@NotNull Range range, @NotNull T initialValue) {
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
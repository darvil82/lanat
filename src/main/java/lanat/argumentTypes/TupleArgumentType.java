package lanat.argumentTypes;

import lanat.ArgValueCount;
import lanat.ArgumentType;
import lanat.utils.displayFormatter.Color;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;


public abstract class TupleArgumentType<T> extends ArgumentType<T> {
	private final @NotNull ArgValueCount argCount;

	public TupleArgumentType(@NotNull ArgValueCount argValueCount, @NotNull T initialValue) {
		super(initialValue);
		this.argCount = argValueCount;
	}

	@Override
	public @NotNull ArgValueCount getArgValueCount() {
		return this.argCount;
	}

	@Override
	public @NotNull TextFormatter getRepresentation() {
		return new TextFormatter(this.getValue().getClass().getSimpleName())
			.concat(new TextFormatter(this.argCount.getRegexRange()).setColor(Color.BRIGHT_YELLOW));
	}
}
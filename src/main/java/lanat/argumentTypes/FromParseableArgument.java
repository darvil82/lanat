package lanat.argumentTypes;

import lanat.utils.Range;
import lanat.ArgumentType;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FromParseableArgument<T extends Parseable<TInner>, TInner> extends ArgumentType<TInner> {
	private final @NotNull T parseable;

	public FromParseableArgument(@NotNull T parseable) {
		this.parseable = parseable;
	}

	@Override
	public @Nullable TInner parseValues(@NotNull String @NotNull [] args) {
		return this.parseable.parseValues(args);
	}

	@Override
	public @NotNull Range getRequiredArgValueCount() {
		return this.parseable.getRequiredArgValueCount();
	}

	@Override
	public @Nullable TextFormatter getRepresentation() {
		return this.parseable.getRepresentation();
	}
}

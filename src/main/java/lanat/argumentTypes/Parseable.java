package lanat.argumentTypes;

import lanat.ArgValueCount;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Parseable<T> {
	@NotNull ArgValueCount getNumberOfArgValues();
	@Nullable T parseValues(@NotNull String @NotNull [] args);
	@Nullable TextFormatter getRepresentation();
}

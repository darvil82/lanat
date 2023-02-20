package lanat.argumentTypes;

import lanat.utils.Range;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Parseable<T> {
	@NotNull Range getRequiredArgValueCount();

	@Nullable T parseValues(@NotNull String @NotNull [] args);

	@Nullable TextFormatter getRepresentation();
}

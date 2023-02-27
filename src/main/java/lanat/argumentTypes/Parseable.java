package lanat.argumentTypes;

import lanat.NamedWithDescription;
import lanat.utils.Range;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Parseable<T> extends NamedWithDescription {
	@NotNull Range getRequiredArgValueCount();

	@Nullable T parseValues(@NotNull String @NotNull [] args);

	default @Nullable TextFormatter getRepresentation() {
		return null;
	}

	@Override
	default @NotNull String getName() {
		return this.getClass()
			.getSimpleName()
			.toLowerCase()
			.replaceAll("argument$", "");
	}

	@Override
	default @Nullable String getDescription() {
		return null;
	}
}

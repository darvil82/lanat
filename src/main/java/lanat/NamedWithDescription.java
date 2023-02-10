package lanat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface NamedWithDescription {
	@NotNull String getName();

	@Nullable String getDescription();
}

package lanat.helpRepresentation.descriptions;

import lanat.CommandUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class Tag {
	protected final @Nullable String value;

	public Tag(@Nullable String value) {
		this.value = value;
	}
	public Tag() {
		this(null);
	}

	protected abstract @NotNull String parse(@NotNull CommandUser user);
}

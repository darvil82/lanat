package io.github.darvil.lanat.helpRepresentation.descriptions.exceptions;

import io.github.darvil.lanat.exceptions.LanatException;
import io.github.darvil.lanat.helpRepresentation.descriptions.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Thrown when a tag is malformed. */
public class MalformedTagException extends LanatException {
	public MalformedTagException(@NotNull Class<? extends Tag> tagClass, @Nullable String reason) {
		super(
			"Tag '" + Tag.getTagNameFromTagClass(tagClass) + "' is malformed"
				+ (reason == null ? "" : ": " + reason)
		);
	}

	public MalformedTagException(@NotNull String message) {
		super(message);
	}
}

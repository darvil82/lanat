package lanat.helpRepresentation.descriptions.exceptions;

import lanat.exceptions.LanatException;
import lanat.helpRepresentation.descriptions.Tag;
import lanat.utils.UtlString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Thrown when a tag is malformed. */
public class MalformedTagException extends LanatException {
	public MalformedTagException(@NotNull Class<? extends Tag> tagClass, @Nullable String reason) {
		super(
			"Tag "
				+ UtlString.surround(Tag.getTagNameFromTagClass(tagClass))
				+ " is malformed"
				+ (reason == null ? "" : ": " + reason)
		);
	}

	public MalformedTagException(@NotNull String message) {
		super(message);
	}
}

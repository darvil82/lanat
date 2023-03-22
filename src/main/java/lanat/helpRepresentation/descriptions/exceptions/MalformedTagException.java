package lanat.helpRepresentation.descriptions.exceptions;

import lanat.exceptions.LanatException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Thrown when a tag is malformed. */
public class MalformedTagException extends LanatException {
	public MalformedTagException(@NotNull String tagName, @Nullable String reason) {
		super("tag " + tagName + " is malformed" + (reason == null ? "" : ": " + reason));
	}

	public MalformedTagException(@NotNull String message) {
		super(message);
	}
}

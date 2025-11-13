package io.github.darvil.lanat.helpRepresentation.descriptions.exceptions;

import io.github.darvil.lanat.exceptions.LanatException;
import org.jetbrains.annotations.NotNull;

/** Thrown when a tag with an unknown name is attempted to be used. */
public class UnknownTagException extends LanatException {
	public UnknownTagException(@NotNull String tagName) {
		super("Tag '" + tagName + "' does not exist");
	}
}
package io.github.darvil.lanat.exceptions;

import io.github.darvil.lanat.CommandTemplate;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when a type of field inside a {@link CommandTemplate} is incompatible with the type returned by the
 * argument type inner value.
 */
public class IncompatibleCommandTemplateTypeException extends CommandTemplateException {
	public IncompatibleCommandTemplateTypeException(@NotNull String message) {
		super(message);
	}
}
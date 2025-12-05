package io.github.darvil.lanat.exceptions;

import io.github.darvil.lanat.CommandTemplate;
import org.jetbrains.annotations.NotNull;

/** Thrown when an error occurs while parsing a {@link CommandTemplate}. */
public class CommandTemplateException extends LanatException {
	public CommandTemplateException(@NotNull String message) {
		super(message);
	}
}

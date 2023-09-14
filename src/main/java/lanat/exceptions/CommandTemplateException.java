package lanat.exceptions;

import org.jetbrains.annotations.NotNull;

/** Thrown when an error occurs while parsing a {@link lanat.CommandTemplate}. */
public class CommandTemplateException extends LanatException {
	public CommandTemplateException(@NotNull String message) {
		super(message);
	}
}

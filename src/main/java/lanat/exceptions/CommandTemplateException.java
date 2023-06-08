package lanat.exceptions;

import org.jetbrains.annotations.NotNull;

public class CommandTemplateException extends LanatException {
	public CommandTemplateException(@NotNull String message) {
		super(message);
	}
}

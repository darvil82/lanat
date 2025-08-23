package lanat.utils.errors;

import org.jetbrains.annotations.NotNull;
import textFormatter.color.Color;
import textFormatter.color.SimpleColor;

/**
 * Represents the multiple levels that an error can have.
 */
public enum ErrorLevel {
	DEBUG(SimpleColor.BRIGHT_GREEN),
	INFO(SimpleColor.BRIGHT_BLUE),
	WARNING(SimpleColor.BRIGHT_YELLOW),
	ERROR(SimpleColor.BRIGHT_RED);

	public final @NotNull Color color;

	ErrorLevel(@NotNull Color color) {
		this.color = color;
	}

	/**
	 * Returns whether this error level is under the given minimum.
	 * @param minimum The minimum to check against.
	 * @return Whether this error level is under the given minimum.
	 */
	public boolean isInMinimum(@NotNull ErrorLevel minimum) {
		return this.ordinal() >= minimum.ordinal();
	}
}
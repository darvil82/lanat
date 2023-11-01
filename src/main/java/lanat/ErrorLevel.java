package lanat;

import lanat.utils.displayFormatter.Color;
import org.jetbrains.annotations.NotNull;

public enum ErrorLevel {
	ERROR(Color.BRIGHT_RED),
	WARNING(Color.BRIGHT_YELLOW),
	INFO(Color.BRIGHT_BLUE),
	DEBUG(Color.BRIGHT_GREEN);

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
		return this.ordinal() <= minimum.ordinal();
	}
}



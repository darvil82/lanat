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

	public boolean isInErrorMinimum(@NotNull ErrorLevel minimum) {
		return this.ordinal() <= minimum.ordinal();
	}
}



package argparser.utils;

import argparser.utils.displayFormatter.Color;

public enum ErrorLevel {
	ERROR(Color.BRIGHT_RED),
	WARNING(Color.BRIGHT_YELLOW),
	INFO(Color.BRIGHT_BLUE),
	DEBUG(Color.BRIGHT_GREEN);

	public final Color color;

	ErrorLevel(Color color) {
		this.color = color;
	}

	public boolean isInErrorMinimum(ErrorLevel minimum) {
		return this.ordinal() <= minimum.ordinal();
	}
}



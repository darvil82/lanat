package argparser;

import argparser.displayFormatter.Color;

public enum ErrorLevel {
	ERROR(Color.BRIGHT_RED),
	WARNING(Color.BRIGHT_YELLOW),
	INFO(Color.BRIGHT_BLUE),
	NONE(null);

	public final Color color;

	ErrorLevel(Color color) {
		this.color = color;
	}

	public boolean isInErrorMinimum(ErrorLevel minimum) {
		return this.ordinal() <= minimum.ordinal();
	}
}


interface ErrorLevelProvider {
	ErrorLevel getErrorLevel();
}

interface MayHaveErrors {
	boolean hasErrors();
}
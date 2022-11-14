package argparser;

import argparser.displayFormatter.Color;

public enum ErrorLevel {
	ERROR(Color.BRIGHT_RED),
	WARNING(Color.BRIGHT_YELLOW),
	INFO(Color.BRIGHT_BLUE);

	public final Color color;

	ErrorLevel(Color color) {
		this.color = color;
	}
}

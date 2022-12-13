package argparser;

import argparser.displayFormatter.TextFormatter;

public class HelpFormatter {
	private final Command parentCmd;
	private byte indentSize = 3;

	HelpFormatter(Command parentCmd) {
		this.parentCmd = parentCmd;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer(this.parentCmd.name);
		if (this.parentCmd.description != null) {
			buffer.append(": ").append(this.parentCmd.description);
		}
		return buffer.toString();
	}
}

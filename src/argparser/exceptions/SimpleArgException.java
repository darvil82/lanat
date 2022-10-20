package argparser.exceptions;

import argparser.Argument;

public class SimpleArgException extends ArgParserException {
	public final Argument<?, ?> arg;
	public final short position;

	public SimpleArgException(Argument<?, ?> arg, short pos) {
		this.arg = arg;
		this.position = pos;
	}
}

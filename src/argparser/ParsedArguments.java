package argparser;

import java.util.HashMap;

public class ParsedArguments {
	private final HashMap<String, Object> parsed_args;

	public ParsedArguments(HashMap<String, Object> parsed_args) {
		this.parsed_args = parsed_args;
	}
}

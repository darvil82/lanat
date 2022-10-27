package argparser;

import java.util.HashMap;

public class ParsedArguments {
	private final HashMap<String, Object> parsed_args;
	private final ParsedArguments subArgs;
	private final String name;

	public ParsedArguments(HashMap<String, Object> parsed_args, ParsedArguments subArgs, String name) {
		this.parsed_args = parsed_args;
		this.subArgs = subArgs;
		this.name = name;
	}
}

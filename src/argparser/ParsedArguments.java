package argparser;

import java.util.HashMap;

public class ParsedArguments {
	private final HashMap<String, Object> parsedArgs;
	private final ParsedArguments subArgs;
	private final String name;

	public ParsedArguments(HashMap<String, Object> parsedArgs, ParsedArguments subArgs, String name) {
		this.parsedArgs = parsedArgs;
		this.subArgs = subArgs;
		this.name = name;
	}
}

package argparser;

import java.util.HashMap;

public class ParsedArguments {
	private final HashMap<Argument<?, ?>, Object> parsedArgs;
	private final ParsedArguments[] subArgs;
	private final String name;

	ParsedArguments(String name, HashMap<Argument<?, ?>, Object> parsedArgs, ParsedArguments[] subArgs) {
		this.parsedArgs = parsedArgs;
		this.subArgs = subArgs;
		this.name = name;
	}
}

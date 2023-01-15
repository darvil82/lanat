package argparser;

import java.util.HashMap;

public class ParsedArgumentsRoot extends ParsedArguments {
	private final String forwardValue;

	ParsedArgumentsRoot(
		String name,
		HashMap<Argument<?, ?>, Object> parsedArgs,
		ParsedArguments[] subArgs,
		String forwardValue
	) {
		super(name, parsedArgs, subArgs);
		this.forwardValue = forwardValue;
	}

	/**
	 * Returns the forward value. An empty {@link String} is returned if no forward value was specified.
	 */
	public String getForwardValue() {
		return forwardValue;
	}
}

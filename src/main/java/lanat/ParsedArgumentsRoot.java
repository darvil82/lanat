package lanat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

public class ParsedArgumentsRoot extends ParsedArguments {
	private final @NotNull String forwardValue;

	ParsedArgumentsRoot(
		@NotNull String name,
		@NotNull HashMap<@NotNull Argument<?, ?>, @Nullable Object> parsedArgs,
		@NotNull List<@NotNull ParsedArguments> subArgs,
		@NotNull String forwardValue
	)
	{
		super(name, parsedArgs, subArgs);
		this.forwardValue = forwardValue;
	}

	/**
	 * Returns the forward value. An empty {@link String} is returned if no forward value was specified.
	 */
	public @NotNull String getForwardValue() {
		return this.forwardValue;
	}
}

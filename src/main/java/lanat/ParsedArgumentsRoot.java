package lanat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Container for all the parsed arguments and their respective values.
 * Provides methods specific to the root command.
 */
public class ParsedArgumentsRoot extends ParsedArguments {
	private final @Nullable String forwardValue;

	ParsedArgumentsRoot(
		@NotNull ArgumentParser cmd,
		@NotNull HashMap<@NotNull Argument<?, ?>, @Nullable Object> parsedArgs,
		@NotNull List<@NotNull ParsedArguments> subArgs,
		@Nullable String forwardValue
	)
	{
		super(cmd, parsedArgs, subArgs);
		this.forwardValue = forwardValue;
	}

	/**
	 * Returns the forward value. The forward value is the string that is passed after the {@code --} token.
	 * @return An {@link Optional} containing the forward value, or {@link Optional#empty()} if there is no forward
	 *  value.
	 */
	public @NotNull Optional<String> getForwardValue() {
		return Optional.ofNullable(this.forwardValue);
	}
}

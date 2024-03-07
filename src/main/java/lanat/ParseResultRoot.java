package lanat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Container for all the parsed arguments and their respective values.
 * Provides methods specific to the root command.
 */
public class ParseResultRoot extends ParseResult {
	private final @Nullable String forwardValue;

	ParseResultRoot(
		@NotNull ArgumentParser cmd,
		@NotNull HashMap<@NotNull Argument<?, ?>, @Nullable Object> parsedArgumentValues,
		@NotNull List<@NotNull ParseResult> subArgs,
		@Nullable String forwardValue
	) {
		super(cmd, parsedArgumentValues, subArgs);
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

	/**
	 * Returns a list of all the {@link ParseResult} objects that belong to a command that was used by the user.
	 * The list is ordered from the root command to the last used command.
	 * <p>
	 * The list contains this {@link ParseResultRoot} object as well.
	 * @return A list of all the {@link ParseResult} objects that belong to a command that was used by the user
	 */
	public @NotNull List<@NotNull ParseResult> getUsedResults() {
		if (!this.wasUsed())
			return new ArrayList<>(0);

		ParseResult current = this;
		var list = new ArrayList<ParseResult>(1);

		do {
			list.add(current);

			current = current.subResults.stream()
				.filter(ParseResult::wasUsed)
				.findFirst()
				.orElse(null);
		} while (current != null);

		return list;
	}
}
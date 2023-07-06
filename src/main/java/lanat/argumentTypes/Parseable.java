package lanat.argumentTypes;

import lanat.NamedWithDescription;
import lanat.utils.Range;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public interface Parseable<T> extends NamedWithDescription {
	Pattern DEFAULT_NAME_REGEX = Pattern.compile("argument$", Pattern.CASE_INSENSITIVE);


	/** Specifies the number of values that this parser should receive when calling {@link #parseValues(String[])}. */
	@NotNull Range getRequiredArgValueCount();

	/**
	 * Parses the received values and returns the result. If the values are invalid, this method shall return {@code null}.
	 *
	 * @param args The values that were received.
	 * @return The parsed value.
	 */
	@Nullable T parseValues(@NotNull String @NotNull [] args);

	/** Returns the representation of this parseable type. This may appear in places like the help message. */
	default @Nullable TextFormatter getRepresentation() {
		return new TextFormatter(this.getName());
	}

	@Override
	default @NotNull String getName() {
		return Parseable.DEFAULT_NAME_REGEX
			.matcher(this.getClass().getSimpleName())
			.replaceAll("");
	}

	@Override
	default @Nullable String getDescription() {
		return null;
	}
}

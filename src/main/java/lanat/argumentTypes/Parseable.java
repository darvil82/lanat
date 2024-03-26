package lanat.argumentTypes;

import lanat.utils.NamedWithDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import textFormatter.TextFormatter;
import utils.Range;

/**
 * The basic interface for all argument types. In order to use a class that implements this interface as an
 * argument type, you must use {@link FromParseableArgumentType} to wrap it.
 * @param <T> The type that this argument type parses.
 * @see FromParseableArgumentType
 */
@FunctionalInterface
public interface Parseable<T> extends NamedWithDescription {
	/**
	 * Parses the received values and returns the result. If the values are invalid, this method shall return {@code null}.
	 * @param values The values that were received.
	 * @return The parsed value.
	 */
	@Nullable T parseValues(@NotNull String @NotNull... values);


	/**
	 * Specifies the number of times this argument type can be used during parsing.
	 * By default, this is 1. ({@link Range#ONE}).
	 * <p>
	 * <strong>Note: </strong> The minimum value must be at least 1.
	 * </p>
	 */
	default @NotNull Range getUsageCountBounds() {
		return Range.ONE;
	}

	/**
	 * Specifies the number of values that this parser should receive when calling {@link #parseValues(String[])}.
	 * By default, this is 1. ({@link Range#ONE}).
	 * */
	default @NotNull Range getValueCountBounds() {
		return Range.ONE;
	}

	/** Returns the representation of this parseable type. This may appear in places like the help message. */
	default @Nullable TextFormatter getRepresentation() {
		return TextFormatter.of(this.getName());
	}

	/**
	 * Returns the name of this argument type. By default, this is the name of the class without the "ArgumentType" suffix.
	 * @return The name of this argument type.
	 */
	@Override
	default @NotNull String getName() {
		// Remove the "ArgumentType" suffix from the class name
		return this.getClass().getSimpleName().replaceAll("ArgumentType$", "");
	}

	@Override
	default @Nullable String getDescription() {
		return null;
	}
}
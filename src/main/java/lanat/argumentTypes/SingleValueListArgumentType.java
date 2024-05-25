package lanat.argumentTypes;

import lanat.ArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import textFormatter.FormatOption;
import textFormatter.TextFormatter;
import textFormatter.color.SimpleColor;

import java.util.stream.Stream;

/**
 * An argument type that takes a single value out of a list of values.
 * @param <T> The type of the value that this argument type holds.
 */
public abstract class SingleValueListArgumentType<T> extends ArgumentType<T> {
	/** The list of values that the argument type will accept. */
	protected final @NotNull T @NotNull [] listValues;
	/** The list of string representations for the values. */
	protected final @NotNull String @NotNull [] listValuesStr;

	/**
	 * Creates a new single value list argument type.
	 * @param listValues The list of values that the argument type will accept.
	 * @param initialValue The initial value of the argument type.
	 */
	protected SingleValueListArgumentType(@NotNull T @NotNull [] listValues, @NotNull T initialValue) {
		super(initialValue);
		this.listValues = listValues;
		this.listValuesStr = this.checkValidValues();
	}

	/**
	 * Creates a new single value list argument type.
	 * @param listValues The list of values that the argument type will accept.
	 */
	protected SingleValueListArgumentType(@NotNull T @NotNull [] listValues) {
		this.listValues = listValues;
		this.listValuesStr = this.checkValidValues();
	}

	/**
	 * Checks if the list of values is valid.
	 * @return The list of string representations for the values.
	 */
	private @NotNull String @NotNull [] checkValidValues() {
		if (this.listValues.length == 0)
			throw new IllegalArgumentException("The list of values cannot be empty.");

		return Stream.of(this.listValues)
			.map(this::valueToString)
			.map(String::trim)
			.peek(v -> {
				if (v.chars().anyMatch(Character::isWhitespace))
					throw new IllegalArgumentException("Value cannot contain spaces: '" + v + "'.");
			})
			.toArray(String[]::new);
	}

	/**
	 * Converts the given value to a string.
	 * By default, this method returns the result of calling {@link Object#toString()} on the value.
	 * @param value The value to convert.
	 * @return The string representation of the value.
	 */
	protected @NotNull String valueToString(@NotNull T value) {
		return value.toString();
	}


	@Override
	public T parseValues(@NotNull String @NotNull [] values) {
		for (int i = 0; i < this.listValuesStr.length; i++) {
			if (values[0].equalsIgnoreCase(this.listValuesStr[i]))
				return this.listValues[i];
		}

		this.addError("Value '" + values[0] + "' not matching any in " + this.getRepresentation());
		return null;
	}

	@Override
	public @NotNull TextFormatter getRepresentation() {
		final var fmt = TextFormatter.of("(");
		var initialValue = this.getInitialValue();

		for (var i = 0; i < this.listValuesStr.length; i++) {
			final var valueStr = this.listValuesStr[i];

			// if value is the default value, make it bold and yellow
			if (initialValue != null && valueStr.equalsIgnoreCase(this.valueToString(initialValue)))
				fmt.concat(TextFormatter.of(valueStr)
					.withForegroundColor(SimpleColor.YELLOW)
					.addFormat(FormatOption.BOLD)
				);
			else
				fmt.concat(valueStr);

			if (i < this.listValuesStr.length - 1)
				fmt.concat(" | ");
		}

		return fmt.concat(")");
	}

	@Override
	public @Nullable String getDescription() {
		var initialValue = this.getInitialValue();

		return "Specify one of the values in "
			+ String.join(", ", Stream.of(this.listValuesStr).toList())
			+ (
				initialValue == null
					? ""
					: (". Default is " + TextFormatter.of(this.valueToString(initialValue), SimpleColor.YELLOW)
						.addFormat(FormatOption.BOLD))
			)
			+ ".";
	}
}
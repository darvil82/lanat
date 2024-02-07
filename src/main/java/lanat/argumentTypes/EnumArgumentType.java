package lanat.argumentTypes;

import lanat.ArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import textFormatter.Color;
import textFormatter.FormatOption;
import textFormatter.TextFormatter;

import java.util.stream.Stream;

/**
 * An argument type that takes an enum value.
 * By supplying a default value in the constructor, the enum type is inferred.
 * <p>
 * The user can specify any of the enum values by their names.
 * The names are case-insensitive.
 * </p>
 * @param <T> The enum type.
 */
public class EnumArgumentType<T extends Enum<T>> extends ArgumentType<T> {
	private final @NotNull T @NotNull [] values;

	/**
	 * Creates a new enum argument type.
	 * @param defaultValue The default value of the enum type. This is also used to infer the type of the enum.
	 */
	public EnumArgumentType(@NotNull T defaultValue) {
		super(defaultValue);
		this.values = defaultValue.getDeclaringClass().getEnumConstants();
	}

	@Override
	public T parseValues(@NotNull String @NotNull [] args) {
		for (var enumValue : this.values) {
			if (enumValue.name().equalsIgnoreCase(args[0])) {
				return enumValue;
			}
		}
		this.addError("Invalid enum value: '" + args[0] + "'.");
		return null;
	}

	@Override
	public @NotNull TextFormatter getRepresentation() {
		final var fmt = TextFormatter.of("(");
		for (var i = 0; i < this.values.length; i++) {
			final var value = this.values[i];

			// if value is the default value, make it bold and yellow
			if (value == this.getInitialValue())
				fmt.concat(TextFormatter.of(value.name())
					.withForegroundColor(Color.YELLOW)
					.addFormat(FormatOption.BOLD)
				);
			else
				fmt.concat(value.name());

			if (i < this.values.length - 1)
				fmt.concat(" | ");
		}
		return fmt.concat(")");
	}

	@Override
	public @Nullable String getDescription() {
		return "Specify one of the following values (case is ignored): "
			+ String.join(", ", Stream.of(this.values).map(Enum::name).toList())
			+ ". Default is " + this.getInitialValue().name() + ".";
	}
}
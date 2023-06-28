package lanat.argumentTypes;

import lanat.ArgumentType;
import lanat.utils.displayFormatter.Color;
import lanat.utils.displayFormatter.FormatOption;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class EnumArgumentType<T extends Enum<T>> extends ArgumentType<T> {
	private final @NotNull T @NotNull [] values;

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
		final var fmt = new TextFormatter("(");
		for (var i = 0; i < this.values.length; i++) {
			final var value = this.values[i];

			if (value == this.getInitialValue())
				fmt.concat(new TextFormatter(value.name())
					.setColor(Color.YELLOW)
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
			+ String.join(", ", Arrays.stream(this.values).map(Enum::name).toList())
			+ ". Default is " + this.getInitialValue().name() + ".";
	}
}

package lanat.argumentTypes;

import lanat.ArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import textFormatter.FormatOption;
import textFormatter.TextFormatter;
import textFormatter.color.SimpleColor;

import java.util.stream.Stream;

public abstract class SingleValueListArgumentType<T> extends ArgumentType<T> {
	private final @NotNull T @NotNull [] listValues;

	protected SingleValueListArgumentType(@NotNull T @NotNull [] listValues, @NotNull T initialValue) {
		super(initialValue);
		this.listValues = listValues;
	}

	protected SingleValueListArgumentType(@NotNull T @NotNull [] listValues) {
		this.listValues = listValues;
	}

	protected abstract boolean predicate(@NotNull T value, @NotNull String strValue);

	protected @NotNull String valueToString(@NotNull T value) {
		return value.toString();
	}


	@Override
	public T parseValues(@NotNull String @NotNull [] values) {
		for (var value : this.listValues) {
			for (var strValue : values) {
				if (this.predicate(value, strValue))
					return value;
			}
		}
		this.addError("Invalid value: '" + values[0] + "'.");
		return null;
	}

	@Override
	public @NotNull TextFormatter getRepresentation() {
		final var fmt = TextFormatter.of("(");
		var initialValue = this.getInitialValue();

		for (var i = 0; i < this.listValues.length; i++) {
			final var valueStr = this.valueToString(this.listValues[i]);

			// if value is the default value, make it bold and yellow
			if (initialValue != null && valueStr.equalsIgnoreCase(this.valueToString(initialValue)))
				fmt.concat(TextFormatter.of(valueStr)
					.withForegroundColor(SimpleColor.YELLOW)
					.addFormat(FormatOption.BOLD)
				);
			else
				fmt.concat(valueStr);

			if (i < this.listValues.length - 1)
				fmt.concat(" | ");
		}

		return fmt.concat(")");
	}

	@Override
	public @Nullable String getDescription() {
		return "Specify one of the following values (case is ignored): "
			+ String.join(", ", Stream.of(this.listValues).map(this::valueToString).toList())
			+ (this.getInitialValue() == null ? "" : (". Default is " + this.getInitialValue()))
			+ ".";
	}
}
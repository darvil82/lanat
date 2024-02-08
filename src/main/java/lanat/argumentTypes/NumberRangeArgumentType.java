package lanat.argumentTypes;

import lanat.ArgumentType;
import lanat.ArgumentTypeInfer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import textFormatter.Color;
import textFormatter.TextFormatter;

import java.util.Objects;

/**
 * An argument type that is a number between a minimum and maximum value.
 * @param <T> The type of number that this argument type holds.
 */
public class NumberRangeArgumentType<T extends Number & Comparable<T>> extends ArgumentType<T> {
	private final ArgumentType<T> argumentType;
	private final T min, max;

	/**
	 * Creates a new number range argument type.
	 * @param min The minimum value.
	 * @param max The maximum value.
	 * @throws lanat.exceptions.ArgumentTypeInferException If the type of the default value is not supported.
	 */
	@SuppressWarnings("unchecked")
	public NumberRangeArgumentType(@NotNull T min, @NotNull T max) {
		if (min.compareTo(max) > 0) {
			throw new IllegalArgumentException("min must be less than or equal to max");
		}

		this.argumentType = (ArgumentType<T>)ArgumentTypeInfer.get(min.getClass());
		this.registerSubType(this.argumentType);

		this.min = min;
		this.max = max;
	}

	@Override
	public @Nullable T parseValues(@NotNull String @NotNull... values) {
		var result = this.argumentType.parseValues(values);

		if (result == null) return null;

		if (result.compareTo(this.min) < 0 || result.compareTo(this.max) > 0) {
			this.addError("Value must be between " + this.min + " and " + this.max + ".");
			return null;
		}

		return result;
	}

	@Override
	public @NotNull TextFormatter getRepresentation() {
		return Objects.requireNonNull(this.argumentType.getRepresentation())
			.concat(TextFormatter.of("[%s-%s]".formatted(this.min, this.max)).withForegroundColor(Color.YELLOW));
	}

	@Override
	public @Nullable String getDescription() {
		return this.argumentType.getDescription() + " Must be between " + this.min + " and " + this.max + ". (Inclusive)";
	}
}
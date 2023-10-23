package lanat.argumentTypes;

import lanat.ArgumentType;
import lanat.ArgumentTypeInfer;
import lanat.utils.displayFormatter.Color;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
	public @Nullable T parseValues(@NotNull String... args) {
		var result = this.argumentType.parseValues(args);

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
			.concat(new TextFormatter("[%s-%s]".formatted(this.min, this.max)).withForegroundColor(Color.YELLOW));
	}

	@Override
	public @Nullable String getDescription() {
		return this.argumentType.getDescription() + " Must be between " + this.min + " and " + this.max + ". (Inclusive)";
	}
}

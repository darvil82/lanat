package lanat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Container for a parsed argument value.
 *
 * @param <T> The type of the argument value.
 */
public class ParsedArgumentValue<T> {
	private final @Nullable T value;

	ParsedArgumentValue(@Nullable T value) {
		this.value = value;
	}

	/**
	 * @return The parsed value of the argument, or <code>null</code> if the argument was not parsed.
	 */
	public @Nullable T get() {
		return this.value;
	}

	/**
	 * @return <code>true</code> if the argument was parsed, <code>false</code> otherwise.
	 */
	public boolean defined() {
		return this.value != null;
	}

	/**
	 * Specifies a function to run if the argument was parsed.
	 *
	 * @param onDefined The function to run if the argument was parsed. This function will receive the parsed value.
	 */
	public @NotNull ParsedArgumentValue<T> defined(@NotNull Consumer<T> onDefined) {
		if (this.defined()) onDefined.accept(this.value);
		return this;
	}

	/**
	 * Returns <code>true</code> if the argument was not parsed, <code>false</code> otherwise. If a single value array
	 * is passed, and the argument was parsed, this will set the first value of the array to the parsed value.
	 *
	 * @param value A single value array to set the parsed value to if the argument was parsed.
	 * @return <code>true</code> if the argument was parsed, <code>false</code> otherwise.
	 * @throws IllegalArgumentException If the value array is not of length 1
	 */
	public boolean defined(@Nullable T @NotNull [] value) {
		if (value.length != 1) {
			throw new IllegalArgumentException("value must be an array of length 1");
		}

		if (this.defined()) {
			value[0] = this.value;
			return true;
		}

		return false;
	}

	/**
	 * @return <code>true</code> if the argument was not parsed, <code>false</code> otherwise.
	 */
	public boolean undefined() {
		return this.value == null;
	}

	/**
	 * Returns the supplied fallback value if the argument was not parsed, otherwise returns the parsed value.
	 *
	 * @param fallbackValue The fallback value to return if the argument was not parsed.
	 * @return The parsed value if the argument was parsed, otherwise the fallback value.
	 */
	public T undefined(@NotNull T fallbackValue) {
		return this.defined() ? this.value : fallbackValue;
	}

	/**
	 * Specifies a supplier function that will be called when the argument is not parsed. The supplier will be called
	 * and its return value will be returned if so.
	 *
	 * @param fallbackCb The supplier function to call if the argument was not parsed.
	 * @return The parsed value if the argument was parsed, otherwise the value returned by the supplier.
	 */
	public T undefined(@NotNull Supplier<@NotNull T> fallbackCb) {
		return this.defined() ? this.value : fallbackCb.get();
	}

	/**
	 * Specifies a function to run if the argument was not parsed.
	 *
	 * @param onUndefined The function to run if the argument was not parsed.
	 */
	public @NotNull ParsedArgumentValue<T> undefined(@NotNull Runnable onUndefined) {
		if (this.undefined()) onUndefined.run();
		return this;
	}

	/**
	 * Returns <code><code>true</code></code> if the argument was parsed and the value matches the given predicate,
	 * <code>false</code> otherwise.
	 *
	 * @param predicate The predicate to test the value against (if the argument was parsed). This predicate will never
	 * 	receive a <code>null</code> value.
	 * @return <code><code>true</code></code> if the argument was parsed and the value matches the given predicate,
	 * 	<code>false</code> otherwise.
	 */
	public boolean matches(@NotNull Predicate<@Nullable T> predicate) {
		return this.defined() && predicate.test(this.value);
	}

	/**
	 * @return A {@link Optional} containing the parsed value if the argument was parsed, or an empty {@link Optional}
	 * 	otherwise.
	 */
	public Optional<T> asOptional() {
		return Optional.ofNullable(this.value);
	}
}

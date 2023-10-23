package lanat.argumentTypes;

import lanat.ArgumentType;
import lanat.utils.Range;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An argument type that uses a {@link Parseable} to parse values. If the {@link Parseable#parseValues(String[])}
 * method returns {@code null}, an error is added. The error message can be specified in the constructor.
 * @param <T> The {@link Parseable} type.
 * @param <TInner> The type of the value returned by the {@link Parseable#parseValues(String[])} method.
 * @see Parseable
 */
public class FromParseableArgumentType<T extends Parseable<TInner>, TInner> extends ArgumentType<TInner> {
	private final @NotNull T parseable;
	private final @NotNull String errorMessage;

	public FromParseableArgumentType(@NotNull T parseable, @NotNull String errorMessage) {
		this.parseable = parseable;
		this.errorMessage = errorMessage;
	}

	public FromParseableArgumentType(@NotNull T parseable) {
		this(parseable, "Invalid value for type " + parseable.getName() + ".");
	}

	@Override
	public @Nullable TInner parseValues(@NotNull String @NotNull [] args) {
		TInner result = this.parseable.parseValues(args);
		if (result == null) {
			this.addError(this.errorMessage);
		}
		return result;
	}

	@Override
	public @NotNull Range getRequiredArgValueCount() {
		return this.parseable.getRequiredArgValueCount();
	}

	@Override
	public @Nullable TextFormatter getRepresentation() {
		return this.parseable.getRepresentation();
	}

	@Override
	public @Nullable String getDescription() {
		return this.parseable.getDescription();
	}

	@Override
	public @NotNull String getName() {
		return this.parseable.getName();
	}
}

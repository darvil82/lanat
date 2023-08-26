package lanat.argumentTypes;

import lanat.ArgumentType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public abstract class NumberArgumentType<T extends Number> extends ArgumentType<T> {
	protected abstract @NotNull Function<@NotNull String, @NotNull T> getParseFunction();

	@Override
	public T parseValues(@NotNull String @NotNull [] args) {
		try {
			return this.getParseFunction().apply(args[0]);
		} catch (NumberFormatException e) {
			this.addError("Invalid " + this.getName() + " value: '" + args[0] + "'.");
			return null;
		}
	}
}

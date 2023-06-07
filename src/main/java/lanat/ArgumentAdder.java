package lanat;

import lanat.exceptions.ArgumentNotFoundException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ArgumentAdder {
	/**
	 * Inserts an argument for this command to be parsed.
	 *
	 * @param argument the argument to be inserted
	 */
	<T extends ArgumentType<TInner>, TInner> void addArgument(@NotNull Argument<T, TInner> argument);

	default <T extends ArgumentType<TInner>, TInner>
	void addArgument(@NotNull Argument.Builder<T, TInner> argument) {
		this.addArgument(argument.build());
	}

	@NotNull List<@NotNull Argument<?, ?>> getArguments();

	default boolean hasArgument(@NotNull String name) {
		for (final var argument : this.getArguments()) {
			if (argument.hasName(name)) {
				return true;
			}
		}
		return false;
	}

	default @NotNull Argument<?, ?> getArgument(@NotNull String name) {
		for (final var argument : this.getArguments()) {
			if (argument.hasName(name)) {
				return argument;
			}
		}
		throw new ArgumentNotFoundException(name);
	}
}

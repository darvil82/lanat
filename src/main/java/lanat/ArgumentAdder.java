package lanat;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ArgumentAdder {
	/**
	 * Inserts an argument for this command to be parsed.
	 *
	 * @param argument the argument to be inserted
	 */
	<T extends ArgumentType<TInner>, TInner> void addArgument(@NotNull Argument<T, TInner> argument);

	@NotNull List<@NotNull Argument<?, ?>> getArguments();
}

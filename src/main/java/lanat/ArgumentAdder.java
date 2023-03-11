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

	default <T extends ArgumentType<TInner>, TInner>
	void addArgument(@NotNull Argument.ArgumentBuilder<T, TInner> argument) {
		this.addArgument(argument.build());
	}

	@NotNull List<@NotNull Argument<?, ?>> getArguments();

	default Argument<?, ?> getArgument(@NotNull String name) {
		return this.getArguments().stream()
			.filter(a -> a.hasName(name))
			.findFirst()
			.orElse(null);
	}
}

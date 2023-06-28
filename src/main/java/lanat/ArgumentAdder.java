package lanat;

import lanat.exceptions.ArgumentAlreadyExistsException;
import lanat.exceptions.ArgumentNotFoundException;
import lanat.utils.UtlMisc;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An interface for objects that can add arguments to themselves.
 */
public interface ArgumentAdder extends NamedWithDescription {
	/**
	 * Inserts an argument into this container.
	 *
	 * @param argument the argument to be inserted
	 * @param <T> the type of the argument
	 * @param <TInner> the type of the inner value of the argument
	 */
	<T extends ArgumentType<TInner>, TInner> void addArgument(@NotNull Argument<T, TInner> argument);

	/**
	 * Inserts an argument into this container. This is a convenience method for {@link #addArgument(Argument)}.
	 * It is equivalent to {@code addArgument(argument.build())}.
	 * @param argument the argument to be inserted
	 * @param <T> the type of the argument
	 * @param <TInner> the type of the inner value of the argument
	 */
	default <T extends ArgumentType<TInner>, TInner>
	void addArgument(@NotNull ArgumentBuilder<T, TInner> argument) {
		this.addArgument(argument.build());
	}

	/**
	 * Returns a list of the arguments in this container.
	 * @return an immutable list of the arguments in this container
	 */
	@NotNull List<@NotNull Argument<?, ?>> getArguments();

	/**
	 * Checks that all the arguments in this container have unique names.
	 * @throws ArgumentAlreadyExistsException if there are two arguments with the same name
	 */
	default void checkUniqueArguments() {
		UtlMisc.requireUniqueElements(this.getArguments(), a -> new ArgumentAlreadyExistsException(a, this));
	}

	/**
	 * Checks if this container has an argument with the given name.
	 * @param name the name of the argument
	 * @return {@code true} if this container has an argument with the given name, {@code false} otherwise
	 */
	default boolean hasArgument(@NotNull String name) {
		for (final var argument : this.getArguments()) {
			if (argument.hasName(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the argument with the given name.
	 * @param name the name of the argument
	 * @return the argument with the given name
	 * @throws ArgumentNotFoundException if there is no argument with the given name
	 */
	default @NotNull Argument<?, ?> getArgument(@NotNull String name) {
		for (final var argument : this.getArguments()) {
			if (argument.hasName(name)) {
				return argument;
			}
		}
		throw new ArgumentNotFoundException(name);
	}
}

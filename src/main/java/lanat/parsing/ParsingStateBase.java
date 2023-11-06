package lanat.parsing;

import lanat.Argument;
import lanat.Command;
import lanat.utils.ErrorLevelProvider;
import lanat.utils.ErrorsContainerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public abstract class ParsingStateBase<T extends ErrorLevelProvider> extends ErrorsContainerImpl<T> {
	protected final @NotNull Command command;
	/** Whether the parsing/tokenizing has finished. */
	protected boolean hasFinished = false;

	public ParsingStateBase(@NotNull Command command) {
		super(command.getMinimumExitErrorLevel(), command.getMinimumDisplayErrorLevel());
		this.command = command;
	}

	/**
	 * Executes a callback for the argument found by the name specified.
	 *
	 * @return {@code true} if an argument was found
	 */
	protected boolean runForArgument(@NotNull String argName, @NotNull Consumer<@NotNull Argument<?, ?>> f) {
		var arg = this.getArgument(argName);
		if (arg != null) {
			f.accept(arg);
			return true;
		}
		return false;
	}


	/**
	 * Executes a callback for the argument found by the single character name specified.
	 *
	 * @return {@code true} if an argument was found
	 */
	/* This method right here looks like it could be replaced by just changing it to
	 *    return this.runForArgument(String.valueOf(argName), f);
	 *
	 * It can't. "checkMatch" has also a char overload. The former would always return false.
	 * I don't really want to make "checkMatch" have different behavior depending on the length of the string, so
	 * an overload seems better. */
	protected boolean runForArgument(char argName, @NotNull Consumer<@NotNull Argument<?, ?>> f) {
		var arg = this.getArgument(argName);
		if (arg != null) {
			f.accept(arg);
			return true;
		}
		return false;
	}

	/**
	 * Returns the argument found by the single character name specified.
	 * @param argName the name of the argument to find
	 * @return the argument found, or {@code null} if no argument was found
	 */
	protected @Nullable Argument<?, ?> getArgument(char argName) {
		for (final var argument : this.getArguments()) {
			if (argument.checkMatch(argName)) {
				return argument;
			}
		}
		return null;
	}

	/**
	 * Returns the argument found by the name specified.
	 * @param argName the name of the argument to find
	 * @return the argument found, or {@code null} if no argument was found
	 */
	protected @Nullable Argument<?, ?> getArgument(String argName) {
		for (final var argument : this.getArguments()) {
			if (argument.checkMatch(argName)) {
				return argument;
			}
		}
		return null;
	}

	protected @NotNull List<@NotNull Argument<?, ?>> getArguments() {
		return this.command.getArguments();
	}

	protected @NotNull List<@NotNull Command> getCommands() {
		return this.command.getCommands();
	}

	public boolean hasFinished() {
		return this.hasFinished;
	}
}
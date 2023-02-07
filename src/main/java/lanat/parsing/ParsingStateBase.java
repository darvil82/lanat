package lanat.parsing;

import lanat.Argument;
import lanat.Command;
import lanat.utils.ErrorLevelProvider;
import lanat.utils.ErrorsContainer;

import java.util.List;
import java.util.function.Consumer;

public abstract class ParsingStateBase<T extends ErrorLevelProvider> extends ErrorsContainer<T> {
	protected final Command command;
	/** Whether the parsing/tokenizing has finished. */
	protected boolean hasFinished = false;

	public ParsingStateBase(Command command) {
		super(command.getMinimumExitErrorLevel(), command.getMinimumDisplayErrorLevel());
		this.command = command;
	}

	/**
	 * Executes a callback for the argument found by the name specified.
	 *
	 * @return <a>ParseErrorType.ArgumentNotFound</a> if an argument was found
	 */
	protected boolean runForArgument(String argName, Consumer<Argument<?, ?>> f) {
		for (final var argument : this.getArguments()) {
			if (argument.checkMatch(argName)) {
				f.accept(argument);
				return true;
			}
		}
		return false;
	}


	/**
	 * Executes a callback for the argument found by the name specified.
	 *
	 * @return <code>true</code> if an argument was found
	 */
	/* This method right here looks like it could be replaced by just changing it to
	 *    return this.runForArgument(String.valueOf(argName), f);
	 *
	 * It can't. "checkMatch" has also a char overload. The former would always return false.
	 * I don't really want to make "checkMatch" have different behavior depending on the length of the string, so
	 * an overload seems better. */
	protected boolean runForArgument(char argName, Consumer<Argument<?, ?>> f) {
		for (final var argument : this.getArguments()) {
			if (argument.checkMatch(argName)) {
				f.accept(argument);
				return true;
			}
		}
		return false;
	}

	protected List<Argument<?, ?>> getArguments() {
		return this.command.getArguments();
	}

	protected List<Command> getSubCommands() {
		return this.command.getSubCommands();
	}

	public boolean hasFinished() {
		return this.hasFinished;
	}
}
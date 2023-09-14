package lanat;

import lanat.exceptions.CommandNotFoundException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An interface for objects that can add {@link Command}s to themselves.
 */
public interface CommandAdder {
	void addCommand(@NotNull Command command);

	/**
	 * Returns a list of all the Sub-Commands that belong to this command.
	 *
	 * @return a list of all the Sub-Commands in this command
	 */
	@NotNull List<@NotNull Command> getCommands();

	default boolean hasCommand(@NotNull String name) {
		for (final var command : this.getCommands()) {
			if (command.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the Sub-Command with the specified name.
	 *
	 * @param name the name of the command to get
	 * @return the command with the specified name
	 * @throws CommandNotFoundException if no command with the specified name exists
	 */
	default @NotNull Command getCommand(@NotNull String name) {
		for (final var command : this.getCommands()) {
			if (command.getName().equals(name)) {
				return command;
			}
		}
		throw new CommandNotFoundException(name);
	}
}

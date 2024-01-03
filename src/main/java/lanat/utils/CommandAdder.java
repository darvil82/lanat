package lanat.utils;

import lanat.Command;
import lanat.exceptions.CommandNotFoundException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An interface for objects that can add {@link Command}s to themselves.
 */
public interface CommandAdder {
	/**
	 * Adds the specified Sub-Command to this object.
	 * @param command the command to add
	 */
	void addCommand(@NotNull Command command);

	/**
	 * Returns a list of all the Sub-Commands that belong to this command.
	 * @return a list of all the Sub-Commands in this command
	 */
	@NotNull List<@NotNull Command> getCommands();

	/**
	 * Returns {@code true} if this object has a Sub-Command with the specified name.
	 * @param name the name of the command to check
	 * @return {@code true} if this object has a Sub-Command with the specified name, {@code false} otherwise
	 */
	default boolean hasCommand(@NotNull String name) {
		for (final var command : this.getCommands()) {
			if (command.hasName(name)) {
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
			if (command.hasName(name)) {
				return command;
			}
		}
		throw new CommandNotFoundException(name);
	}
}
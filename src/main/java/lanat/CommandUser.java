package lanat;

import org.jetbrains.annotations.NotNull;

/**
 * This interface is used for getting the parent Command of an object that is part of a command.
 */
public interface CommandUser {
	/**
	 * Gets the Command object that this object belongs to.
	 *
	 * @return The parent command of this object.
	 */
	Command getParentCommand();

	/**
	 * Sets the parent command of this object.
	 * @param parentCommand the parent command to set
	 */
	void registerToCommand(@NotNull Command parentCommand);
}

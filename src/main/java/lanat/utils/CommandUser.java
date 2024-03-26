package lanat.utils;

import lanat.Command;
import org.jetbrains.annotations.NotNull;

/**
 * This interface is used for objects that belong to a {@link Command}.
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
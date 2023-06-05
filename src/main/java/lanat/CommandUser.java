package lanat;

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
}

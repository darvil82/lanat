package lanat;

import org.jetbrains.annotations.NotNull;

/**
 * This interface is used for getting the parent Command of an object that is part of a command.
 */
public interface ArgumentGroupUser {
	/**
	 * Gets the {@link ArgumentGroup} object that this object belongs to.
	 *
	 * @return The parent group of this object.
	 */
	ArgumentGroup getParentGroup();

	/**
	 * Sets the parent group of this object.
	 * @param parentGroup the parent group to set
	 */
	void registerToGroup(@NotNull ArgumentGroup parentGroup);
}

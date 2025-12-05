package io.github.darvil.lanat.utils;

import io.github.darvil.lanat.Group;
import org.jetbrains.annotations.NotNull;

/**
 * This interface is used for objects that belong to an {@link Group}.
 */
public interface GroupUser {
	/**
	 * Gets the {@link Group} object that this object belongs to.
	 *
	 * @return The parent group of this object.
	 */
	Group getParentGroup();

	/**
	 * Sets the parent group of this object.
	 * @param parentGroup the parent group to set
	 */
	void registerToGroup(@NotNull Group parentGroup);
}
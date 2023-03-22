package lanat;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ArgumentGroupAdder {
	/**
	 * Adds an argument group to this element.
	 */
	void addGroup(@NotNull ArgumentGroup group);

	@NotNull List<@NotNull ArgumentGroup> getSubGroups();
}

package lanat.exceptions;

import lanat.Group;
import lanat.utils.GroupAdder;
import lanat.utils.NamedWithDescription;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when an {@link Group} is added to a container that already contains an {@link Group} with the
 * same name.
 */
public class GroupAlreadyExistsException extends ObjectAlreadyExistsException {
	public <T extends NamedWithDescription & GroupAdder>
	GroupAlreadyExistsException(@NotNull Group group, final T container) {
		super("Group", group, container);
	}
}
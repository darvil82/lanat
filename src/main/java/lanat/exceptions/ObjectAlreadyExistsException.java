package lanat.exceptions;

import lanat.utils.NamedWithDescription;
import org.jetbrains.annotations.NotNull;
import utils.UtlReflection;

/**
 * Thrown when an object is added to a container that already contains an object equal to the added one.
 */
public abstract class ObjectAlreadyExistsException extends LanatException {
	public ObjectAlreadyExistsException(
		@NotNull String typeName,
		@NotNull NamedWithDescription obj,
		@NotNull NamedWithDescription container
	)
	{
		super(
			typeName
				+ " '" + obj.getName() + "' already exists in "
				+ UtlReflection.getSimpleName(container.getClass())
				+ " '" + container.getName() + "'"
		);
	}
}
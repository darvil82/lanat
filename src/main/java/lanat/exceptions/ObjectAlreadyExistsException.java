package lanat.exceptions;

import lanat.NamedWithDescription;
import lanat.utils.UtlReflection;
import lanat.utils.UtlString;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when an object is added to a container that already contains an object equal to the added one.
 */
abstract class ObjectAlreadyExistsException extends LanatException {
	public ObjectAlreadyExistsException(
		@NotNull String typeName,
		@NotNull NamedWithDescription obj,
		@NotNull NamedWithDescription container
	)
	{
		super(
			typeName
				+ " "
				+ UtlString.surround(obj.getName())
				+ " already exists in "
				+ UtlReflection.getSimpleName(container.getClass())
				+ " "
				+ UtlString.surround(container.getName())
		);
	}
}

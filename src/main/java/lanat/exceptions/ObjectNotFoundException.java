package lanat.exceptions;

import lanat.NamedWithDescription;
import lanat.utils.UtlReflection;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when an object is not found.
 */
public class ObjectNotFoundException extends LanatException {
	public ObjectNotFoundException(
		@NotNull String typeName,
		@NotNull NamedWithDescription obj,
		@NotNull NamedWithDescription container
	)
	{
		super(
			typeName
				+ " '" + obj.getName() + "' was not found in "
				+ UtlReflection.getSimpleName(container.getClass())
				+ " '" + container.getName() + "'"
		);
	}

	public ObjectNotFoundException(@NotNull String typeName, @NotNull NamedWithDescription obj) {
		this(typeName, obj.getName());
	}

	public ObjectNotFoundException(@NotNull String typeName, @NotNull String name) {
		super(typeName + " '" + name + "' was not found");
	}
}

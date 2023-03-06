package lanat.exceptions;

import lanat.NamedWithDescription;
import lanat.utils.UtlReflection;
import lanat.utils.UtlString;

/**
 * Thrown when an object is added to a container that
 * already contains an object with the same name.
 * */
class ObjectAlreadyExistsException extends LanatException {
	public ObjectAlreadyExistsException(NamedWithDescription obj, NamedWithDescription container) {
		super(
			UtlReflection.getSimpleName(obj.getClass())
				+ " "
				+ UtlString.surround(obj.getName())
				+ " already exists in "
				+ UtlReflection.getSimpleName(container.getClass())
				+ " "
				+ UtlString.surround(container.getName())
		);
	}
}

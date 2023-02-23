package lanat.exceptions;

import lanat.NamedWithDescription;
import lanat.utils.UtlReflection;
import lanat.utils.UtlString;

class ObjectAlreadyExistsException extends RuntimeException {
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

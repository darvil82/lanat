package lanat.exceptions;

import lanat.ArgumentGroup;
import lanat.ArgumentGroupAdder;
import lanat.NamedWithDescription;

public class ArgumentGroupAlreadyExistsException extends ObjectAlreadyExistsException {
	public <T extends NamedWithDescription & ArgumentGroupAdder>
	ArgumentGroupAlreadyExistsException(final ArgumentGroup group, final T container) {
		super(group, container);
	}
}

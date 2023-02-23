package lanat.exceptions;

import lanat.Argument;
import lanat.ArgumentGroupAdder;
import lanat.NamedWithDescription;

public class ArgumentAlreadyExistsException extends ObjectAlreadyExistsException {
	public <T extends NamedWithDescription & ArgumentGroupAdder>
	ArgumentAlreadyExistsException(Argument<?, ?> argument, T container) {
		super(argument, container);
	}
}

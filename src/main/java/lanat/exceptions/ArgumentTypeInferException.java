package lanat.exceptions;

public class ArgumentTypeInferException extends ArgumentTypeException {
	public ArgumentTypeInferException(Class<?> clazz) {
		super("No argument type found for type: " + clazz.getName());
	}
}

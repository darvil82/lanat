module lanat {
	requires org.jetbrains.annotations;
	requires utils;
	requires textFormatter;

	exports lanat;
	exports lanat.argumentTypes;
	exports lanat.helpRepresentation;
	exports lanat.helpRepresentation.descriptions;
	exports lanat.helpRepresentation.descriptions.exceptions;
	exports lanat.parsing;
	exports lanat.parsing.errors;
	exports lanat.parsing.errors.formatGenerators;
	exports lanat.utils;
	exports lanat.exceptions;

	exports lanat.helpRepresentation.descriptions.tags to utils;
}
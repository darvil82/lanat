module darvil.lanat {
	requires org.jetbrains.annotations;
	requires utils;
	requires textFormatter;

	exports lanat;
	exports lanat.argumentTypes;
	exports lanat.exceptions;

	exports lanat.helpRepresentation;
	exports lanat.helpRepresentation.descriptions;
	exports lanat.helpRepresentation.descriptions.exceptions;

	exports lanat.utils;
	exports lanat.utils.errors;

	exports lanat.parsing;
	exports lanat.parsing.errors;
	exports lanat.parsing.errors.formatGenerators;
	exports lanat.parsing.errors.contexts;
	exports lanat.parsing.errors.contexts.formatting;
	exports lanat.parsing.errors.handlers;

	opens lanat.helpRepresentation.descriptions.tags to utils;
}
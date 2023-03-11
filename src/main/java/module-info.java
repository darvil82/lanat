module lanat {
	requires org.jetbrains.annotations;
    requires fade.mirror;

    exports lanat;
	exports lanat.argumentTypes;
	exports lanat.utils.displayFormatter;
	exports lanat.helpRepresentation;
	exports lanat.helpRepresentation.descriptions;
	exports lanat.helpRepresentation.descriptions.exceptions;
	exports lanat.parsing;
	exports lanat.utils;
	exports lanat.exceptions;
	exports lanat.commandTemplates;

	opens lanat.parsing.errors to fade.mirror;
	opens lanat.commandTemplates to fade.mirror;
}
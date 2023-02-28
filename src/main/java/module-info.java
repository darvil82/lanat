module lanat {
	requires org.jetbrains.annotations;
    requires fade.mirror;

    exports lanat;
	exports lanat.argumentTypes;
	exports lanat.utils.displayFormatter;
	exports lanat.helpRepresentation;
	exports lanat.parsing;
	exports lanat.utils;
	exports lanat.exceptions;

	opens lanat.parsing.errors to fade.mirror;
	opens lanat.commandTemplates to fade.mirror;
	exports lanat.helpRepresentation.descriptions;
}
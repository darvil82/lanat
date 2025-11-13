module darvil.lanat {
	requires org.jetbrains.annotations;
	requires io.github.darvil.utils;
	requires io.github.darvil.terminal.textformatter;

	exports io.github.darvil.lanat;
	exports io.github.darvil.lanat.argumentTypes;
	exports io.github.darvil.lanat.exceptions;

	exports io.github.darvil.lanat.helpRepresentation;
	exports io.github.darvil.lanat.helpRepresentation.descriptions;
	exports io.github.darvil.lanat.helpRepresentation.descriptions.exceptions;

	exports io.github.darvil.lanat.utils;
	exports io.github.darvil.lanat.utils.errors;

	exports io.github.darvil.lanat.parsing;
	exports io.github.darvil.lanat.parsing.errors;
	exports io.github.darvil.lanat.parsing.errors.formatGenerators;
	exports io.github.darvil.lanat.parsing.errors.contexts;
	exports io.github.darvil.lanat.parsing.errors.contexts.formatting;
	exports io.github.darvil.lanat.parsing.errors.handlers;

	opens io.github.darvil.lanat.helpRepresentation.descriptions.tags to io.github.darvil.utils;
}
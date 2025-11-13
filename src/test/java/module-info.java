module darvil.lanat.test {
	requires org.junit.jupiter.api;
	requires darvil.lanat;
	requires org.jetbrains.annotations;
	requires darvil.utils;
	requires darvil.textFormatter;

	exports lanat.tests to darvil.lanat, org.junit.platform.commons, darvil.utils;
	exports lanat.tests.parser.commandTemplates to darvil.lanat, org.junit.platform.commons, darvil.utils;
	exports lanat.tests.parser to darvil.lanat, org.junit.platform.commons, darvil.utils;
}
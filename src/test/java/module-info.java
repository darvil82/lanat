module darvil.lanat.test {
	requires org.junit.jupiter.api;
	requires darvil.lanat;
	requires org.jetbrains.annotations;
	requires utils;
	requires textFormatter;

	exports lanat.tests to darvil.lanat, org.junit.platform.commons, utils;
	exports lanat.tests.parser.commandTemplates to darvil.lanat, org.junit.platform.commons, utils;
	exports lanat.tests.parser to darvil.lanat, org.junit.platform.commons, utils;
}
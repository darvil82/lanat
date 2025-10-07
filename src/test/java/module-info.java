module lanat.test {
	requires org.junit.jupiter.api;
	requires lanat;
	requires org.jetbrains.annotations;
	requires utils;
	requires textFormatter;

	exports lanat.tests to lanat, org.junit.platform.commons, utils;
	exports lanat.tests.parser.commandTemplates to lanat, org.junit.platform.commons, utils;
	exports lanat.tests.parser to lanat, org.junit.platform.commons, utils;
}
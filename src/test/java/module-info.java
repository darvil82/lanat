module lanat.test {
	requires org.junit.jupiter.api;
	requires lanat;
	requires org.jetbrains.annotations;
	requires utils;
	requires textFormatter;

	exports lanat.test to org.junit.platform.commons, lanat;
	exports lanat.test.exampleTests to org.junit.platform.commons, lanat, utils;
	exports lanat.test.units to lanat, org.junit.platform.commons;
	exports lanat.test.units.commandTemplates to lanat, org.junit.platform.commons, utils;
}
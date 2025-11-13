module darvil.lanat.test {
	requires org.junit.jupiter.api;
	requires darvil.lanat;
	requires org.jetbrains.annotations;
	requires io.github.darvil.utils;
	requires io.github.darvil.terminal.textformatter;

	exports lanat.tests to darvil.lanat, org.junit.platform.commons, io.github.darvil.utils;
	exports lanat.tests.parser.commandTemplates to darvil.lanat, org.junit.platform.commons, io.github.darvil.utils;
	exports lanat.tests.parser to darvil.lanat, org.junit.platform.commons, io.github.darvil.utils;
}
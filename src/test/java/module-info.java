module darvil.lanat.test {
	requires org.junit.jupiter.api;
	requires darvil.lanat;
	requires org.jetbrains.annotations;
	requires io.github.darvil.utils;
	requires io.github.darvil.terminal.textformatter;

	exports io.github.darvil.lanat.tests to darvil.lanat, org.junit.platform.commons, io.github.darvil.utils;
	exports io.github.darvil.lanat.tests.parser.templates to darvil.lanat, org.junit.platform.commons, io.github.darvil.utils;
	exports io.github.darvil.lanat.tests.parser to darvil.lanat, org.junit.platform.commons, io.github.darvil.utils;
}
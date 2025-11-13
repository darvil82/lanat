module io.github.darvil.lanat.test {
	requires org.junit.jupiter.api;
	requires io.github.darvil.lanat;
	requires org.jetbrains.annotations;
	requires io.github.darvil.utils;
	requires io.github.darvil.terminal.textformatter;

	exports io.github.darvil.lanat.tests to io.github.darvil.lanat, org.junit.platform.commons, io.github.darvil.utils;
	exports io.github.darvil.lanat.tests.parser.templates to io.github.darvil.lanat, org.junit.platform.commons, io.github.darvil.utils;
	exports io.github.darvil.lanat.tests.parser to io.github.darvil.lanat, org.junit.platform.commons, io.github.darvil.utils;
}
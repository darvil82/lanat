module lanat.test {
	requires org.junit.jupiter.api;
	requires lanat;
	requires org.jetbrains.annotations;

	exports lanat.test to org.junit.platform.commons, lanat;
	exports lanat.test.manualTests to lanat, org.junit.platform.commons;
}
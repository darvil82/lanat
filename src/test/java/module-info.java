module lanat.test {
	requires org.junit.jupiter.api;
	requires lanat;
	requires org.jetbrains.annotations;
	requires fade.mirror;

	exports lanat.test to org.junit.platform.commons;
	opens lanat.test to fade.mirror;
}
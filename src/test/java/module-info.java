module lanat.test {
	requires org.junit.jupiter.api;
	requires lanat;
	requires org.jetbrains.annotations;
	requires reflect;

	exports lanat.test to org.junit.platform.commons;
}
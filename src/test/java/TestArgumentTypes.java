import argparser.Argument;
import argparser.ArgumentType;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class TestArgumentTypes extends UnitTests {
	private enum TestEnum {
		ONE, TWO, THREE
	}

	@Override
	public void setParser() {
		this.parser = new TestingParser("TestArgumentTypes") {{
			this.addArgument(new Argument<>("boolean", ArgumentType.BOOLEAN()));
			this.addArgument(new Argument<>(ArgumentType.COUNTER(), "counter", "c"));
			this.addArgument(new Argument<>("integer", ArgumentType.INTEGER()));
			this.addArgument(new Argument<>("float", ArgumentType.FLOAT()));
			this.addArgument(new Argument<>("string", ArgumentType.STRING()));
			this.addArgument(new Argument<>("multiple-strings", ArgumentType.STRINGS()));
			this.addArgument(new Argument<>("file", ArgumentType.FILE()));
			this.addArgument(new Argument<>("enum", ArgumentType.ENUM(TestEnum.TWO)));
			this.addArgument(new Argument<>("key-value", ArgumentType.KEY_VALUES(ArgumentType.INTEGER())));
			this.addArgument(new Argument<>("int-range", ArgumentType.INTEGER_RANGE(3, 10)));
		}};
	}

	@Test
	public void testBoolean() {
		assertTrue(this.parser.parseArgs("--boolean").<Boolean>get("boolean").get());
		assertFalse(this.parser.parseArgs("").<Boolean>get("boolean").defined());
	}

	@Test
	public void testCounter() {
		assertEquals(0, this.parser.parseArgs("").<Integer>get("counter").get());
		assertEquals(1, this.parser.parseArgs("-c").<Integer>get("counter").get());
		assertEquals(4, this.parser.parseArgs("-cccc").<Integer>get("counter").get());
	}

	@Test
	public void testInteger() {
		assertEquals(4, this.<Integer>parseArg("integer", "4"));
		this.assertNotPresent("integer");
		assertNull(this.parseArg("integer", "invalid"));
	}

	@Test
	public void testFloat() {
		assertEquals(4.67f, this.<Float>parseArg("float", "4.67"));
		this.assertNotPresent("float");
		assertNull(this.parseArg("float", "invalid"));
	}

	@Test
	public void testString() {
		assertEquals("hello", this.parseArg("string", "hello"));
		assertEquals("foo", this.parseArg("string", "foo bar"));
		assertEquals("foo bar", this.parseArg("string", "'foo bar'"));
		this.assertNotPresent("string");
	}

	@Test
	public void testStrings() {
		assertArrayEquals(new String[] { "hello" }, this.parseArg("multiple-strings", "hello"));
		assertArrayEquals(new String[] { "hello", "world" }, this.parseArg("multiple-strings", "hello world"));
		assertArrayEquals(new String[] { "hello world" }, this.parseArg("multiple-strings", "'hello world'"));
	}

	@Test
	public void testFile() {
		assertEquals("hello.txt", this.<File>parseArg("file", "hello.txt").getName());
		this.assertNotPresent("file");
	}

	@Test
	public void testEnum() {
		assertEquals(TestEnum.ONE, this.parseArg("enum", "ONE"));
		assertEquals(TestEnum.TWO, this.parseArg("enum", "TWO"));
		assertEquals(TestEnum.THREE, this.parseArg("enum", "THREE"));
		assertEquals(TestEnum.TWO, this.parser.parseArgs("").get("enum").get());
	}

	@Test
	public void testKeyValue() {
		final var hashMap = new HashMap<String, Integer>() {{
			this.put("key", 6);
			this.put("foo", 2);
			this.put("foo2", 1996);
		}};

		assertEquals(hashMap, this.parseArg("key-value", "key=6 foo=2 foo2=1996"));
		this.assertNotPresent("key-value");
		assertNull(this.parseArg("key-value", "invalid"));
	}

	@Test
	public void testIntegerRange() {
		assertEquals(4, this.<Integer>parseArg("int-range", "4"));
		this.assertNotPresent("int-range");
		assertNull(this.parseArg("int-range", "invalid"));
		assertNull(this.parseArg("int-range", "11"));
	}
}

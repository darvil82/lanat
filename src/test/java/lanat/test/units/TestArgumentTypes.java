package lanat.test.units;

import lanat.Argument;
import lanat.argumentTypes.*;
import lanat.test.TestingParser;
import lanat.test.UnitTests;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class TestArgumentTypes extends UnitTests {
	private enum TestEnum {
		ONE, TWO, THREE
	}

	@Override
	protected TestingParser setParser() {
		return new TestingParser("TestArgumentTypes") {{
			this.addArgument(Argument.createOfBoolType("boolean"));
			this.addArgument(Argument.create(new CounterArgumentType(), "counter", "c"));
			this.addArgument(Argument.create(new IntegerArgumentType(), "integer"));
			this.addArgument(Argument.create(new FloatArgumentType(), "float"));
			this.addArgument(Argument.create(new StringArgumentType(), "string"));
			this.addArgument(Argument.create(new MultipleStringsArgumentType(), "multiple-strings"));
			this.addArgument(Argument.create(new FileArgumentType(), "file"));
			this.addArgument(Argument.create(new EnumArgumentType<>(TestEnum.TWO), "enum"));
			this.addArgument(Argument.create(new KeyValuesArgumentType<>(new IntegerArgumentType()), "key-value"));
			this.addArgument(Argument.create(new IntegerRangeArgumentType(3, 10), "int-range"));
			this.addArgument(Argument.create(new TryParseArgumentType<>(Double.class), "try-parse"));
		}};
	}

	@Test
	public void testBoolean() {
		assertEquals(Boolean.TRUE, this.parser.parseGetValues("--boolean").<Boolean>get("boolean").orElse(null));
		assertEquals(Boolean.FALSE, this.parser.parseGetValues("").<Boolean>get("boolean").orElse(null));
	}

	@Test
	public void testCounter() {
		assertEquals(0, this.parser.parseGetValues("").<Integer>get("counter").orElse(null));
		assertEquals(1, this.parser.parseGetValues("-c").<Integer>get("counter").orElse(null));
		assertEquals(4, this.parser.parseGetValues("-cccc").<Integer>get("counter").orElse(null));
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
		assertEquals(TestEnum.TWO, this.parser.parseGetValues("").get("enum").orElse(null));
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

	@Test
	public void testTryParse() {
		assertEquals(4.67, this.<Double>parseArg("try-parse", "4.67"));
		this.assertNotPresent("try-parse");
		assertNull(this.parseArg("try-parse", "invalid"));
	}
}
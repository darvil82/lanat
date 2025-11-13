package lanat.tests.parser;

import io.github.darvil.utils.Range;
import lanat.Argument;
import lanat.argumentTypes.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestArgumentTypes extends TestParser {
	private enum TestEnum {
		ONE,
		@EnumArgumentType.Default
		TWO,
		THREE
	}

	private enum TestEnum2 {
		ONE,
		TWO,
		THREE
	}

	@Override
	protected TestingParser setParser() {
		return new TestingParser("TestArgumentTypes") {{
			this.addArgument(Argument.create(new BooleanArgumentType(), "boolean"));
			this.addArgument(Argument.create(new CounterArgumentType(), "counter", "c"));
			this.addArgument(Argument.create(new IntegerArgumentType(), "integer"));
			this.addArgument(Argument.create(new FloatArgumentType(), "float"));
			this.addArgument(Argument.create(new StringArgumentType(), "string"));
			this.addArgument(Argument.create(new TupleArgumentType<>(Range.AT_LEAST_ONE, new StringArgumentType()), "multiple-strings"));
			this.addArgument(Argument.create(new TupleArgumentType<>(Range.AT_LEAST_ONE, new IntegerArgumentType()), "multiple-ints")
				.defaultValue(new Integer[] { 10101 })
			);
			this.addArgument(Argument.create(new FileArgumentType(true), "file"));
			this.addArgument(Argument.create(new EnumArgumentType<>(TestEnum.class), "enum"));
			this.addArgument(Argument.create(new EnumArgumentType<>(TestEnum2.class), "enum2"));
			this.addArgument(Argument.create(new OptListArgumentType(List.of("foo", "bar", "qux"), "qux"), "optlist"));
			this.addArgument(Argument.create(new OptListArgumentType("foo", "bar", "qux"), "optlist2"));
			this.addArgument(Argument.create(new KeyValuesArgumentType<>(new IntegerArgumentType()), "key-value"));
			this.addArgument(Argument.create(new NumberRangeArgumentType<>(3, 10), "int-range"));
			this.addArgument(Argument.create(new TryParseArgumentType<>(Double.class), "try-parse"));
			this.addArgument(Argument.create(SimpleArgumentType.of((values, errorProxy) -> "hello " + values[0]), "simple"));
		}};
	}

	@Test
	public void testBoolean() {
		assertEquals(Boolean.TRUE, this.parser.parseGetValues("--boolean").<Boolean>get("boolean").orElse(null));
		assertEquals(Boolean.TRUE, this.parser.parseGetValues("--boolean true").<Boolean>get("boolean").orElse(null));
		assertEquals(Boolean.FALSE, this.parser.parseGetValues("").<Boolean>get("boolean").orElse(null));
		assertEquals(Boolean.FALSE, this.parser.parseGetValues("--boolean no").<Boolean>get("boolean").orElse(null));
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
	public void testNumbers() {
		assertArrayEquals(new Integer[] { 4 }, this.parseArg("multiple-ints", "4"));
		assertArrayEquals(new Integer[] { 4, 5, 6 }, this.parseArg("multiple-ints", "4 5 6"));
		assertArrayEquals(new Integer[] { 10101 }, this.parser.parseGetValues("").<Integer[]>get("multiple-ints").orElse(null));
	}

	@Test
	public void testFile() {
		assertNull(this.<File>parseArg("file", "hello.txt"));
	}

	@Test
	public void testEnum() {
		// test with default value
		assertEquals(TestEnum.ONE, this.parseArg("enum", "ONE"));
		assertEquals(TestEnum.TWO, this.parseArg("enum", "TWO"));
		assertEquals(TestEnum.THREE, this.parseArg("enum", "THREE"));
		assertEquals(TestEnum.TWO, this.parser.parseGetValues("").get("enum").orElse(null)); // default value

		// test without default value
		assertEquals(TestEnum2.ONE, this.parseArg("enum2", "ONE"));
		assertEquals(TestEnum2.TWO, this.parseArg("enum2", "TWO"));
		this.assertNotPresent("enum2");
	}

	@Test
	public void testOptList() {
		// test with default value
		assertEquals("foo", this.parseArg("optlist", "foo"));
		assertEquals("bar", this.parseArg("optlist", "bar"));
		assertEquals("qux", this.parseArg("optlist", "qux"));
		assertEquals("qux", this.parser.parseGetValues("").get("optlist").orElse(null)); // default value

		// test without default value
		assertEquals("foo", this.parseArg("optlist2", "foo"));
		assertEquals("bar", this.parseArg("optlist2", "bar"));
		this.assertNotPresent("optlist2");
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
	public void testNumberRange() {
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

	@Test
	public void testSimple() {
		assertEquals("hello world", this.parseArg("simple", "world"));
		this.assertNotPresent("simple");
	}
}
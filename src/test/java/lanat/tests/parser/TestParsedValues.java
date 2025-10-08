package lanat.tests.parser;

import lanat.ParseResultRoot;
import lanat.exceptions.ArgumentNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TestParsedValues extends TestParser {
	private ParseResultRoot parseArgs(String args) {
		return this.parser.parseGetValues(args);
	}

	@Test
	@DisplayName("Test the get() method")
	public void testGetSimple() {
		assertEquals("(hello), (world)", this.parseArgs("--what hello world").<String>get("what").orElse(null));
	}

	@Test
	@DisplayName("Exception thrown when querying an invalid argument")
	public void testUnknownArg() {
		assertThrows(
			ArgumentNotFoundException.class,
			() -> this.parseArgs("--what hello world").<String>get("not-there")
		);
	}

	@Test
	@DisplayName("Test querying parsed values from arguments in Sub-Commands")
	public void testNestedArguments() {
		var parsedArgs = this.parseArgs("smth subCommand -cccc another 56");
		assertEquals(4, parsedArgs.<Integer>get("subCommand", "c").orElse(null));
		assertEquals(4, parsedArgs.<Integer>get("subCommand", "c").orElse(null));

		assertEquals(56, parsedArgs.<Integer>get("subCommand", "another", "number").orElse(null));
		assertEquals(56, parsedArgs.<Integer>get("subCommand", "another", "number").orElse(null));
	}

	@Test
	@DisplayName("Test values present or not")
	public void testDefinedCallbacks() {
		var parsedArgs = this.parseArgs("smth subCommand -cccc");
		final byte[] called = { 0 };

		parsedArgs.<Integer>get("subCommand", "c").ifPresent(v -> {
			assertEquals(4, v);
			called[0]++;
		});

		parsedArgs.<Integer>get("subCommand", "another", "number").ifPresentOrElse((v) -> {}, () -> called[0]++);

		assertEquals(2, called[0]);
	}

	@Test
	@DisplayName("Test the forward value")
	public void testForwardValue() {
		{
			Optional<String> parsedArgs = this.parseArgs("foo -- hello world").getForwardValue();
			assertTrue(parsedArgs.isPresent());
			assertEquals("hello world", parsedArgs.get());
		}

		{
			Optional<String> parsedArgs = this.parseArgs("foo").getForwardValue();
			assertFalse(parsedArgs.isPresent());
		}
	}

	@Test
	@DisplayName("Test the getUsedResults() method")
	public void testGetUsedResults() {
		var usedResults = this.parseArgs("smth subCommand -cccc another 56").getUsedResults();

		assertEquals(3, usedResults.size());
		assertEquals("Testing", usedResults.get(0).getCommand().getName());
		assertEquals("subCommand", usedResults.get(1).getCommand().getName());
		assertEquals("another", usedResults.get(2).getCommand().getName());
	}

	@Test
	@DisplayName("Test the default value")
	public void testDefaultValue() {
		assertEquals(34, this.<Integer>parseArg("integer", ""));
		assertEquals(10, this.<Integer>parseArg("integer", "10"));
	}
}
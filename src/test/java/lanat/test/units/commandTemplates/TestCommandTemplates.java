package lanat.test.units.commandTemplates;

import lanat.CLInput;
import lanat.Command;
import lanat.exceptions.ArgumentNotFoundException;
import lanat.test.TestingParser;
import lanat.test.UnitTests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestCommandTemplates extends UnitTests {
	@Override
	protected TestingParser setParser() {
		return new TestingParser(CmdTemplate1.class) {{
			this.addCommand(new Command(CmdTemplate1.CmdTemplate2.class));
		}};
	}

	@Test
	@DisplayName("assert CmdTemplate1 instance is created and arguments are set")
	public void testCmdTemplate1() {
		assertEquals(56, this.<Integer>parseArg("number", "56"));
		assertEquals("hello", this.<String>parseArg("text", "hello"));
		assertTrue(this.<Boolean>parseArg("name1", ""));
		assertTrue(this.<Boolean>parseArg("f", ""));
		assertThrows(
			ArgumentNotFoundException.class,
			() -> this.parseArg("flag", "")
		);
	}

	@Test
	@DisplayName("test into method")
	public void testInto() {
		var result = this.parser.parse(CLInput.from("--number 56 --text hello -f"))
			.into(CmdTemplate1.class);

		assertTrue(result.flag);
		assertEquals(56, result.number);
		assertEquals("hello", result.text);
		assertNull(result.cmd2.number);
	}
}

package lanat.test.units.commandTemplates;

import lanat.ArgumentParser;
import lanat.CLInput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestFromInto {
	@Test
	@DisplayName("test parseFromInto method")
	public void testParseFromInto() {
		var result = ArgumentParser.parseFromInto(
			CmdTemplate1.class, CLInput.from("--number 56 --text hello -f CmdTemplate2 --number 54.0")
		);

		assertTrue(result.flag);
		assertEquals(56, result.number);
		assertEquals("hello", result.text);
		assertEquals(54.0f, result.cmd2.number);
	}
}
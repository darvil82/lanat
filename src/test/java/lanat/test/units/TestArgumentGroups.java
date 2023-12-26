package lanat.test.units;

import lanat.Argument;
import lanat.ArgumentGroup;
import lanat.test.TestingParser;
import lanat.test.UnitTests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestArgumentGroups extends UnitTests {
	@Override
	protected TestingParser setParser() {
		final var parser = super.setParser();

		parser.addGroup(new ArgumentGroup("group") {{
			this.setRestricted(true);
			this.addArgument(Argument.createOfBoolType("group-arg"));
			this.addArgument(Argument.createOfBoolType("group-arg2"));
		}});

		return parser;
	}

	@Test
	@DisplayName("Test restricted group")
	public void testRestrictedGroup() {
		var parsedArgs = this.parser.parseGetValues("--group-arg --group-arg2");
		assertEquals(Boolean.TRUE, parsedArgs.<Boolean>get("group-arg").orElse(null));
		assertEquals(Boolean.FALSE, parsedArgs.<Boolean>get("group-arg2").orElse(null)); // group-arg2 should not be present
	}
}
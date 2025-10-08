package lanat.tests.parser;

import lanat.Argument;
import lanat.Group;
import lanat.argumentTypes.IntegerArgumentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestGroups extends TestParser {
	@Override
	protected TestingParser setParser() {
		final var parser = super.setParser();

		parser.addGroup(new Group("group") {{
			this.setRestricted(true);
			this.addArgument(Argument.create(new IntegerArgumentType(), "group-arg"));
			this.addArgument(Argument.create(new IntegerArgumentType(), "group-arg2"));
		}});

		return parser;
	}

	@Test
	@DisplayName("Test restricted group")
	public void testRestrictedGroup() {
		var parsedArgs = this.parser.parseGetValues("--group-arg 5 --group-arg2 5");
		assertEquals(5, parsedArgs.<Integer>get("group-arg").orElse(null));
		this.assertNotPresent("group-arg2"); // group-arg2 should not be present
	}
}
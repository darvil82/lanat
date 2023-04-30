package lanat.test;

import lanat.Argument;
import lanat.ArgumentGroup;
import lanat.ArgumentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestArgumentGroups extends UnitTests {
	@Override
	public void setParser() {
		super.setParser();
		this.parser.addGroup(new ArgumentGroup("group") {{
			this.setExclusive(true);
			this.addArgument(Argument.create("group-arg", ArgumentType.BOOLEAN()));
			this.addArgument(Argument.create("group-arg2", ArgumentType.BOOLEAN()));
		}});
	}

	@Test
	@DisplayName("Test exclusive group")
	public void testExclusiveGroup() {
		var parsedArgs = this.parser.parseGetValues("--group-arg --group-arg2");
		assertEquals(Boolean.TRUE, parsedArgs.<Boolean>get("group-arg").get());
		assertEquals(Boolean.FALSE, parsedArgs.<Boolean>get("group-arg2").get()); // group-arg2 should not be present
	}
}
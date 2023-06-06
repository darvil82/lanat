package lanat.test.units;

import lanat.Argument;
import lanat.ArgumentType;
import lanat.helpRepresentation.HelpFormatter;
import lanat.helpRepresentation.LayoutItem;
import lanat.helpRepresentation.descriptions.DescriptionFormatter;
import lanat.helpRepresentation.descriptions.exceptions.InvalidRouteException;
import lanat.test.TestingParser;
import lanat.test.UnitTests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestHelpFormatting extends UnitTests {
	private HelpFormatter helpFormatter;

	@Override
	protected TestingParser setParser() {
		this.helpFormatter = new HelpFormatter() {
			@Override
			protected void initLayout() {
				this.setLayout(
					LayoutItem.of(DescriptionFormatter::parse)
				);
			}
		};

		return new TestingParser(
			"TestHelpFormatting",
			"description of <link=args.arg1>: (<desc=args.arg1>)"
		)
		{{
			this.addArgument(Argument.create("arg1", "a1")
				.withDescription("description of arg2: (<desc=args.arg2>)"));
			this.addArgument(Argument.create("arg2", ArgumentType.COUNTER())
				.withDescription("description of my type: (<desc=!.type>) i am in the command <link>"));
		}};
	}

	@Test
	@DisplayName("Test help formatting")
	public void testHelpFormatting() {
		assertEquals(
			"description of --arg1/a1: (description of arg2: (description of my type: "
				+ "(Counts the number of times this argument is used.) i am in the command TestHelpFormatting))",
			this.helpFormatter.generate(this.parser)
		);
	}

	@Test
	@DisplayName("Invalid tag route")
	public void testInvalidTagRoute() {
		assertThrows(
			InvalidRouteException.class,
			() -> DescriptionFormatter.parse(this.parser, "<link=!.type>")
		);
	}

	@Test
	@DisplayName("Unfinished tag route")
	public void testUnfinishedTagRoute() {
		assertThrows(
			InvalidRouteException.class,
			() -> DescriptionFormatter.parse(this.parser, "<link=args>")
		);
	}
}

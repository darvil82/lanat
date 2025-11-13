package io.github.darvil.lanat.tests.parser;

import io.github.darvil.lanat.Argument;
import io.github.darvil.lanat.argumentTypes.CounterArgumentType;
import io.github.darvil.lanat.helpRepresentation.HelpFormatter;
import io.github.darvil.lanat.helpRepresentation.LayoutItem;
import io.github.darvil.lanat.helpRepresentation.descriptions.DescriptionParser;
import io.github.darvil.lanat.helpRepresentation.descriptions.RouteParser;
import io.github.darvil.lanat.helpRepresentation.descriptions.Tag;
import io.github.darvil.lanat.helpRepresentation.descriptions.exceptions.InvalidRouteException;
import io.github.darvil.lanat.helpRepresentation.descriptions.exceptions.MalformedTagException;
import io.github.darvil.lanat.utils.NamedWithDescription;
import io.github.darvil.terminal.textformatter.FormatOption;
import io.github.darvil.terminal.textformatter.TextFormatter;
import io.github.darvil.terminal.textformatter.color.SimpleColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestHelpFormatting extends TestParser {
	private HelpFormatter helpFormatter;

	@Override
	protected TestingParser setParser() {
		this.helpFormatter = new HelpFormatter() {
			@Override
			protected void initLayout() {
				this.setLayout(
					LayoutItem.of(DescriptionParser::parse)
				);
			}
		};

		var parser = super.setParser();
		parser.setNames(List.of("TestHelpFormatting"));
		parser.setDescription("description of <link=args.arg1>: (<desc=args.arg1>)");

		parser.addArgument(Argument.createOfActionType("arg1", "a1")
			.description("description of arg2: (<desc=args.arg2>)"));
		parser.addArgument(Argument.create(new CounterArgumentType(), "arg2")
			.description("description of my type: (<desc=!.type>) i am in the command <link>"));

		return parser;
	}

	@Test
	@DisplayName("Test help formatting")
	public void testHelpFormatting() {
		assertEquals(
			"description of -arg1: (description of arg2: (description of my type: "
				+ "(Counts the number of times this argument is used.) i am in the command TestHelpFormatting))",
			this.helpFormatter.generate(this.parser)
		);
	}

	@Test
	@DisplayName("Invalid tag route")
	public void testInvalidTagRoute() {
		assertThrows(
			InvalidRouteException.class,
			() -> DescriptionParser.parse(this.parser, "<link=!.type>")
		);
	}

	@Test
	@DisplayName("Unfinished tag route")
	public void testUnfinishedTagRoute() {
		assertThrows(
			InvalidRouteException.class,
			() -> DescriptionParser.parse(this.parser, "<link=args>")
		);
	}

	@Test
	@DisplayName("Test malformed tag")
	public void testMalformedTag() {
		assertThrows(
			MalformedTagException.class,
			() -> DescriptionParser.parse(this.parser, "<link=args.arg1")
		);

		assertThrows(
			MalformedTagException.class,
			() -> DescriptionParser.parse(this.parser, "<>")
		);

		assertThrows(
			MalformedTagException.class,
			() -> DescriptionParser.parse(this.parser, "< >")
		);
	}

	@Test
	@DisplayName("Test backslash escapes")
	public void testBackslashEscaping() {
		assertEquals("<link=args.arg1>", DescriptionParser.parse(this.parser, "\\<link=args.arg1\\>"));
		assertEquals("<link=args.arg1", DescriptionParser.parse(this.parser, "\\<link=args.arg1"));
		assertEquals("link=args.arg1>", DescriptionParser.parse(this.parser, "link=args.arg1\\>"));
		assertEquals("\\", DescriptionParser.parse(this.parser, "\\"));
		assertEquals("test\\", DescriptionParser.parse(this.parser, "test\\"));
	}


	public static class TestTag extends Tag {
		@Override
		protected @NotNull String parse(@NotNull NamedWithDescription user, @Nullable String value) {
			if (value == null)
				return "No value!";
			return user.getName() + ": " + value;
		}
	}

	@Test
	@DisplayName("Test custom tag")
	public void testCustomTag() {
		Tag.register("test", TestTag.class);

		assertEquals("TestHelpFormatting: hello", DescriptionParser.parse(this.parser, "<test=hello>"));
		assertEquals("No value!", DescriptionParser.parse(this.parser, "<test>"));
		assertThrows(MalformedTagException.class, () -> DescriptionParser.parse(this.parser, "<test=>"));
	}


	@Test
	@DisplayName("Test color tag")
	public void testColorTag() {
		TextFormatter.enableSequences = true;

		assertEquals(
			SimpleColor.BRIGHT_RED.fg() + "red" + FormatOption.RESET_ALL,
			DescriptionParser.parse(this.parser, "<color=red>red")
		);
		assertEquals(
			SimpleColor.BRIGHT_BLUE.bg() + "bluebg" + FormatOption.RESET_ALL,
			DescriptionParser.parse(this.parser, "<color=:blue>bluebg")
		);
		assertEquals(
			SimpleColor.BRIGHT_RED.fg() + SimpleColor.BRIGHT_BLUE.bg() + "bluebg" + FormatOption.RESET_ALL,
			DescriptionParser.parse(this.parser, "<color=red:blue>bluebg")
		);

		TextFormatter.enableSequences = false;
	}

	@Test
	@DisplayName("Test format tag")
	public void testFormatTag() {
		TextFormatter.enableSequences = true;

		assertEquals(
			FormatOption.BOLD + "bold" + FormatOption.BOLD.reset() + " is no more" + FormatOption.RESET_ALL,
			DescriptionParser.parse(this.parser, "<format=bold>bold<format=!bold> is no more")
		);

		assertEquals(
			FormatOption.STRIKETHROUGH.toString() + FormatOption.ITALIC + "test"
				+ FormatOption.STRIKETHROUGH.reset() + " no strike"
				+ FormatOption.ITALIC.reset() + " no italics" + FormatOption.RESET_ALL,
			DescriptionParser.parse(this.parser, "<format=s,i>test<format=!s> no strike<format=!i> no italics")
		);


		TextFormatter.enableSequences = false;
	}

	@Test
	@DisplayName("Test route parser")
	public void testRouteParser() {
		var whatArg = this.parser.getArgument("what");
		var innerArg = this.parser.getCommand("subCommand")
			.getCommand("another")
			.getArgument("ball");

		assertEquals(whatArg, RouteParser.parse(whatArg, "!"));
		assertEquals(whatArg.getParentCommand(), RouteParser.parse(whatArg, ""));
		assertEquals(whatArg, RouteParser.parse(whatArg, "args.what"));
		assertEquals(whatArg.type, RouteParser.parse(whatArg, "args.what.type"));
		assertEquals(innerArg, RouteParser.parse(whatArg, "cmds.subCommand.cmds.another.args.ball"));
	}
}
package lanat.test.units.commandTemplates;

import lanat.Argument;
import lanat.Command;
import lanat.CommandTemplate;
import lanat.argumentTypes.BooleanArgumentType;
import lanat.argumentTypes.FloatArgumentType;
import lanat.argumentTypes.IntegerArgumentType;
import lanat.argumentTypes.StringArgumentType;

import java.util.Optional;

public class CmdTemplates {
	@Command.Define(names = "cmd1")
	public static class CmdTemplate1 extends CommandTemplate {
		@Argument.Define(type = IntegerArgumentType.class)
		public Integer number;

		@Argument.Define(type = StringArgumentType.class)
		public String text;

		@Argument.Define(names = { "name1", "f" }, type = BooleanArgumentType.class)
		public boolean flag;

		@Argument.Define(type = IntegerArgumentType.class)
		public Optional<Integer> numberParsedArgValue = Optional.of(0);


		@CommandAccessor
		public CmdTemplate1_1 cmd2;

		@Command.Define(names = "cmd1-1")
		public static class CmdTemplate1_1 extends CommandTemplate {
			@Argument.Define(type = FloatArgumentType.class)
			public Float number;

			@Argument.Define(type = IntegerArgumentType.class)
			public Optional<Integer> number2;
		}
	}

	@Command.Define(names = "cmd2")
	public static class CmdTemplate2 extends CommandTemplate {
		@Command.Define
		public static class CmdTemplate2_1 extends CommandTemplate { }
	}

	@Command.Define
	public static class CmdTemplate3 extends CommandTemplate {
		@Argument.Define(type = IntegerArgumentType.class, positional = true)
		public int number;

		@CommandAccessor
		public CmdTemplate3_1 cmd2;

		@Command.Define(names = "cmd3-1")
		public static class CmdTemplate3_1 extends CommandTemplate {
			@Argument.Define(type = IntegerArgumentType.class, positional = true)
			public int number;

			@CommandAccessor
			public CmdTemplate3_1_1 cmd3;

			@Command.Define(names = "cmd3-1-1")
			public static class CmdTemplate3_1_1 extends CommandTemplate {
				@Argument.Define(type = IntegerArgumentType.class, positional = true)
				public int number;
			}
		}
	}

	@Command.Define
	public static class CmdTemplate4 extends CommandTemplate {
		@Argument.Define
		public int number;

		@Argument.Define
		public String text;

		@Argument.Define
		public boolean flag;

		@Argument.Define
		public Double number2;

		@Argument.Define
		public Byte[] bytes;
	}
}
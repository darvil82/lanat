package lanat.test.units.commandTemplates;

import lanat.Argument;
import lanat.Command;
import lanat.CommandTemplate;
import lanat.argumentTypes.BooleanArgument;
import lanat.argumentTypes.FloatArgument;
import lanat.argumentTypes.IntArgument;
import lanat.argumentTypes.StringArgument;

public class CmdTemplates {
	@Command.Define(names = "cmd1")
	public static class CmdTemplate1 extends CommandTemplate {
		@Argument.Define(type = IntArgument.class)
		public int number;

		@Argument.Define(type = StringArgument.class)
		public String text;

		@Argument.Define(names = { "name1", "f" }, type = BooleanArgument.class)
		public boolean flag;


		@CommandAccessor
		public CmdTemplate1_1 cmd2;

		@Command.Define(names = "cmd1-1")
		public static class CmdTemplate1_1 extends CommandTemplate {
			@Argument.Define(type = FloatArgument.class)
			public Float number;
		}
	}

	@Command.Define(names = "cmd2")
	public static class CmdTemplate2 extends CommandTemplate {
		@Command.Define
		public static class CmdTemplate2_1 extends CommandTemplate { }
	}

	@Command.Define
	public static class CmdTemplate3 extends CommandTemplate {
		@Argument.Define(type = IntArgument.class, positional = true)
		public int number;

		@CommandAccessor
		public CmdTemplate3_1 cmd2;

		@Command.Define(names = "cmd3-1")
		public static class CmdTemplate3_1 extends CommandTemplate {
			@Argument.Define(type = IntArgument.class, positional = true)
			public int number;

			@CommandAccessor
			public CmdTemplate3_1_1 cmd3;

			@Command.Define(names = "cmd3-1-1")
			public static class CmdTemplate3_1_1 extends CommandTemplate {
				@Argument.Define(type = IntArgument.class, positional = true)
				public int number;
			}
		}
	}
}

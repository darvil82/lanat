package lanat.test.units;

import lanat.Argument;
import lanat.Command;
import lanat.CommandTemplate;
import lanat.argumentTypes.BooleanArgument;
import lanat.argumentTypes.IntArgument;
import lanat.argumentTypes.StringArgument;

@Command.Define(names = "cmd1")
public class CmdTemplate1 extends CommandTemplate {
	@Argument.Define(type = IntArgument.class)
	public int number;

	@Argument.Define(type = StringArgument.class)
	public String text;

	@Argument.Define(names = { "name1", "name2" }, type = BooleanArgument.class)
	public boolean flag;
}

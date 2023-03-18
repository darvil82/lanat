package lanat.test;

import lanat.Argument;
import lanat.ArgumentGroup;
import lanat.Command;
import lanat.argumentTypes.IntArgument;
import lanat.argumentTypes.StringArgument;
import lanat.commandTemplates.DefaultCommandTemplate;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public final class ManualTests {
	@Test
	public void main() {
//		HelpFormatter.lineWrapMax = 110;
//		HelpFormatter.debugLayout = true;
//		TextFormatter.debug = true;

		enum TestEnum {
			ONE, TWO, THREE
		}

		var parser = new TestingParser("Testing", "<color=cyan><format=b>some simple description") {{
			final var that = this;
			this.from(MyProgram.class);

			this.addGroup(new ArgumentGroup("test-group") {{
				this.addArgument(that.getArgument("string"));
				this.addArgument(that.getArgument("number"));
			}});
		}};

		var parsed = parser.parse("--string hello --number 12")
			.printErrors()
			.into(MyProgram.class);

		System.out.println(parsed.string);
		System.out.println(parsed.number);
	}
}

@Command.Define
class MyProgram extends DefaultCommandTemplate {
	public MyProgram() {}

	@Argument.Define(type = StringArgument.class, description = "This is a string argument.")
	public String string;

	@Argument.Define(type = IntArgument.class, description = "<desc=!.type>")
	public int number = 12;

	@InitDef
	public static void init(@NotNull CommandBuildHelper helper) {
		helper.getArgument("help").withDescription("This is a custom help message.");
	}
}
package lanat.commandTemplates;

import lanat.Argument;
import lanat.ArgumentParser;
import lanat.CommandTemplate;
import org.jetbrains.annotations.NotNull;

public class DefaultCommandTemplate extends CommandTemplate {
	@InitDef
	public static void init(@NotNull CommandBuildHelper helper) {
		helper.cmd().addArgument(Argument.create("help")
			.onOk(t -> System.out.println(helper.cmd().getHelp()))
			.withDescription("Shows this message.")
			.allowsUnique()
		);

		if (helper.cmd() instanceof ArgumentParser ap) {
			ap.addArgument(Argument.create("version")
				.onOk(t -> System.out.println("Version: " + ap.getVersion()))
				.withDescription("Shows the version of this program.")
				.allowsUnique()
			);
		}
	}
}

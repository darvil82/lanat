package lanat.commandTemplates;

import lanat.Argument;
import lanat.ArgumentParser;
import lanat.CommandTemplate;
import org.jetbrains.annotations.NotNull;

public class DefaultCommandTemplate extends CommandTemplate {
	/*
	 * The reason we add these arguments here is so that they do not "physically" appear in the
	 * actual class that extends this one. 'help' and 'version' are just
	 * arguments that execute actions, and they not really provide any useful values.
	 */
	@InitDef
	public static void init(@NotNull CommandBuildHelper helper) {
		helper.addArgument(Argument.create("help")
			.onOk(t -> System.out.println(helper.cmd().getHelp()))
			.withDescription("Shows this message.")
			.allowsUnique()
		);

		if (helper.cmd() instanceof ArgumentParser ap) {
			helper.addArgument(Argument.create("version")
				.onOk(t -> System.out.println("Version: " + ap.getVersion()))
				.withDescription("Shows the version of this program.")
				.allowsUnique()
			);
		}
	}
}

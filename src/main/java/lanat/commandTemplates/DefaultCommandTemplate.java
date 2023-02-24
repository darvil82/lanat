package lanat.commandTemplates;

import lanat.Argument;
import lanat.ArgumentParser;
import lanat.CommandTemplate;
import lanat.argumentTypes.BooleanArgument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultCommandTemplate extends CommandTemplate {
	@ArgDef
	public @NotNull Argument<BooleanArgument, Boolean> help() {
		return Argument.create("help")
			.onOk(t -> System.out.println(this.cmd().getHelp()))
			.description("Shows this message.")
			.allowUnique();
	}

	@ArgDef
	public @Nullable Argument<BooleanArgument, Boolean> version() {
		return this.cmd() instanceof ArgumentParser ap
			? Argument.create("version")
			.onOk(t -> System.out.println("Version: " + ap.getVersion()))
			.description("Shows the version of this program.")
			.allowUnique()
			: null;
	}
}

package lanat.argumentTypes;

import lanat.ArgumentType;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class FileArgument extends ArgumentType<File> {
	@Override
	public File parseValues(@NotNull String @NotNull [] args) {
		try {
			return new File(args[0]);
		} catch (Exception e) {
			this.addError("File not found: '" + args[0] + "'.");
			return null;
		}
	}
}

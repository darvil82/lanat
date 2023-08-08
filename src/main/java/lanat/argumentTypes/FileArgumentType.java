package lanat.argumentTypes;

import lanat.ArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * An argument type that takes a file path, and returns a {@link File} instance.
 * If the file could not be found, an error is added.
 */
public class FileArgumentType extends ArgumentType<File> {
	@Override
	public File parseValues(@NotNull String @NotNull [] args) {
		try {
			return new File(args[0]);
		} catch (Exception e) {
			this.addError("File not found: '" + args[0] + "'.");
			return null;
		}
	}

	@Override
	public @Nullable String getDescription() {
		return "A file path.";
	}
}

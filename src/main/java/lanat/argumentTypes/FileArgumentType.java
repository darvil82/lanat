package lanat.argumentTypes;

import lanat.ArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * An argument type that takes a file path and returns a {@link File} instance representing it.
 */
public class FileArgumentType extends ArgumentType<File> {
	private final boolean needsToExist;

	/**
	 * Creates a new file argument type.
	 * @param needsToExist whether the file needs to exist or not. If set to false, the file instance will be created
	 * 	even it doesn't exist.
	 */
	public FileArgumentType(boolean needsToExist) {
		this.needsToExist = needsToExist;
	}

	@Override
	public File parseValues(@NotNull String @NotNull [] args) {
		File file = new File(args[0]);
		if (this.needsToExist && !file.exists()) {
			this.addError("File does not exist.");
			return null;
		}
		return file;
	}

	@Override
	public @Nullable String getDescription() {
		return "A file path.";
	}
}

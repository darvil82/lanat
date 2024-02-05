package lanat.argumentTypes;

import lanat.ArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import textFormatter.TextFormatter;

import java.io.File;

/**
 * An argument type that takes a file path and returns a {@link File} instance representing it.
 * This argument type can also check if the file exists and if it is a regular file or a directory.
 * @see File
 */
public class FileArgumentType extends ArgumentType<File> {
	/** The type of the file. */
	public enum FileType {
		REGULAR_FILE,
		DIRECTORY,
		ANY;

		/**
		 * Returns a string representation of the file type.
		 * @param shortName whether to return a short name or not
		 * @return a string representation of the file type
		 */
		private @NotNull String toString(boolean shortName) {
			return switch (this) {
				case REGULAR_FILE -> "file";
				case DIRECTORY -> "directory";
				case ANY -> shortName ? "(file/dir)" : "file or directory";
			};
		}
	}

	private final boolean mustExist;
	private final @NotNull FileType fileType;


	/**
	 * Creates a new file argument type.
	 * @param mustExist whether the file must exist or not
	 * @param fileType the type of the file (regular file, directory, or any)
	 */
	public FileArgumentType(boolean mustExist, @NotNull FileType fileType) {
		this.mustExist = mustExist;
		this.fileType = fileType;
	}

	/**
	 * Creates a new file argument type which accepts any kind of file.
	 * @param mustExist whether the file must exist or not
	 */
	public FileArgumentType(boolean mustExist) {
		this(mustExist, FileType.ANY);
	}

	/**
	 * Checks if the file is valid. This method may add errors to the type.
	 * @param file the file to check
	 * @return whether the file is valid or not
	 */
	protected boolean checkFile(@NotNull File file) {
		if (this.mustExist && !file.exists()) {
			this.addError("File does not exist.");
			return false;
		}

		if (this.fileType == FileType.REGULAR_FILE && !file.isFile()) {
			this.addError("File is not a regular file.");
			return false;
		}

		if (this.fileType == FileType.DIRECTORY && !file.isDirectory()) {
			this.addError("File is not a directory.");
			return false;
		}

		return true;
	}

	@Override
	public File parseValues(@NotNull String @NotNull [] args) {
		File file = new File(args[0]);
		return this.checkFile(file) ? file : null;
	}

	@Override
	public @Nullable String getDescription() {
		return "A path to "
			+ (this.mustExist ? "an existing" : "a")
			+ " "
			+ this.fileType.toString(false)
			+ ".";
	}

	@Override
	public @Nullable TextFormatter getRepresentation() {
		return TextFormatter.of(
			"path" + File.separator + "to" + File.separator + this.fileType.toString(true)
		);
	}
}
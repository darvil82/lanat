package lanat;

public enum ArgumentCallbacksOption {
	/** The callback will only be invoked when there are no errors in the argument. */
	NO_ERROR_IN_ARGUMENT,

	/** The callback will only be invoked when there are no errors in the command it belongs to. */
	NO_ERROR_IN_COMMAND,

	/**
	 * The callback will only be invoked when there are no errors in the command it belongs to, and all its
	 * subcommands.
	 */
	NO_ERROR_IN_COMMAND_AND_SUBCOMMANDS,

	/** The callback will only be invoked when there are no errors in the whole command tree. */
	NO_ERROR_IN_ALL_COMMANDS,
}

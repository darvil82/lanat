package lanat;


import lanat.parsing.TokenType;
import lanat.parsing.errors.ErrorHandler;
import lanat.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class ArgumentParser extends Command {
	private boolean isParsed = false;
	private @Nullable String license;


	public ArgumentParser(@NotNull String programName, @Nullable String description) {
		super(programName, description);
	}

	public ArgumentParser(@NotNull String programName) {
		this(programName, null);
	}


	/**
	 * {@link ArgumentParser#parseArgs(String)}
	 */
	public @NotNull ParsedArgumentsRoot parseArgs(@NotNull String @NotNull [] args) {
		// if we receive the classic args array, just join it back
		return this.parseArgs(String.join(" ", args));
	}

	/**
	 * Parses the given command line arguments and returns a {@link ParsedArguments} object.
	 *
	 * @param args The command line arguments to parse.
	 */
	public @NotNull ParsedArgumentsRoot parseArgs(@NotNull String args) {
		final var res = this.parseArgsNoExit(args);
		final var errorCode = this.getErrorCode();

		for (var msg : res.second()) {
			System.err.println(msg);
		}

		if (errorCode != 0) {
			System.exit(errorCode);
		}

		return res.first();
	}

	/**
	 * Parses the arguments from the <code>sun.java.command</code> system property.
	 */
	public @NotNull ParsedArguments parseArgs() {
		var args = System.getProperty("sun.java.command").split(" ");
		return this.parseArgs(Arrays.copyOfRange(args, 1, args.length));
	}


	protected @NotNull Pair<@NotNull ParsedArgumentsRoot, @NotNull List<@NotNull String>>
	parseArgsNoExit(@NotNull String args)
	{
		if (this.isParsed) {
			// reset all parsing related things to the initial state
			this.resetState();
		}

		// pass the properties of this Sub-Command to its children recursively (most of the time this is what the user will want)
		this.passPropertiesToChildren();
		this.tokenize(args); // first. This will tokenize all Sub-Commands recursively
		var errorHandler = new ErrorHandler(this);
		this.parse(); // same thing, this parses all the stuff recursively

		this.invokeCallbacks();

		this.isParsed = true;

		return new Pair<>(this.getParsedArguments(), errorHandler.handleErrorsGetMessages());
	}

	@Override
	@NotNull
	ParsedArgumentsRoot getParsedArguments() {
		return new ParsedArgumentsRoot(
			this,
			this.getParser().getParsedArgumentsHashMap(),
			this.subCommands.stream().map(Command::getParsedArguments).toList(),
			this.getForwardValue()
		);
	}

	private @NotNull String getForwardValue() {
		final var tokens = this.getFullTokenList();
		final var lastToken = tokens.get(tokens.size() - 1);

		if (lastToken.type() == TokenType.FORWARD_VALUE)
			return lastToken.contents();

		return "";
	}

	public @Nullable String getLicense() {
		return this.license;
	}

	public void setLicense(@NotNull String license) {
		this.license = license;
	}
}
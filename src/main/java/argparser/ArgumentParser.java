package argparser;


import argparser.parsing.errors.ErrorHandler;
import argparser.utils.Pair;

import java.util.Arrays;
import java.util.List;

public class ArgumentParser extends Command {
	private boolean isParsed = false;
	private String license;


	public String getLicense() {
		return this.license;
	}

	public void setLicense(String license) {
		this.license = license;
	}


	public ArgumentParser(String programName, String description) {
		super(programName, description, true);
	}

	public ArgumentParser(String programName) {
		this(programName, null);
	}


	/**
	 * {@link ArgumentParser#parseArgs(String)}
	 */
	public ParsedArgumentsRoot parseArgs(String[] args) {
		// if we receive the classic args array, just join it back
		return this.parseArgs(String.join(" ", args));
	}

	/**
	 * Parses the given command line arguments and returns a {@link ParsedArguments} object.
	 *
	 * @param args The command line arguments to parse.
	 */
	public ParsedArgumentsRoot parseArgs(String args) {
		if (this.isParsed) {
			// reset all parsing related things to the initial state
			this.resetState();
		}

		// pass the properties of this subcommand to its children recursively (most of the time this is what the user will want)
		this.passPropertiesToChildren();
		this.tokenize(args); // first. This will tokenize all subCommands recursively
		var errorHandler = new ErrorHandler(this);
		this.parseTokens(); // same thing, this parses all the stuff recursively

		this.invokeCallbacks();
		errorHandler.handleErrorsPrint();
		int errorCode = errorHandler.getErrorCode();

		if (errorCode != 0) {
			System.exit(errorCode);
		}

		this.isParsed = true;

		return this.getParsedArguments();
	}

	/**
	 * Parses the arguments from the <code>sun.java.command</code> system property.
	 */
	public ParsedArguments parseArgs() {
		var args = System.getProperty("sun.java.command").split(" ");
		return this.parseArgs(Arrays.copyOfRange(args, 1, args.length));
	}


	/**
	 * <b>DO NOT USE.</b> This is only used for testing purposes.
	 */
	protected Pair<ParsedArgumentsRoot, List<String>> __parseArgsNoExit(String args) {
		if (this.isParsed) {
			// reset all parsing related things to the initial state
			this.resetState();
		}

		this.passPropertiesToChildren();
		this.tokenize(args);
		var errorHandler = new ErrorHandler(this);
		this.parseTokens();
		this.invokeCallbacks();

		this.isParsed = true;

		return new Pair<>(this.getParsedArguments(), errorHandler.handleErrorsGetMessages());
	}

	@Override
	ParsedArgumentsRoot getParsedArguments() {
		return new ParsedArgumentsRoot(
			this.name,
			this.parsingState.getParsedArgumentsHashMap(),
			this.subCommands.stream().map(Command::getParsedArguments).toList(),
			this.getForwardValue()
		);
	}

	private String getForwardValue() {
		for (var token : this.getFullTokenList()) {
			if (token.type() == TokenType.FORWARD_VALUE) return token.contents();
		}
		return "";
	}
}
package argparser;


import argparser.utils.Pair;
import java.util.Arrays;

public class ArgumentParser extends Command {
	public ArgumentParser(String programName, String description) {
		super(programName, description, true);
	}

	public ArgumentParser(String programName) {
		this(programName, null);
	}


	/**
	 * {@link ArgumentParser#parseArgs(String)}
	 */
	public ParsedArguments parseArgs(String[] args) {
		// if we receive the classic args array, just join it back
		return this.parseArgs(String.join(" ", args));
	}

	/**
	 * Parses the given command line arguments and returns a {@link ParsedArguments} object.
	 * @param args The command line arguments to parse.
	 */
	public ParsedArguments parseArgs(String args) {
		this.initParsingState();
		this.tokenize(args); // first. This will tokenize all subCommands recursively
		var errorHandler = new ErrorHandler(this);
		this.parseTokens(); // same thing, this parses all the stuff recursively

		this.invokeCallbacks();
		errorHandler.handleErrorsView();
		int errorCode = errorHandler.getErrorCode();

		if (errorCode != 0) {
			System.exit(errorCode);
		}

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
	protected Pair<ParsedArguments, Integer> __parseArgsNoExit(String args) {
		this.initParsingState();
		this.tokenize(args); // first. This will tokenize all subCommands recursively
		var errorHandler = new ErrorHandler(this);
		this.parseTokens(); // same thing, this parses all the stuff recursively

		this.invokeCallbacks();
		errorHandler.handleErrorsView();
		int errorCode = errorHandler.getErrorCode();

		if (errorCode != 0) {
			return new Pair<>(null, errorCode);
		}

		return new Pair<>(this.getParsedArguments(), 0);
	}
}
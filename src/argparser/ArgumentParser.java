package argparser;

import argparser.utils.Pair;

import java.util.ArrayList;
import java.util.List;

public class ArgumentParser extends Command {
	private final List<CustomParseError> extraErrors = new ArrayList<>();
	public ArgumentParser(String programName, String description) {
		super(programName, description, true);
	}

	public ArgumentParser(String programName) {
		this(programName, null);
	}

	public void addError(String message, ErrorLevel level) {
		this.extraErrors.add(new CustomParseError(message, level));
	}

	public ParsedArguments parseArgs(String[] args) {
		// if we receive the classic args array, just join it back
		return this.parseArgs(String.join(" ", args));
	}

	public ParsedArguments parseArgs(String args) {
		this.initParsingState();
		this.tokenize(args); // first. This will tokenize all subCommands recursively
		var errorHandler = new ErrorHandler(this, this.extraErrors);
		this.parseTokens(); // same thing, this parses all the stuff recursively

		errorHandler.handleErrors();

		int errorCode = errorHandler.getErrorCode();

		if (errorCode != 0) {
			System.exit(errorCode);
		}

		this.getTokenizedSubCommands().forEach(Command::finishParsing);

		return new ParsedArguments(null, null, null);
	}


	/**
	 * <b>DO NOT USE.</b> This is only used for testing purposes.
	 */
	protected Pair<ParsedArguments, Integer> __parseArgsNoExit(String args) {
		this.initParsingState();
		this.tokenize(args); // first. This will tokenize all subCommands recursively
		var errorHandler = new ErrorHandler(this, this.extraErrors);
		this.parseTokens(); // same thing, this parses all the stuff recursively

		errorHandler.handleErrors();

		int errorCode = errorHandler.getErrorCode();

		return new Pair<>(null, errorCode);
	}
}
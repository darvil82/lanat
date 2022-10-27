package argparser;

public class ArgumentParser extends Command {
	public ArgumentParser(String programName, String description) {
		super(programName, description);
		this.addArgument(new Argument<>("help", ArgumentType.BOOLEAN())
			.callback(t -> System.out.println(this.getHelp()))
		);
	}

	public ArgumentParser(String programName) {
		this(programName, null);
	}


	public ParsedArguments parseArgs(String[] args) throws Exception {
		// if we receive the classic args array, just join it back
		return this.parseArgs(String.join(" ", args));
	}

	public ParsedArguments parseArgs(String args) {
		var res = this.tokenize(args); // first. This will tokenize all subCommands recursively
		this.debugShit();
		var res2 = this.parseTokens(); // same thing, this parses all the stuff recursively

		return new ParsedArguments(null, null, null);
	}

	public ArgumentParser tupleCharacter(TupleCharacter tupleCharacter) {
		this.tupleChars = tupleCharacter.getCharPair();
		return this;
	}
}
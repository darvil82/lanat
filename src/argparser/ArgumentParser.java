package argparser;

public class ArgumentParser extends Command {
	private TupleCharacter tupleCharacter = TupleCharacter.SquareBrackets;


	public ArgumentParser(String programName, String description) {
		super(programName, description);
		this.addArgument(new Argument<>("help", ArgumentType.BOOLEAN())
			.callback(t -> System.out.println(this.getHelp()))
		);
	}

	public ArgumentParser(String programName) {
		this(programName, "");
	}


	public ParsedArguments parseArgs(String[] args) throws Exception {
		// if we receive the classic args array, just join it back
		return this.parseArgs(String.join(" ", args));
	}

	public ParsedArguments parseArgs(String args) {
		this.tokenize(args); // first. This will tokenize all subCommands recursively too
		this.debugShit();
		this.parse(); // same thing, this parses all the stuff recursively
		return new ParsedArguments(null);
	}

	public ArgumentParser tupleCharacter(TupleCharacter tupleCharacter) {
		this.tupleCharacter = tupleCharacter;
		return this;
	}
}
package argparser;

import java.util.ArrayList;

public class ArgumentParser {
	protected final String programName, description;
	protected ArrayList<Argument<?, ?>> arguments = new ArrayList<>();
	private TupleCharacter tupleCharacter = TupleCharacter.SquareBrackets;


	public ArgumentParser(String programName, String description) {
		this.programName = programName;
		this.description = description;
//		this.addArgument('h', "help", ArgumentType.BOOLEAN(), t -> System.out.println(this.getHelp()));
		this.addArgument(new Argument<>('h', "help", ArgumentType.BOOLEAN())
			.callback(t -> System.out.println(this.getHelp()))
		);
	}

	public ArgumentParser(String programName) {
		this(programName, "");
	}

	public String getHelp() {
		return "This is the help of the program.";
	}

	public <T extends ArgumentType<TInner>, TInner>
	void addArgument(Argument<T, TInner> argument) {
		arguments.add(argument);
	}

	public void parseArgs(String[] args) throws Exception {
		this.parseArgs(String.join("", args));
	}

	public ParsedArguments parseArgs(String args) throws Exception {
		ParserState ps = new ParserState(String.join("", args), this.arguments, tupleCharacter);
		return new ParsedArguments(ps.parse());
	}

	public ArgumentParser tupleCharacter(TupleCharacter tupleCharacter) {
		this.tupleCharacter = tupleCharacter;
		return this;
	}
}
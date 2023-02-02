package argparser;

import argparser.helpRepresentation.HelpFormatter;
import argparser.parsing.Parser;
import argparser.parsing.Tokenizer;
import argparser.parsing.errors.CustomError;
import argparser.utils.*;
import argparser.utils.displayFormatter.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A command is a container for {@link Argument}s and other Sub{@link Command}s.
 */
public class Command
	extends ErrorsContainer<CustomError>
	implements ErrorCallbacks<Command, Command>, ArgumentAdder, ArgumentGroupAdder, Resettable, NamedWithDescription
{
	public final String name, description;
	final ArrayList<Argument<?, ?>> arguments = new ArrayList<>();
	final ArrayList<Command> subCommands = new ArrayList<>();
	final ArrayList<ArgumentGroup> argumentGroups = new ArrayList<>();
	private final ModifyRecord<TupleCharacter> tupleChars = new ModifyRecord<>(TupleCharacter.SQUARE_BRACKETS);
	private final ModifyRecord<Integer> errorCode = new ModifyRecord<>(1);
	private Consumer<Command> onErrorCallback;
	private Consumer<Command> onCorrectCallback;
	private boolean isRootCommand = false;
	private final ModifyRecord<HelpFormatter> helpFormatter = new ModifyRecord<>(new HelpFormatter(this));

	/**
	 * A pool of the colors that an argument will have when being represented on the help
	 */
	final LoopPool<Color> colorsPool = new LoopPool<>(-1, Color.getBrightColors());


	public Command(String name, String description) {
		if (!UtlString.matchCharacters(name, Character::isAlphabetic)) {
			throw new IllegalArgumentException("name must be alphabetic");
		}
		this.name = UtlString.sanitizeName(name);
		this.description = description;
		this.addArgument(Argument.simple("help")
			.onOk(t -> System.out.println(this.getHelp()))
			.description("Shows this message.")
			.allowUnique()
		);
	}

	public Command(String name) {
		this(name, null);
	}

	Command(String name, String description, boolean isRootCommand) {
		this(name, description);
		this.isRootCommand = isRootCommand;
	}

	@Override
	public <T extends ArgumentType<TInner>, TInner>
	void addArgument(Argument<T, TInner> argument) {
		if (this.arguments.stream().anyMatch(a -> a.equals(argument))) {
			throw new IllegalArgumentException("duplicate argument identifier '" + argument.getLongestName() + "'");
		}
		argument.setParentCmd(this);
		this.arguments.add(argument);
	}

	@Override
	public void addGroup(ArgumentGroup group) {
		if (this.argumentGroups.stream().anyMatch(g -> g.name.equals(group.name))) {
			throw new IllegalArgumentException("duplicate group identifier '" + group.name + "'");
		}
		group.registerGroup(this);
		this.argumentGroups.add(group);
	}

	@Override
	public List<ArgumentGroup> getSubGroups() {
		return Collections.unmodifiableList(this.argumentGroups);
	}

	public void addSubCommand(Command cmd) {
		Objects.requireNonNull(cmd);

		if (this.subCommands.stream().anyMatch(a -> a.name.equals(cmd.name))) {
			throw new IllegalArgumentException("cannot create two sub commands with the same name");
		}

		if (cmd.isRootCommand) {
			throw new IllegalArgumentException("cannot add root command as sub command");
		}

		this.subCommands.add(cmd);
	}

	public List<Command> getSubCommands() {
		return Collections.unmodifiableList(this.subCommands);
	}

	/**
	 * Specifies the error code that the program should return when this command failed to parse.
	 * When multiple commands fail, the program will return the result of the OR bit operation that will be
	 * applied to all other command results. For example:
	 * <ul>
	 *     <li>Command 'foo' has a return value of 2. <code>(0b010)</code></li>
	 *     <li>Command 'bar' has a return value of 5. <code>(0b101)</code></li>
	 * </ul>
	 * Both commands failed, so in this case the resultant return value would be 7 <code>(0b111)</code>.
	 */
	public void setErrorCode(int errorCode) {
		this.errorCode.set(errorCode);
	}

	public void setTupleChars(TupleCharacter tupleChars) {
		this.tupleChars.set(tupleChars);
	}

	public TupleCharacter getTupleChars() {
		return tupleChars.get();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	public void setHelpFormatter(HelpFormatter helpFormatter) {
		helpFormatter.setParentCmd(this);
		this.helpFormatter.set(helpFormatter);
	}

	public void addError(String message, ErrorLevel level) {
		this.addError(new CustomError(message, level));
	}

	public String getHelp() {
		return this.helpFormatter.get().toString();
	}

	public HelpFormatter getHelpFormatter() {
		return this.helpFormatter.get();
	}

	@Override
	public List<Argument<?, ?>> getArguments() {
		return Collections.unmodifiableList(this.arguments);
	}

	public List<Argument<?, ?>> getPositionalArguments() {
		return this.getArguments().stream().filter(Argument::isPositional).toList();
	}

	/**
	 * Returns true if an argument with {@link Argument#allowUnique()} in the command was used.
	 */
	public boolean uniqueArgumentReceivedValue() {
		return this.arguments.stream().anyMatch(a -> a.getUsageCount() >= 1 && a.allowsUnique())
			|| this.subCommands.stream().anyMatch(Command::uniqueArgumentReceivedValue);
	}

	public boolean isRootCommand() {
		return this.isRootCommand;
	}

	@Override
	public String toString() {
		return "Command[name='%s', description='%s', arguments=%s, subCommands=%s]"
			.formatted(
				this.name, this.description, this.arguments, this.subCommands
			);
	}

	ParsedArguments getParsedArguments() {
		return new ParsedArguments(
			this.name,
			this.parser.getParsedArgumentsHashMap(),
			this.subCommands.stream().map(Command::getParsedArguments).toList()
		);
	}

	/**
	 * Get all the tokens of all subcommands (the ones that we can get without errors)
	 * into one single list. This includes the {@link TokenType#SUB_COMMAND} tokens.
	 */
	public ArrayList<Token> getFullTokenList() {
		final ArrayList<Token> list = new ArrayList<>() {{
			add(new Token(TokenType.SUB_COMMAND, name));
			addAll(Command.this.getTokenizer().getFinalTokens());
		}};

		final Command subCmd = this.getTokenizer().getTokenizedSubCommand();

		if (subCmd != null) {
			list.addAll(subCmd.getFullTokenList());
		}

		return list;
	}

	/**
	 * Inherits certain properties from another command, only if they are not already set to something.
	 */
	private void inheritProperties(Command parent) {
		this.tupleChars.setIfNotModified(parent.tupleChars);
		this.getMinimumExitErrorLevel().setIfNotModified(parent.getMinimumExitErrorLevel());
		this.getMinimumDisplayErrorLevel().setIfNotModified(parent.getMinimumDisplayErrorLevel());
		this.errorCode.setIfNotModified(parent.errorCode);
		this.helpFormatter.setIfNotModified(() -> {
			/* NEED TO BE COPIED!! If we don't then all commands will have the same formatter,
			 * which causes lots of problems.
			 *
			 * Stuff like the layout generators closures are capturing the reference to the previous Command
			 * and will not be updated properly when the parent command is updated. */
			var fmt = new HelpFormatter(parent.helpFormatter.get());
			fmt.setParentCmd(this); // we need to update the parent command!
			return fmt;
		});

		this.passPropertiesToChildren();
	}

	void passPropertiesToChildren() {
		this.subCommands.forEach(c -> c.inheritProperties(this));
	}

	// ------------------------------------------------ Error Handling ------------------------------------------------

	@Override
	public void setOnErrorCallback(Consumer<Command> callback) {
		this.onErrorCallback = callback;
	}

	@Override
	public void setOnCorrectCallback(Consumer<Command> callback) {
		this.onCorrectCallback = callback;
	}

	@Override
	public void invokeCallbacks() {
		if (this.hasExitErrors()) {
			if (this.onErrorCallback != null) this.onErrorCallback.accept(this);
		} else {
			if (this.onCorrectCallback != null) this.onCorrectCallback.accept(this);
		}
		this.parser.getParsedArgumentsHashMap().forEach(Argument::invokeCallbacks);
		this.subCommands.forEach(Command::invokeCallbacks);
	}

	@Override
	public boolean hasExitErrors() {
		var tokenizedSubCommand = this.getTokenizer().getTokenizedSubCommand();

		return super.hasExitErrors()
			|| tokenizedSubCommand != null && tokenizedSubCommand.hasExitErrors()
			|| this.arguments.stream().anyMatch(Argument::hasExitErrors)
			|| this.parser.hasExitErrors()
			|| this.tokenizer.hasExitErrors();
	}

	@Override
	public boolean hasDisplayErrors() {
		var tokenizedSubCommand = this.getTokenizer().getTokenizedSubCommand();

		return super.hasDisplayErrors()
			|| tokenizedSubCommand != null && tokenizedSubCommand.hasDisplayErrors()
			|| this.arguments.stream().anyMatch(Argument::hasDisplayErrors)
			|| this.parser.hasDisplayErrors()
			|| this.tokenizer.hasDisplayErrors();
	}

	public int getErrorCode() {
		int errCode = this.subCommands.stream()
			.filter(c -> c.tokenizer.isFinishedTokenizing())
			.map(sc -> sc.getMinimumExitErrorLevel().get().isInErrorMinimum(this.getMinimumExitErrorLevel().get()) ? sc.getErrorCode() : 0)
			.reduce(0, (a, b) -> a | b);

		/* If we have errors, or the subcommands had errors, do OR with our own error level.
		 * By doing this, the error code of a subcommand will be OR'd with the error codes of all its parents. */
		if (
			this.hasExitErrors() || errCode != 0
		) {
			errCode |= this.errorCode.get();
		}

		return errCode;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//                                         Argument tokenization and parsing    							      //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private Tokenizer tokenizer = new Tokenizer(this);
	private Parser parser = new Parser(this);

	public Tokenizer getTokenizer() {
		return tokenizer;
	}

	public Parser getParser() {
		return parser;
	}

	void tokenize(String input) {
		// this tokenizes recursively!
		this.tokenizer.tokenize(input);
	}

	void parse() {
		// first we need to set the tokens of all tokenized subcommands
		Command cmd = this;
		do {
			cmd.parser.setTokens(cmd.tokenizer.getFinalTokens());
		} while ((cmd = cmd.getTokenizer().getTokenizedSubCommand()) != null);

		// this parses recursively!
		this.parser.parseTokens();
	}

	@Override
	public void resetState() {
		this.tokenizer = new Tokenizer(this);
		this.parser = new Parser(this);
		this.arguments.forEach(Argument::resetState);
		this.argumentGroups.forEach(ArgumentGroup::resetState);

		this.subCommands.forEach(Command::resetState);
	}
}


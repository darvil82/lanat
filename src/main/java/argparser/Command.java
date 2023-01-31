package argparser;

import argparser.helpRepresentation.HelpFormatter;
import argparser.utils.*;
import argparser.utils.displayFormatter.Color;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

/**
 * A command is a container for {@link Argument}s and other Sub{@link Command}s.
 */
public class Command
	extends ErrorsContainer<CustomError>
	implements ErrorCallbacks<Command, Command>, ArgumentAdder, ArgumentGroupAdder, Resettable
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

	boolean isRootCommand() {
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
			this.parsingState.getParsedArgumentsHashMap(),
			this.subCommands.stream().map(Command::getParsedArguments).toList()
		);
	}

	/**
	 * Get all the tokens of all subcommands (the ones that we can get without errors)
	 * into one single list. This includes the {@link TokenType#SUB_COMMAND} tokens.
	 */
	protected ArrayList<Token> getFullTokenList() {
		final ArrayList<Token> list = new ArrayList<>() {{
			add(new Token(TokenType.SUB_COMMAND, name));
			addAll(Command.this.parsingState.tokens);
		}};

		final Command subCmd = this.getTokenizedSubCommand();

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
			// NEED TO BE COPIED!! if we don't then all commands will have the same formatter
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
		this.parsingState.getParsedArgumentsHashMap().forEach(Argument::invokeCallbacks);
		this.subCommands.forEach(Command::invokeCallbacks);
	}

	@Override
	public boolean hasExitErrors() {
		var tokenizedSubCommand = this.getTokenizedSubCommand();

		return super.hasExitErrors()
			|| tokenizedSubCommand != null && tokenizedSubCommand.hasExitErrors()
			|| this.arguments.stream().anyMatch(Argument::hasExitErrors)
			|| this.parsingState.hasExitErrors()
			|| this.tokenizingState.hasExitErrors();
	}

	@Override
	public boolean hasDisplayErrors() {
		var tokenizedSubCommand = this.getTokenizedSubCommand();

		return super.hasDisplayErrors()
			|| tokenizedSubCommand != null && tokenizedSubCommand.hasDisplayErrors()
			|| this.arguments.stream().anyMatch(Argument::hasDisplayErrors)
			|| this.parsingState.hasDisplayErrors()
			|| this.tokenizingState.hasDisplayErrors();
	}

	public int getErrorCode() {
		int errCode = this.subCommands.stream()
			.filter(c -> c.tokenizingState.finishedTokenizing)
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

	private abstract class ParsingStateBase<T extends ErrorLevelProvider> extends ErrorsContainer<T> {
		public ParsingStateBase() {
			super(Command.this.getMinimumExitErrorLevel(), Command.this.getMinimumDisplayErrorLevel());
		}
	}

	class TokenizingState extends ParsingStateBase<TokenizeError> {
		public boolean tupleOpen = false;
		public boolean stringOpen = false;
		public boolean finishedTokenizing = false;

		void addError(TokenizeError.TokenizeErrorType type, int index) {
			this.addError(new TokenizeError(type, index));
		}

	}

	class ParsingState extends ParsingStateBase<ParseError> {
		private final ArrayList<CustomError> customErrors = new ArrayList<>();

		/**
		 * Array of all the tokens that we have parsed from the CLI arguments.
		 */
		private List<Token> tokens;

		/**
		 * The index of the current token that we are parsing.
		 */
		private short currentTokenIndex = 0;

		private HashMap<Argument<?, ?>, Object> parsedArguments;

		HashMap<Argument<?, ?>, Object> getParsedArgumentsHashMap() {
			if (this.parsedArguments == null) {
				this.parsedArguments = new HashMap<>() {{
					Command.this.arguments.forEach(arg -> put(arg, arg.finishParsing()));
				}};
			}
			return this.parsedArguments;
		}

		public short getCurrentTokenIndex() {
			return currentTokenIndex;
		}

		void addError(ParseError.ParseErrorType type, Argument<?, ?> arg, int argValueCount, int currentIndex) {
			this.addError(new ParseError(type, currentIndex, arg, argValueCount));
		}

		void addError(ParseError.ParseErrorType type, Argument<?, ?> arg, int argValueCount) {
			this.addError(type, arg, argValueCount, this.currentTokenIndex);
		}


		void addError(CustomError customError) {
			this.customErrors.add(customError);
		}

		@Override
		public boolean hasExitErrors() {
			return super.hasExitErrors() || this.anyErrorInMinimum(this.customErrors, false);
		}

		@Override
		public boolean hasDisplayErrors() {
			return super.hasDisplayErrors() || this.anyErrorInMinimum(this.customErrors, true);
		}

		List<CustomError> getCustomErrors() {
			return this.getErrorsInLevelMinimum(this.customErrors, true);
		}
	}

	TokenizingState tokenizingState = this.new TokenizingState();
	ParsingState parsingState = this.new ParsingState();


	@Override
	public void resetState() {
		tokenizingState = this.new TokenizingState();
		parsingState = this.new ParsingState();
		this.arguments.forEach(Argument::resetState);
		this.argumentGroups.forEach(ArgumentGroup::resetState);

		this.subCommands.forEach(Command::resetState);
	}

	// ------------------------------------------------- Tokenization -------------------------------------------------

	void tokenize(String content) {
		this.tokenizingState.finishedTokenizing = false; // just in case we are tokenizing again for any reason

		final var TUPLE_CHARS = this.tupleChars.get().getCharPair();
		final var finalTokens = new ArrayList<Token>();
		final var currentValue = new StringBuilder();
		final char[] chars = content.toCharArray();

		final var values = new Object() {
			int i;
			char currentStringChar = 0;
			TokenizeError.TokenizeErrorType errorType = null;
		};

		final BiConsumer<TokenType, String> addToken = (t, c) -> finalTokens.add(new Token(t, c));

		final Runnable tokenizeSection = () -> {
			Token token = this.tokenizeSection(currentValue.toString());
			Command subCmd;
			// if this is a subcommand, continue tokenizing next elements
			if (token.type() == TokenType.SUB_COMMAND && (subCmd = getSubCommandByName(token.contents())) != null) {
				// forward the rest of stuff to the subCommand
				subCmd.tokenize(content.substring(values.i));
				this.tokenizingState.finishedTokenizing = true;
			} else {
				finalTokens.add(token);
			}
			currentValue.setLength(0);
		};

		BiPredicate<Integer, Character> charAtRelativeIndex = (index, character) -> {
			index += values.i;
			if (index >= chars.length || index < 0) return false;
			return chars[index] == character;
		};


		for (values.i = 0; values.i < chars.length && !this.tokenizingState.finishedTokenizing; values.i++) {
			char cChar = chars[values.i];

			// user is trying to escape a character
			if (cChar == '\\') {
				currentValue.append(chars[++values.i]); // skip the \ character and append the next character

				// reached a possible value wrapped in quotes
			} else if (cChar == '"' || cChar == '\'') {
				// if we are already in an open string, push the current value and close the string. Make sure
				// that the current char is the same as the one that opened the string
				if (this.tokenizingState.stringOpen && values.currentStringChar == cChar) {
					addToken.accept(TokenType.ARGUMENT_VALUE, currentValue.toString());
					currentValue.setLength(0);
					this.tokenizingState.stringOpen = false;

					// the string is open, but the character does not match. Push it as a normal character
				} else if (this.tokenizingState.stringOpen) {
					currentValue.append(cChar);

					// the string is not open, so open it and set the current string char to the current char
				} else {
					this.tokenizingState.stringOpen = true;
					values.currentStringChar = cChar;
				}

				// append characters to the current value as long as we are in a string
			} else if (this.tokenizingState.stringOpen) {
				currentValue.append(cChar);

				// reached a possible tuple start character
			} else if (cChar == TUPLE_CHARS.first()) {
				// if we are already in a tuple, set error and stop tokenizing
				if (this.tokenizingState.tupleOpen) {
					values.errorType = TokenizeError.TokenizeErrorType.TUPLE_ALREADY_OPEN;
					break;
				} else if (!currentValue.isEmpty()) { // if there was something before the tuple, tokenize it
					tokenizeSection.run();
				}

				// push the tuple token and set the state to tuple open
				addToken.accept(TokenType.ARGUMENT_VALUE_TUPLE_START, TUPLE_CHARS.first().toString());
				this.tokenizingState.tupleOpen = true;

				// reached a possible tuple end character
			} else if (cChar == TUPLE_CHARS.second()) {
				// if we are not in a tuple, set error and stop tokenizing
				if (!this.tokenizingState.tupleOpen) {
					values.errorType = TokenizeError.TokenizeErrorType.UNEXPECTED_TUPLE_CLOSE;
					break;
				}

				// if there was something before the tuple, tokenize it
				if (!currentValue.isEmpty()) {
					addToken.accept(TokenType.ARGUMENT_VALUE, currentValue.toString());
				}

				// push the tuple token and set the state to tuple closed
				addToken.accept(TokenType.ARGUMENT_VALUE_TUPLE_END, TUPLE_CHARS.second().toString());
				currentValue.setLength(0);
				this.tokenizingState.tupleOpen = false;

				// reached a "--". Push all the rest as a FORWARD_VALUE.
			} else if (cChar == '-' && charAtRelativeIndex.test(1, '-') && charAtRelativeIndex.test(2, ' ')) {
				addToken.accept(TokenType.FORWARD_VALUE, content.substring(values.i + 3));
				break;

				// reached a possible separator
			} else if (
				(cChar == ' ' && !currentValue.isEmpty()) // there's a space and some value to tokenize
					// also check if this is defining the value of an argument, or we are in a tuple. If so, don't tokenize
					|| (cChar == '=' && !tokenizingState.tupleOpen && this.isArgumentSpecifier(currentValue.toString()))
			)
			{
				tokenizeSection.run();

				// push the current char to the current value
			} else if (cChar != ' ') {
				currentValue.append(cChar);
			}
		}

		if (values.errorType == null)
			if (this.tokenizingState.tupleOpen) {
				values.errorType = TokenizeError.TokenizeErrorType.TUPLE_NOT_CLOSED;
			} else if (this.tokenizingState.stringOpen) {
				values.errorType = TokenizeError.TokenizeErrorType.STRING_NOT_CLOSED;
			}

		// we left something in the current value, tokenize it
		if (!currentValue.isEmpty()) {
			tokenizeSection.run();
		}

		if (values.errorType != null) {
			tokenizingState.addError(values.errorType, finalTokens.size());
		}

		parsingState.tokens = Collections.unmodifiableList(finalTokens);
		this.tokenizingState.finishedTokenizing = true;
	}

	private Token tokenizeSection(String str) {
		final TokenType type;

		if (this.tokenizingState.tupleOpen || this.tokenizingState.stringOpen) {
			type = TokenType.ARGUMENT_VALUE;
		} else if (this.isArgName(str)) {
			type = TokenType.ARGUMENT_NAME;
		} else if (this.isArgNameList(str)) {
			type = TokenType.ARGUMENT_NAME_LIST;
		} else if (this.isSubCommand(str)) {
			type = TokenType.SUB_COMMAND;
		} else {
			type = TokenType.ARGUMENT_VALUE;
		}

		return new Token(type, str);
	}

	List<Command> getTokenizedSubCommands() {
		final List<Command> x = new ArrayList<>();
		final Command subCmd;

		x.add(this);
		if ((subCmd = this.getTokenizedSubCommand()) != null) {
			x.addAll(subCmd.getTokenizedSubCommands());
		}
		return x;
	}

	private boolean isArgNameList(String str) {
		if (str.length() < 2) return false;

		final var possiblePrefixes = new ArrayList<Character>();
		final var charArray = str.substring(1).toCharArray();

		for (final char argName : charArray) {
			if (!runForArgument(argName, a -> possiblePrefixes.add(a.getPrefix())))
				break;
		}

		return possiblePrefixes.size() >= 1 && possiblePrefixes.contains(str.charAt(0));
	}

	private boolean isArgName(String str) {
		// first try to figure out if the prefix is used, to save time (does it start with '--'? (assuming the prefix is '-'))
		if (
			str.length() > 1 // make sure we are working with long enough strings
				&& str.charAt(0) == str.charAt(1) // first and second chars are equal?
		)
		{
			// now check if the name actually exist
			return this.arguments.stream().anyMatch(a -> a.checkMatch(str));
		}

		return false;
	}

	private boolean isArgumentSpecifier(String str) {
		return this.isArgName(str) || this.isArgNameList(str);
	}

	private boolean isSubCommand(String str) {
		return this.subCommands.stream().anyMatch(c -> c.name.equals(str));
	}

	private Command getSubCommandByName(String name) {
		var x = this.subCommands.stream().filter(sc -> sc.name.equals(name)).toList();
		return x.isEmpty() ? null : x.get(0);
	}

	private Command getTokenizedSubCommand() {
		return this.subCommands.stream().filter(sb -> sb.tokenizingState.finishedTokenizing).findFirst().orElse(null);
	}

	private Argument<?, ?> getArgumentByPositionalIndex(short index) {
		final var posArgs = this.getPositionalArguments();

		for (short i = 0; i < posArgs.size(); i++) {
			if (i == index) {
				return posArgs.get(i);
			}
		}
		return null;
	}

	// ---------------------------------------------------- Parsing ----------------------------------------------------

	void parseTokens() {
		short argumentNameCount = 0;
		boolean foundNonPositionalArg = false;
		Argument<?, ?> lastPosArgument; // this will never be null when being used

		for (parsingState.currentTokenIndex = 0; parsingState.currentTokenIndex < parsingState.tokens.size(); ) {
			final Token currentToken = parsingState.tokens.get(parsingState.currentTokenIndex);

			if (currentToken.type() == TokenType.ARGUMENT_NAME) {
				parsingState.currentTokenIndex++;
				runForArgument(currentToken.contents(), this::executeArgParse);
				foundNonPositionalArg = true;
			} else if (currentToken.type() == TokenType.ARGUMENT_NAME_LIST) {
				parseArgNameList(currentToken.contents().substring(1));
				foundNonPositionalArg = true;
			} else if (
				(currentToken.type() == TokenType.ARGUMENT_VALUE || currentToken.type() == TokenType.ARGUMENT_VALUE_TUPLE_START)
					&& !foundNonPositionalArg
					&& (lastPosArgument = getArgumentByPositionalIndex(argumentNameCount)) != null
			)
			{ // this is most likely a positional argument
				executeArgParse(lastPosArgument);
				argumentNameCount++;
			} else {
				parsingState.currentTokenIndex++;
				if (currentToken.type() != TokenType.FORWARD_VALUE)
					parsingState.addError(ParseError.ParseErrorType.UNMATCHED_TOKEN, null, 0);
			}
		}

		// now parse the subcommands
		this.subCommands.stream()
			.filter(sb -> sb.tokenizingState.finishedTokenizing) // only get the commands that were actually tokenized
			.forEach(Command::parseTokens); // now parse them
	}

	/**
	 * Reads the next tokens and parses them as values for the given argument.
	 * <p>
	 * This keeps in mind the type of the argument, and will stop reading tokens when it
	 * reaches the max number of values, or if the end of a tuple is reached.
	 * </p>
	 */
	private void executeArgParse(Argument<?, ?> arg) {
		final ArgValueCount argumentValuesRange = arg.argType.getNumberOfArgValues();

		// just skip the whole thing if it doesn't need any values
		if (argumentValuesRange.isZero()) {
			arg.parseValues();
			return;
		}

		final boolean isInTuple = (
			parsingState.currentTokenIndex < parsingState.tokens.size()
				&& parsingState.tokens.get(parsingState.currentTokenIndex).type() == TokenType.ARGUMENT_VALUE_TUPLE_START
		);

		final int ifTupleOffset = isInTuple ? 1 : 0;
		int skipCount = ifTupleOffset;

		final ArrayList<Token> tempArgs = new ArrayList<>();

		// add more values until we get to the max of the type, or we encounter another argument specifier
		for (
			int i = parsingState.currentTokenIndex + ifTupleOffset;
			i < parsingState.tokens.size();
			i++, skipCount++
		) {
			final Token currentToken = parsingState.tokens.get(i);
			if (
				(!isInTuple && (
					currentToken.type().isArgumentSpecifier() || i - parsingState.currentTokenIndex >= argumentValuesRange.max
				))
					|| currentToken.type().isTuple()
			)
			{
				break;
			}
			tempArgs.add(currentToken);
		}

		final int tempArgsSize = tempArgs.size();
		final int newCurrentTokenIndex = skipCount + ifTupleOffset;

		if (tempArgsSize > argumentValuesRange.max || tempArgsSize < argumentValuesRange.min) {
			parsingState.addError(ParseError.ParseErrorType.ARG_INCORRECT_VALUE_NUMBER, arg, tempArgsSize + ifTupleOffset);
			parsingState.currentTokenIndex += newCurrentTokenIndex;
			return;
		}

		// pass the arg values to the argument sub parser
		arg.parseValues(tempArgs.stream().map(Token::contents).toArray(String[]::new), (short)(parsingState.currentTokenIndex + ifTupleOffset));

		parsingState.currentTokenIndex += newCurrentTokenIndex;
	}

	/**
	 * Parses the given string as an argument value for the given argument.
	 * <p>
	 * If the value passed in is present (not empty or null), the argument should only require 0 or 1 values.
	 * </p>
	 */
	private void executeArgParse(Argument<?, ?> arg, String value) {
		final ArgValueCount argumentValuesRange = arg.argType.getNumberOfArgValues();

		if (value == null || value.isEmpty()) {
			this.executeArgParse(arg); // value is not present in the suffix of the argList. Continue parsing values.
			return;
		}

		// just skip the whole thing if it doesn't need any values
		if (argumentValuesRange.isZero()) {
			arg.parseValues();
			return;
		}

		if (argumentValuesRange.min > 1) {
			parsingState.addError(ParseError.ParseErrorType.ARG_INCORRECT_VALUE_NUMBER, arg, 0);
			return;
		}

		// pass the arg values to the argument subParser
		arg.parseValues(new String[] { value }, parsingState.currentTokenIndex);
	}


	/**
	 * Parses the given string as a list of single-char argument names.
	 */
	private void parseArgNameList(String args) {
		// its multiple of them. We can only do this with arguments that accept 0 values.
		for (short i = 0; i < args.length(); i++) {
			final short constIndex = i; // this is because the lambda requires the variable to be final

			if (!this.runForArgument(args.charAt(i), a -> {
				// if the argument accepts 0 values, then we can just parse it like normal
				if (a.argType.getNumberOfArgValues().isZero()) {
					this.executeArgParse(a);

					// -- arguments now may accept 1 or more values from now on:

					// if this argument is the last one in the list, then we can parse the next values after it
				} else if (constIndex == args.length() - 1) {
					parsingState.currentTokenIndex++;
					this.executeArgParse(a);

					// if this argument is not the last one in the list, then we can parse the rest of the chars as the value
				} else {
					this.executeArgParse(a, args.substring(constIndex + 1));
				}
			}))
				return;
		}
		parsingState.currentTokenIndex++;
	}

	/**
	 * Executes a callback for the argument found by the name specified.
	 *
	 * @return <a>ParseErrorType.ArgumentNotFound</a> if an argument was found
	 */
	private boolean runForArgument(String argName, Consumer<Argument<?, ?>> f) {
		for (final var argument : this.arguments) {
			if (argument.checkMatch(argName)) {
				f.accept(argument);
				return true;
			}
		}
		return false;
	}


	/**
	 * Executes a callback for the argument found by the name specified.
	 *
	 * @return <code>true</code> if an argument was found
	 */
	/* This method right here looks like it could be replaced by just changing it to
	 *    return this.runForArgument(String.valueOf(argName), f);
	 *
	 * It can't. "checkMatch" has also a char overload. The former would always return false.
	 * I don't really want to make "checkMatch" have different behavior depending on the length of the string, so
	 * an overload seems better. */
	private boolean runForArgument(char argName, Consumer<Argument<?, ?>> f) {
		for (final var argument : this.arguments) {
			if (argument.checkMatch(argName)) {
				f.accept(argument);
				return true;
			}
		}
		return false;
	}
}


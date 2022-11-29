package argparser;

import argparser.utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A command is a container for {@link Argument}s and other Sub{@link Command}s.
 */
public class Command extends ErrorsContainer<CustomError> implements IErrorCallbacks<Command, Command> {
	final String name, description;
	final ArrayList<Argument<?, ?>> arguments = new ArrayList<>();
	final ArrayList<Command> subCommands = new ArrayList<>();
	final ModifyRecord<Pair<Character, Character>> tupleChars = new ModifyRecord<>(TupleCharacter.SQUARE_BRACKETS.getCharPair());
	private final ModifyRecord<Integer> errorCode = new ModifyRecord<>(1);
	TokenizingState tokenizingState;
	ParsingState parsingState;
	private Consumer<Command> onErrorCallback;
	private Consumer<Command> onCorrectCallback;
	private boolean isRootCommand = false;
	private boolean finishedTokenizing = false;

	public Command(String name, String description) {
		if (!UtlString.matchCharacters(name, Character::isAlphabetic)) {
			throw new IllegalArgumentException("name must be alphabetic");
		}
		this.name = name;
		this.description = description;
		this.addArgument(new Argument<>("help", ArgumentType.BOOLEAN())
			.onOk(t -> System.out.println(this.getHelp()))
		);
	}

	public Command(String name) {
		this(name, null);
	}

	Command(String name, String description, boolean isRootCommand) {
		this(name, description);
		this.isRootCommand = isRootCommand;
	}

	/**
	 * Inserts an argument for this command to be parsed.
	 *
	 * @param argument the argument to be inserted
	 * @param <T> the ArgumentType subclass that will parse the value passed to the argument
	 * @param <TInner> the actual type of the value passed to the argument
	 */
	public <T extends ArgumentType<TInner>, TInner>
	void addArgument(Argument<T, TInner> argument) {
		if (this.arguments.stream().anyMatch(a -> a.equals(argument))) {
			throw new IllegalArgumentException("duplicate argument identifiers");
		}
		argument.setParentCmd(this);
		this.arguments.add(argument);
	}

	public void addSubCommand(Command cmd) {
		if (this.subCommands.stream().anyMatch(a -> a.name.equals(cmd.name))) {
			throw new IllegalArgumentException("cannot create two sub commands with the same name");
		}

		// pass some properties to the subcommand (most of the time this is what the user will want)
		cmd.tupleChars.setIfNotModified(this.tupleChars);
		cmd.getMinimumExitErrorLevel().setIfNotModified(this.getMinimumExitErrorLevel());
		cmd.getMinimumDisplayErrorLevel().setIfNotModified(this.getMinimumDisplayErrorLevel());
		cmd.errorCode.setIfNotModified(this.errorCode);
		this.subCommands.add(cmd);
	}

	public void addError(String message, ErrorLevel level) {
		this.addError(new CustomError(message, level));
	}

	public void setTupleChars(TupleCharacter tupleChars) {
		this.tupleChars.set(tupleChars.getCharPair());
	}

	public String getHelp() {
		return "This is the help of the program.";
	}

	public Argument<?, ?>[] getPositionalArguments() {
		return this.arguments.stream().filter(Argument::isPositional).toArray(Argument[]::new);
	}

	@Override
	public String toString() {
		return String.format(
			"Command[name='%s', description='%s', arguments=%s, subCommands=%s]",
			this.name, this.description, this.arguments, this.subCommands
		);
	}

	// ---------------------------------------------------- Parsing ----------------------------------------------------

	List<Command> getTokenizedSubCommands() {
		final List<Command> x = new ArrayList<>();
		final Command subCmd;

		x.add(this);
		if ((subCmd = this.getTokenizedSubCommand()) != null) {
			x.addAll(subCmd.getTokenizedSubCommands());
		}
		return x;
	}

	boolean isRootCommand() {
		return this.isRootCommand;
	}

	void tokenize(String content) {
		this.finishedTokenizing = false; // just in case we are tokenizing again for any reason

		final var finalTokens = new ArrayList<Token>();
		final var currentValue = new StringBuilder();
		TokenizeError.TokenizeErrorType errorType = null;

		final BiConsumer<TokenType, String> addToken = (t, c) -> finalTokens.add(new Token(t, c));
		final Consumer<Integer> tokenizeSection = (i) -> {
			Token token = this.tokenizeSection(currentValue.toString());
			Command subCmd;
			// if this is a subcommand, continue tokenizing next elements
			if (token.type() == TokenType.SUB_COMMAND && (subCmd = getSubCommandByName(token.contents())) != null) {
				// forward the rest of stuff to the subCommand
				subCmd.tokenize(content.substring(i + 1));
				this.finishedTokenizing = true; // dumb java lambdas require me to do this in order to stop tokenizing
			} else {
				finalTokens.add(token);
			}
			currentValue.setLength(0);
		};

		final char[] chars = content.toCharArray();

		for (int i = 0; i < chars.length && !finishedTokenizing; i++) {
			// reached a possible value wrapped in quotes
			if (chars[i] == '"' || chars[i] == '\'') {
				// if we are already in an opened string, push the current value and close the string
				if (this.tokenizingState.stringOpen) {
					addToken.accept(TokenType.ARGUMENT_VALUE, currentValue.toString());
					currentValue.setLength(0);
				}

				// if there's no value, start a new string
				if (currentValue.isEmpty() || this.tokenizingState.stringOpen) {
					this.tokenizingState.stringOpen = !this.tokenizingState.stringOpen;
				}

			// append characters to the current value as long as we are in a string
			} else if (this.tokenizingState.stringOpen) {
				currentValue.append(chars[i]);

			// reached a possible tuple start character
			} else if (chars[i] == tupleChars.get().first()) {
				// if we are already in a tuple, set error and stop tokenizing
				if (this.tokenizingState.tupleOpen) {
					errorType = TokenizeError.TokenizeErrorType.TUPLE_ALREADY_OPEN;
					break;
				} else if (!currentValue.isEmpty()) { // if there was something before the tuple, tokenize it
					tokenizeSection.accept(i);
				}

				// push the tuple token and set the state to tuple open
				addToken.accept(TokenType.ARGUMENT_VALUE_TUPLE_START, tupleChars.get().first().toString());
				this.tokenizingState.tupleOpen = true;

			// reached a possible tuple end character
			} else if (chars[i] == tupleChars.get().second()) {
				// if we are not in a tuple, set error and stop tokenizing
				if (!this.tokenizingState.tupleOpen) {
					errorType = TokenizeError.TokenizeErrorType.UNEXPECTED_TUPLE_CLOSE;
					break;
				}

				// if there was something before the tuple, tokenize it
				if (!currentValue.isEmpty()) {
					addToken.accept(TokenType.ARGUMENT_VALUE, currentValue.toString());
				}

				// push the tuple token and set the state to tuple closed
				addToken.accept(TokenType.ARGUMENT_VALUE_TUPLE_END, tupleChars.get().second().toString());
				currentValue.setLength(0);
				this.tokenizingState.tupleOpen = false;

			// reached the end of the whole input
			} else if (chars[i] != ' ' && i == chars.length - 1) {
				currentValue.append(chars[i]);
				tokenizeSection.accept(i);

			// user is trying to escape a character
			} else if (chars[i] == '\\') {
				i++; // skip the \ character
				currentValue.append(chars[i]); // append the next character

			// reached a possible separator
			} else if (
				(chars[i] == ' ' && !currentValue.isEmpty()) // there's a space and some value to tokenize
					// also check if this is defining the value of an argument, or we are in a tuple. If so, don't tokenize
					|| (chars[i] == '=' && !(tokenizingState.tupleOpen || this.isArgumentSpecifier(currentValue.substring(0, currentValue.length() - 1))))
			) {
				tokenizeSection.accept(i);

			// push the current char to the current value
			} else if (chars[i] != ' ') {
				currentValue.append(chars[i]);
			}
		}

		// we left something in the current value, tokenize it
		if (!currentValue.isEmpty()) {
			tokenizeSection.accept(chars.length);
		}

		if (errorType == null)
			if (this.tokenizingState.tupleOpen) {
				errorType = TokenizeError.TokenizeErrorType.TUPLE_NOT_CLOSED;
			} else if (this.tokenizingState.stringOpen) {
				errorType = TokenizeError.TokenizeErrorType.STRING_NOT_CLOSED;
			}

		if (errorType != null) {
			tokenizingState.addError(errorType, finalTokens.size());
		}

		parsingState.tokens = finalTokens.toArray(Token[]::new);
		finishedTokenizing = true;
	}

	private Token tokenizeSection(String str) {
		final TokenType type;

		if (this.tokenizingState.tupleOpen || this.tokenizingState.stringOpen) {
			type = TokenType.ARGUMENT_VALUE;
		} else if (this.isArgAlias(str)) {
			type = TokenType.ARGUMENT_ALIAS;
		} else if (this.isArgNameList(str)) {
			type = TokenType.ARGUMENT_NAME_LIST;
		} else if (this.isSubCommand(str)) {
			type = TokenType.SUB_COMMAND;
		} else {
			type = TokenType.ARGUMENT_VALUE;
		}

		return new Token(type, str);
	}

	private Argument<?, ?> getArgumentByPositionalIndex(short index) {
		final var posArgs = this.getPositionalArguments();

		for (short i = 0; i < posArgs.length; i++) {
			if (i == index) {
				return posArgs[i];
			}
		}
		return null;
	}

	private void parseArgNameList(String args) {
		// its multiple of them. We can only do this with arguments that accept 0 values.
		for (short i = 0; i < args.length(); i++) {
			final short constIndex = i; // this is because the lambda requires the variable to be final

			if (!this.runForArgument(args.charAt(i), a -> {
				if (a.getNumberOfValues().isZero()) {
					this.executeArgParse(a);
				} else if (constIndex == args.length() - 1) {
					parsingState.currentTokenIndex++;
					this.executeArgParse(a);
				} else {
					this.executeArgParse(a, args.substring(constIndex + 1)); // if this arg accepts more values, treat the rest of chars as value
				}
			}))
				return;
		}
		parsingState.currentTokenIndex++;
	}

	/**
	 * Executes a callback for the argument found by the alias specified.
	 *
	 * @return <a>ParseErrorType.ArgumentNotFound</a> if an argument was found
	 */
	private boolean runForArgument(String argAlias, Consumer<Argument<?, ?>> f) {
		for (final var argument : this.arguments) {
			if (argument.checkMatch(argAlias)) {
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
	private boolean runForArgument(char argName, Consumer<Argument<?, ?>> f) {
		for (final var argument : this.arguments) {
			if (argument.checkMatch(argName)) {
				f.accept(argument);
				return true;
			}
		}
		return false;
	}

	private boolean isArgAlias(String str) {
		// first try to figure out if the prefix is used, to save time (does it start with '--'? (assuming the prefix is '-'))
		if (
			str.length() > 1 // make sure we are working with long enough strings
				&& str.charAt(0) == str.charAt(1) // first and second chars are equal?
		) {
			// now check if the alias actually exist
			return this.arguments.stream().anyMatch(a -> a.checkMatch(str));
		}

		return false;
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

	private boolean isArgumentSpecifier(String str) {
		return this.isArgAlias(str) || this.isArgNameList(str);
	}

	private Command getSubCommandByName(String name) {
		var x = this.subCommands.stream().filter(sc -> sc.name.equals(name)).toList();
		return x.isEmpty() ? null : x.get(0);
	}

	private Command getTokenizedSubCommand() {
		return this.subCommands.stream().filter(sb -> sb.finishedTokenizing).findFirst().orElse(null);
	}

	private boolean isSubCommand(String str) {
		return this.subCommands.stream().anyMatch(c -> c.name.equals(str));
	}

	private void executeArgParse(Argument<?, ?> arg) {
		final ArgValueCount argumentValuesRange = arg.getNumberOfValues();

		// just skip the whole thing if it doesn't need any values
		if (argumentValuesRange.isZero()) {
			arg.parseValues();
			return;
		}

		final boolean isInTuple = (
			parsingState.currentTokenIndex < parsingState.tokens.length
				&& parsingState.tokens[parsingState.currentTokenIndex].type() == TokenType.ARGUMENT_VALUE_TUPLE_START
		);

		final int ifTupleOffset = isInTuple ? 1 : 0;
		int skipCount = ifTupleOffset;

		final ArrayList<Token> tempArgs = new ArrayList<>();

		// add more values until we get to the max of the type, or we encounter another argument specifier
		for (
			int i = parsingState.currentTokenIndex + ifTupleOffset;
			i < parsingState.tokens.length;
			i++, skipCount++
		) {
			final Token currentToken = parsingState.tokens[i];
			if (
				(!isInTuple && (
					currentToken.type().isArgumentSpecifier() || i - parsingState.currentTokenIndex >= argumentValuesRange.max
				))
					|| currentToken.type().isTuple()
			) {
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

	private void executeArgParse(Argument<?, ?> arg, String value) {
		final ArgValueCount argumentValuesRange = arg.getNumberOfValues();

		if (value.isEmpty()) {
			this.executeArgParse(arg); // value is not present in the suffix. Continue parsing values.
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
		arg.parseValues(new String[]{value}, parsingState.currentTokenIndex);
	}

	void parseTokens() {
		short argumentAliasCount = 0;
		boolean foundNonPositionalArg = false;
		Argument<?, ?> lastPosArgument; // this will never be null when being used

		for (parsingState.currentTokenIndex = 0; parsingState.currentTokenIndex < parsingState.tokens.length; ) {
			final Token currentToken = parsingState.tokens[parsingState.currentTokenIndex];

			if (currentToken.type() == TokenType.ARGUMENT_ALIAS) {
				parsingState.currentTokenIndex++;
				runForArgument(currentToken.contents(), this::executeArgParse);
				foundNonPositionalArg = true;
			} else if (currentToken.type() == TokenType.ARGUMENT_NAME_LIST) {
				parseArgNameList(currentToken.contents().substring(1));
				foundNonPositionalArg = true;
			} else if (
				(currentToken.type() == TokenType.ARGUMENT_VALUE || currentToken.type() == TokenType.ARGUMENT_VALUE_TUPLE_START)
					&& !foundNonPositionalArg
					&& (lastPosArgument = getArgumentByPositionalIndex(argumentAliasCount)) != null
			) { // this is most likely a positional argument
				executeArgParse(lastPosArgument);
				argumentAliasCount++;
			} else {
				parsingState.addError(ParseError.ParseErrorType.UNMATCHED_TOKEN, null, 0);
				parsingState.currentTokenIndex++;
			}
		}

		// now parse the subcommands
		this.subCommands.stream()
			.filter(sb -> sb.finishedTokenizing) // only get the commands that were actually tokenized
			.forEach(Command::parseTokens); // now parse them
	}

	/**
	 * Get all the tokens of all subcommands (the ones that we can get without errors)
	 * into one single list. This includes the SubCommand tokens.
	 */
	protected ArrayList<Token> getFullTokenList() {
		final ArrayList<Token> list = new ArrayList<>(Arrays.stream(this.parsingState.tokens).toList());
		final Command subCmd = this.getTokenizedSubCommand();

		if (subCmd != null) {
			list.addAll(subCmd.getFullTokenList());
		}

		return list;
	}

	private ArrayList<Token> getFullTokenList(ArrayList<Token> list) {
		list.add(new Token(TokenType.SUB_COMMAND, this.name));
		list.addAll(Arrays.stream(parsingState.tokens).toList());

		var subCmd = this.getTokenizedSubCommand();
		return subCmd == null ? list : subCmd.getFullTokenList(list);
	}

	void initParsingState() {
		tokenizingState = this.new TokenizingState();
		parsingState = this.new ParsingState();
		this.subCommands.forEach(Command::initParsingState);
	}

	public int getErrorCode() {
		int errCode = this.subCommands.stream()
			.map(sc -> sc.getMinimumExitErrorLevel().get().isInErrorMinimum(this.getMinimumExitErrorLevel().get()) ? sc.getErrorCode() : 0)
			.reduce(0, (a, b) -> a | b);

		/* If we have errors, or the subcommands had errors, do OR with our own error level.
		 * By doing this, the error code of a subcommand will be OR'd with the error codes of all its parents. */
		if (
			(this.parsingState.hasExitErrors() || this.tokenizingState.hasExitErrors())
				|| this.hasExitErrors()
				|| errCode != 0
		) {
			errCode |= this.errorCode.get();
		}

		return errCode;
	}

	ParsedArguments getParsedArguments() {
		return new ParsedArguments(
			this.name,
			this.getParsedArgumentsHashMap(),
			this.subCommands.stream().map(Command::getParsedArguments).toArray(ParsedArguments[]::new)
		);
	}

	private HashMap<Argument<?, ?>, Object> getParsedArgumentsHashMap() {
		return new HashMap<>() {{
			Command.this.arguments.forEach(arg -> this.put(arg, arg.finishParsing(Command.this.parsingState)));
		}};
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
		this.getParsedArgumentsHashMap().forEach(Argument::invokeCallbacks);
		this.subCommands.forEach(Command::invokeCallbacks);
	}

	@Override
	public boolean hasExitErrors() {
		return super.hasExitErrors()
			|| this.subCommands.stream().anyMatch(Command::hasExitErrors)
			|| this.arguments.stream().anyMatch(Argument::hasExitErrors)
			|| this.parsingState.hasExitErrors()
			|| this.tokenizingState.hasExitErrors();
	}

	@Override
	public boolean hasDisplayErrors() {
		return super.hasDisplayErrors()
			|| this.subCommands.stream().anyMatch(Command::hasDisplayErrors)
			|| this.arguments.stream().anyMatch(Argument::hasDisplayErrors)
			|| this.parsingState.hasDisplayErrors()
			|| this.tokenizingState.hasDisplayErrors();
	}

	private abstract class ParsingStateBase<T extends ErrorLevelProvider> extends ErrorsContainer<T> {
		public ParsingStateBase() {
			super(Command.this.getMinimumExitErrorLevel(), Command.this.getMinimumDisplayErrorLevel());
		}
	}

	class TokenizingState extends ParsingStateBase<TokenizeError> {
		public boolean tupleOpen = false;
		public boolean stringOpen = false;

		void addError(TokenizeError.TokenizeErrorType type, int index) {
			this.addError(new TokenizeError(type, index));
		}

	}

	class ParsingState extends ParsingStateBase<ParseError> {
		private final ArrayList<CustomError> customErrors = new ArrayList<>();

		/**
		 * Array of all the tokens that we have parsed from the CLI arguments.
		 */
		private Token[] tokens;

		/**
		 * The index of the current token that we are parsing.
		 */
		private short currentTokenIndex = 0;


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
}

package argparser;

import argparser.utils.Pair;
import argparser.utils.UtlString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class Command {
	protected final String name, description;
	protected final ArrayList<Argument<?, ?>> arguments = new ArrayList<>();
	protected final ArrayList<Command> subCommands = new ArrayList<>();
	protected Pair<Character, Character> tupleChars = TupleCharacter.SquareBrackets.getCharPair();

	public Command(String name, String description) {
		if (!UtlString.matchCharacters(name, Character::isAlphabetic)) {
			throw new IllegalArgumentException("name must be alphabetic");
		}
		this.name = name;
		this.description = description;
		this.addArgument(new Argument<>("help", ArgumentType.BOOLEAN())
			.callback(t -> System.out.println(this.getHelp()))
		);
	}

	public Command(String name) {
		this(name, null);
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
		this.arguments.add(argument);
	}

	public void addSubCommand(Command cmd) {
		if (this.subCommands.stream().anyMatch(a -> a.name.equals(cmd.name))) {
			throw new IllegalArgumentException("cannot create two sub commands with the same name");
		}

		// pass the current tuple chars to the subcommand (most of the time this is what the user will want)
		cmd.tupleChars = this.tupleChars;
		this.subCommands.add(cmd);
	}

	public String getHelp() {
		return "This is the help of the program.";
	}


	public Argument<?, ?>[] getPositionalArguments() {
		return this.arguments.stream().filter(Argument::isPositional).toArray(Argument[]::new);
	}

	public List<Command> getTokenizedSubCommands() {
		List<Command> x = new ArrayList<>();
		Command subCmd;
		x.add(this);
		if ((subCmd = this.getTokenizedSubCommand()) != null) {
			x.addAll(subCmd.getTokenizedSubCommands());
		}
		return x;
	}

	// ---------------------------------------------------- Parsing ----------------------------------------------------


	static class TokenizeState {
		public final ArrayList<ErrorHandler.TokenizeError> errors = new ArrayList<>();
		public boolean tupleOpen = false;
		public boolean stringOpen = false;

		void addError(TokenizeErrorType type, int index) {
			if (type == TokenizeErrorType.None) return;
			this.errors.add(new ErrorHandler.TokenizeError(type, index));
		}
	}

	TokenizeState tokenizeState;

	static class ParseState {
		public final ArrayList<ErrorHandler.ParseError> errors = new ArrayList<>();
		/**
		 * Array of all the tokens that we have parsed from the CLI arguments.
		 */
		private Token[] tokens;

		/**
		 * The index of the current token that we are parsing.
		 */
		private short currentTokenIndex = 0;


		void addError(ParseErrorType type, Argument<?, ?> arg, int argValueCount, int currentIndex) {
			if (type == ParseErrorType.None) return;
			this.errors.add(new ErrorHandler.ParseError(type, currentIndex, arg, argValueCount));
		}

		void addError(ParseErrorType type, Argument<?, ?> arg, int argValueCount) {
			this.addError(type, arg, argValueCount, this.currentTokenIndex);
		}
	}

	ParseState parseState;

	private boolean finishedTokenizing = false;


	void tokenize(String content) {
		this.finishedTokenizing = false; // just in case we are tokenizing again for any reason

		var finalTokens = new ArrayList<Token>();
		var currentValue = new StringBuilder();
		TokenizeErrorType errorType = TokenizeErrorType.None;

		BiConsumer<TokenType, String> addToken = (t, c) -> finalTokens.add(new Token(t, c));
		Consumer<Integer> tokenizeSection = (i) -> {
			Token token = this.tokenizeSection(currentValue.toString());
			Command subCmd;
			// if this is a subcommand, continue tokenizing next elements
			if (token.type() == TokenType.SubCommand && (subCmd = getSubCommandByName(token.contents())) != null) {
				// forward the rest of stuff to the subCommand
				subCmd.tokenize(content.substring(i + 1));
				finishedTokenizing = true; // dumb java lambdas require me to do this in order to stop tokenizing
			} else {
				finalTokens.add(token);
				currentValue.setLength(0);
			}
		};

		char[] chars = content.toCharArray();

		for (int i = 0; i < chars.length && !finishedTokenizing; i++) {
			if (chars[i] == '"' || chars[i] == '\'') {
				if (this.tokenizeState.stringOpen) {
					addToken.accept(TokenType.ArgumentValue, currentValue.toString());
					currentValue.setLength(0);
				} else if (!currentValue.isEmpty()) { // maybe a possible argNameList? tokenize it
					tokenizeSection.accept(i);
				}
				this.tokenizeState.stringOpen = !this.tokenizeState.stringOpen;
			} else if (chars[i] == tupleChars.first() && !this.tokenizeState.stringOpen) {
				if (this.tokenizeState.tupleOpen) {
					errorType = TokenizeErrorType.TupleAlreadyOpen;
					break;
				} else if (!currentValue.isEmpty()) {
					tokenizeSection.accept(i);
				}
				addToken.accept(TokenType.ArgumentValueTupleStart, tupleChars.first().toString());
				this.tokenizeState.tupleOpen = true;
			} else if (chars[i] == tupleChars.second() && !this.tokenizeState.stringOpen) {
				if (!this.tokenizeState.tupleOpen) {
					errorType = TokenizeErrorType.UnexpectedTupleClose;
					break;
				}
				if (!currentValue.isEmpty()) {
					addToken.accept(TokenType.ArgumentValue, currentValue.toString());
				}
				addToken.accept(TokenType.ArgumentValueTupleEnd, tupleChars.second().toString());
				currentValue.setLength(0);
				this.tokenizeState.tupleOpen = false;
			} else if (chars[i] != ' ' && i == chars.length - 1) {
				currentValue.append(chars[i]);
				tokenizeSection.accept(i);
			} else if (this.tokenizeState.stringOpen) {
				if (chars[i] == '\\') i++; // user is trying to escape a character
				currentValue.append(chars[i]);
			} else if ((chars[i] == ' ' || chars[i] == '=') && !currentValue.isEmpty()) {
				tokenizeSection.accept(i);
			} else if (chars[i] != ' ') {
				currentValue.append(chars[i]);
			}
		}

		if (this.tokenizeState.tupleOpen) {
			errorType = TokenizeErrorType.TupleNotClosed;
		} else if (this.tokenizeState.stringOpen) {
			errorType = TokenizeErrorType.StringNotClosed;
		}

		tokenizeState.addError(errorType, finalTokens.size());

		parseState.tokens = finalTokens.toArray(Token[]::new);
		finishedTokenizing = true;
	}


	private Token tokenizeSection(String str) {
		TokenType type;

		if (this.tokenizeState.tupleOpen || this.tokenizeState.stringOpen) {
			type = TokenType.ArgumentValue;
		} else if (this.isArgAlias(str)) {
			type = TokenType.ArgumentAlias;
		} else if (this.isArgNameList(str)) {
			type = TokenType.ArgumentNameList;
		} else if (this.isSubCommand(str)) {
			type = TokenType.SubCommand;
		} else {
			type = TokenType.ArgumentValue;
		}

		return new Token(type, str);
	}

	private Argument<?, ?> getArgumentByPositionalIndex(short index) {
		var posArgs = this.getPositionalArguments();

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
			char currentSimpleArg = args.charAt(i);
			short constIndex = i; // this is because the lambda requires the variable to be final

			if (!this.runForArgument(currentSimpleArg, a -> {
				if (a.getNumberOfValues().isZero()) {
					this.executeArgParse(a);
				} else if (constIndex == args.length() - 1) {
					parseState.currentTokenIndex++;
					this.executeArgParse(a);
				} else {
					this.executeArgParse(a, args.substring(constIndex + 1)); // if this arg accepts more values, treat the rest of chars as value
				}
			}))
				break;
		}
	}

	/**
	 * Executes a callback for the argument found by the alias specified.
	 *
	 * @return <a>ParseErrorType.ArgumentNotFound</a> if an argument was found
	 */
	private boolean runForArgument(String argAlias, Consumer<Argument<?, ?>> f) {
		for (var argument : this.arguments) {
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
		for (var argument : this.arguments) {
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

	private Command getSubCommandByName(String name) {
		var x = this.subCommands.stream().filter(sc -> sc.name.equals(name)).toList();
		return x.isEmpty() ? null : x.get(0);
	}

	private Command getTokenizedSubCommand() {
		return this.subCommands.stream().filter(sb -> sb.finishedTokenizing).findFirst().orElse(null);
	}

	private boolean isArgNameList(String str) {
		if (str.length() < 2) return false;

		var possiblePrefixes = new ArrayList<Character>();
		var charArray = str.substring(1).toCharArray();

		for (char argName : charArray) {
			if (!runForArgument(argName, a -> possiblePrefixes.add(a.getPrefix())))
				break;
		}

		return possiblePrefixes.size() >= 1 && possiblePrefixes.contains(str.charAt(0));
	}

	private boolean isSubCommand(String str) {
		return this.subCommands.stream().anyMatch(c -> c.name.equals(str));
	}

	private void executeArgParse(Argument<?, ?> arg) {
		ArgValueCount argumentValuesRange = arg.getNumberOfValues();

		// just skip the whole thing if it doesn't need any values
		if (argumentValuesRange.isZero()) {
			arg.parseValues(new String[]{});
			return;
		}

		boolean isInTuple = (
			parseState.currentTokenIndex < parseState.tokens.length
				&& parseState.tokens[parseState.currentTokenIndex].type() == TokenType.ArgumentValueTupleStart
		);
		Function<Integer, Integer> ifInTuple = v -> isInTuple ? v : 0;

		int skipCount = ifInTuple.apply(1);

		// first_capture_the_minimum_required_values...
		ArrayList<Token> tempArgs = new ArrayList<>();

		// next add more values until we get to the max of the type, or we encounter another argument specifier
		for (
			int i = parseState.currentTokenIndex + ifInTuple.apply(1);
			i < parseState.tokens.length;
			i++, skipCount++
		) {
			Token currentToken = parseState.tokens[i];
			if (
				(!isInTuple && (
					currentToken.type().isArgumentSpecifier() || i - parseState.currentTokenIndex >= argumentValuesRange.max
				))
					|| currentToken.type().isTuple()
			) {
				break;
			}
			tempArgs.add(currentToken);
		}

		int tempArgsSize = tempArgs.size();
		int newCurrentTokenIndex = skipCount + ifInTuple.apply(1);

		if (tempArgsSize > argumentValuesRange.max || tempArgsSize < argumentValuesRange.min) {
			parseState.addError(ParseErrorType.ArgIncorrectValueNumber, arg, tempArgsSize + ifInTuple.apply(1));
			parseState.currentTokenIndex += newCurrentTokenIndex;
			return;
		}

		parseState.currentTokenIndex += newCurrentTokenIndex;

		// pass the arg values to the argument subparser
		arg.parseValues(tempArgs.stream().map(Token::contents).toArray(String[]::new));
	}

	private void executeArgParse(Argument<?, ?> arg, String value) {
		ArgValueCount argumentValuesRange = arg.getNumberOfValues();

		if (value.isEmpty()) {
			this.executeArgParse(arg); // value is not present in the suffix. Continue parsing values.
			return;
		}

		// just skip the whole thing if it doesn't need any values
		if (argumentValuesRange.isZero()) {
			arg.parseValues(new String[]{});
			return;
		}

		if (argumentValuesRange.min > 1) {
			parseState.addError(ParseErrorType.ArgIncorrectValueNumber, arg, 0);
			return;
		}

		// pass the arg values to the argument subparser
		arg.parseValues(new String[]{value});
	}

	ParsedArguments parseTokens() {
		short argumentAliasCount = 0;
		boolean foundNonPositionalArg = false;
		Argument<?, ?> lastPosArgument; // this will never be null when being used

		for (parseState.currentTokenIndex = 0; parseState.currentTokenIndex < parseState.tokens.length; ) {
			Token c_token = parseState.tokens[parseState.currentTokenIndex];

			if (c_token.type() == TokenType.ArgumentAlias) {
				parseState.currentTokenIndex++;
				runForArgument(c_token.contents(), this::executeArgParse);
				foundNonPositionalArg = true;
			} else if (c_token.type() == TokenType.ArgumentNameList) {
				parseArgNameList(c_token.contents().substring(1));
				foundNonPositionalArg = true;
				parseState.currentTokenIndex++;
			} else if (
				(c_token.type() == TokenType.ArgumentValue || c_token.type() == TokenType.ArgumentValueTupleStart)
					&& !foundNonPositionalArg
					&& (lastPosArgument = getArgumentByPositionalIndex(argumentAliasCount)) != null
			) { // this is most likely a positional argument
				executeArgParse(lastPosArgument);
				argumentAliasCount++;
			} else {
				parseState.addError(ParseErrorType.UnmatchedToken, null, 0);
				parseState.currentTokenIndex++;
			}
		}

		HashMap<String, Object> parsedArgs = new HashMap<>();

		this.arguments.forEach(argument -> {
			ParseResult<?> r = argument.finishParsing();
			if (!r.isCorrect()) {
				parseState.addError(r.reason, argument, 0);
				return;
			}
			if (r.returnValue == null) return;
			parsedArgs.put(argument.getAlias(), r.unpack());
		});

		// now parse the subcommands
		return this.subCommands.stream()
			.filter(sb -> sb.finishedTokenizing) // only get the commands that were actually tokenized
			.map(Command::parseTokens) // now parse them
			.findFirst() // we should only have one because you can't use more than one subcommand
			.map(parsedArguments -> new ParsedArguments(parsedArgs, parsedArguments, this.name))
			.orElseGet(() -> new ParsedArguments(parsedArgs, null, this.name));
	}

	/**
	 * Get all the tokens of all subcommands (the ones that we can get without errors)
	 * into one single list. This includes the SubCommand tokens.
	 */
	protected ArrayList<Token> getFullTokenList() {
		ArrayList<Token> list = new ArrayList<>(Arrays.stream(parseState.tokens).toList());

		var subCmd = this.getTokenizedSubCommand();
		return subCmd == null ? list : subCmd.getFullTokenList(list);
	}

	private ArrayList<Token> getFullTokenList(ArrayList<Token> list) {
		list.add(new Token(TokenType.SubCommand, this.name));
		list.addAll(Arrays.stream(parseState.tokens).toList());

		var subCmd = this.getTokenizedSubCommand();
		return subCmd == null ? list : subCmd.getFullTokenList(list);
	}

	void initParsingState() {
		tokenizeState = new TokenizeState();
		parseState = new ParseState();
		this.subCommands.forEach(Command::initParsingState);
	}
}

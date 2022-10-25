package argparser;

import argparser.ParserState.ParseErrorType;
import argparser.ParserState.ParseResult;
import argparser.utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class Command {
	protected final String name, description;
	protected ArrayList<Argument<?, ?>> arguments = new ArrayList<>();
	protected ArrayList<Command> subCommands = new ArrayList<>();
	protected final Pair<Character, Character> tupleChars = TupleCharacter.SquareBrackets.getCharPair();

	public Command(String name, String description) {
		this.name = name;
		this.description = description;
	}

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
		this.subCommands.add(cmd);
	}

	public String getHelp() {
		return "This is the help of the program.";
	}

	public ArrayList<Argument<?, ?>> getArguments() {
		return arguments;
	}

	public Argument<?, ?>[] getPositionalArguments() {
		return this.arguments.stream().filter(Argument::isPositional).toArray(Argument[]::new);
	}

	public Command[] getSubCommands() {
		return subCommands.toArray(Command[]::new);
	}

	/**
	 * Array of all the tokens that we have parsed from the CLI arguments.
	 */
	private Token[] tokens;

	/**
	 * The index of the current token that we are parsing.
	 */
	private short currentTokenIndex = 0;
	private boolean finishedTokenizing = false;


	public void debugShit() {
		System.out.println(this.name);
		if (this.finishedTokenizing)
			for (var t : this.tokens) {
				System.out.println("\t" + t);
			}
		else
			System.out.println("\tim null");
		if (!this.subCommands.isEmpty()) {
			this.subCommands.forEach(Command::debugShit);
		}
	}


	protected ParseResult<Token[]> tokenize(String content) {
		var result = new ArrayList<Token>();
		var currentValue = new StringBuilder();


		BiConsumer<TokenType, String> addToken = (t, c) -> result.add(new Token(t, c));
		Consumer<Integer> tokenizeSection = (i) -> {
			var token = this.tokenizeSection(currentValue.toString());
			Command subCmd;
			// if this is a subcommand, continue tokenizing next elements
			if (token.type() == TokenType.SubCommand && (subCmd = getSubCommandByName(token.contents())) != null) {
				// forward the rest of stuff to the subCommand
				subCmd.tokenize(content.substring(i + 1));
				finishedTokenizing = true; // dumb java lambdas require me to do this in order to stop tokenizing
			} else {
				result.add(token);
				currentValue.setLength(0);
			}
		};

		boolean stringOpen = false;
		boolean tupleOpen = false;

		char[] chars = content.toCharArray();

		for (int i = 0; i < chars.length && !finishedTokenizing; i++) {
			if (chars[i] == '"' || chars[i] == '\'') {
				if (stringOpen) {
					addToken.accept(TokenType.ArgumentValue, currentValue.toString());
					currentValue.setLength(0);
				} else if (!currentValue.isEmpty()) { // maybe a possible argNameList? tokenize it
					tokenizeSection.accept(i);
				}
				stringOpen = !stringOpen;
			} else if (chars[i] == tupleChars.first() && !stringOpen) {
				if (tupleOpen) {
					// TODO: add proper errors
					return ParseResult.ERROR(ParseErrorType.ArgNameListTakeValues);
				} else if (!currentValue.isEmpty()) {
					tokenizeSection.accept(i);
				}
				addToken.accept(TokenType.ArgumentValueTupleStart, null);
				tupleOpen = true;
			} else if (chars[i] == tupleChars.second() && !stringOpen) {
				if (!tupleOpen) {
					// TODO: add proper errors
					return ParseResult.ERROR(ParseErrorType.ArgNameListTakeValues);
				}
				if (!currentValue.isEmpty()) {
					addToken.accept(TokenType.ArgumentValue, currentValue.toString());
				}
				addToken.accept(TokenType.ArgumentValueTupleEnd, null);
				currentValue.setLength(0);
				tupleOpen = false;
			} else if (chars[i] != ' ' && i == chars.length - 1) {
				if (tupleOpen) {
					// TODO: add proper errors
					return ParseResult.ERROR(ParseErrorType.ArgNameListTakeValues);
				}
				if (stringOpen) {
					// TODO: add proper errors
					return ParseResult.ERROR(ParseErrorType.ArgNameListTakeValues);
				}
				currentValue.append(chars[i]);
				tokenizeSection.accept(i);
			} else if (stringOpen) {
				if (chars[i] == '\\') i++; // user is trying to escape a character
				currentValue.append(chars[i]);
			} else if ((chars[i] == ' ' || chars[i] == '=') && !currentValue.isEmpty()) {
				tokenizeSection.accept(i);
			} else if (chars[i] != ' ') {
				currentValue.append(chars[i]);
			}
		}

		this.tokens = result.toArray(Token[]::new);
		finishedTokenizing = true;
		return ParseResult.CORRECT();
	}


	private Token tokenizeSection(String str) {
		TokenType type;

		if (this.isArgAlias(str)) {
			type = TokenType.ArgumentAlias;
		} else if (this.isArgNames(str)) {
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


	private ParseResult<Void> parseArgNameList(String args) {
		// if its only one, we can parse the arg without problem
		if (args.length() == 1) {
			currentTokenIndex++;
			return runForArgument(args.charAt(0), this::executeArgParse);
		}

		var res_group = new ParseResult<Void>();

		// its multiple of them. We can only do this with arguments that accept 0 values.
		for (short i = 0; i < args.length(); i++) {
			char current_simple_arg = args.charAt(i);
			short const_index = i; // this is because the lambda requires the variable to be final

			var res = this.runForArgument(current_simple_arg, a -> {
				if (a.getNumberOfValues().isZero()) {
					return this.executeArgParse(a);
				} else if (const_index == args.length() - 1) {
					currentTokenIndex++;
					return this.executeArgParse(a);
				}
				return this.executeArgParse(a, args.substring(const_index + 1)); // if this arg accepts more values, treat the rest of chars as value
			});

			if (res.getReason() != ParseErrorType.ArgNameListTakeValues) {
				res_group.addSubResult(res);
			} else {
				break;
			}
		}

		// only return a correct result if all sub results are ok
		return res_group.correctByAll();
	}

	/**
	 * Executes a callback for the argument found by the alias specified.
	 *
	 * @return <code>true</code> if an argument was found
	 */
	private ParseResult<Void> runForArgument(String argAlias, Function<Argument<?, ?>, ParseResult<Void>> f) {
		for (var argument : this.getArguments()) {
			if (argument.checkMatch(argAlias)) {
				return f.apply(argument);
			}
		}
		return ParseResult.ERROR(ParseErrorType.ArgumentNotFound);
	}

	/**
	 * Executes a callback for the argument found by the name specified.
	 *
	 * @return <code>true</code> if an argument was found
	 */
	private ParseResult<Void> runForArgument(char argName, Function<Argument<?, ?>, ParseResult<Void>> f) {
		for (var argument : this.getArguments()) {
			if (argument.checkMatch(argName)) {
				return f.apply(argument);
			}
		}
		return ParseResult.ERROR(ParseErrorType.ArgumentNotFound);
	}


	private boolean isArgAlias(String str) {
		// first try to figure out if the prefix is used, to save time (does it start with '--'? (assuming the prefix is '-'))
		if (
			str.length() > 1 // make sure we are working with long enough strings
				&& str.charAt(0) == str.charAt(1) // first and second chars are equal?
		) {
			// now check if the alias actually exist
			return this.getArguments().stream().anyMatch(a -> a.checkMatch(str));
		}

		return false;
	}

	private Command getSubCommandByName(String name) {
		var x = Arrays.stream(this.getSubCommands()).filter(sc -> sc.name.equals(name)).toList();
		return x.isEmpty() ? null : x.get(0);
	}

	private boolean isArgNames(String str) {
		// TODO: This is not the proper way of doing this
		for (var character : str.substring(1).toCharArray()) {
			return this.getArguments().stream().anyMatch(a -> a.checkMatch(character));
		}

		return false;
	}

	private boolean isSubCommand(String str) {
		return Arrays.stream(this.getSubCommands()).anyMatch(c -> c.name.equals(str));
	}

	private ParseResult<Void> executeArgParse(Argument<?, ?> arg) {
		ArgValueCount argumentValuesRange = arg.getNumberOfValues();

		// just skip the whole thing if it doesn't need any values
		if (argumentValuesRange.isZero()) {
			arg.parseValues(new String[]{});
			return ParseResult.CORRECT();
		}

		boolean isInTuple = (
			currentTokenIndex < this.tokens.length
				&& this.tokens[currentTokenIndex].type() == TokenType.ArgumentValueTupleStart
		);
		Function<Integer, Integer> ifInTuple = v -> isInTuple ? v : 0;

		int skipCount = ifInTuple.apply(1);

		// first capture the minimum required values...
		ArrayList<Token> temp_args = new ArrayList<>();

		// next add more values until we get to the max of the type, or we encounter another argument specifier
		for (
			int i = currentTokenIndex + ifInTuple.apply(1);
			i < this.tokens.length;
			i++, skipCount++
		) {
			var actual_token = this.tokens[i];
			if (
				(!isInTuple && (
					actual_token.isArgumentSpecifier() || i - currentTokenIndex >= argumentValuesRange.max
				))
					|| actual_token.type() == TokenType.ArgumentValueTupleEnd
			) {
				break;
			}
			temp_args.add(actual_token);
		}

		int temp_args_size = temp_args.size();

		this.currentTokenIndex += skipCount + ifInTuple.apply(1);

		if (temp_args_size > argumentValuesRange.max || temp_args_size < argumentValuesRange.min)
			return ParseResult.ERROR(ParseErrorType.ArgIncorrectValueNumber, temp_args_size);

		// pass the arg values to the argument subparser
		arg.parseValues(temp_args.stream().map(Token::contents).toArray(String[]::new));


		return ParseResult.CORRECT();
	}

	private ParseResult<Void> executeArgParse(Argument<?, ?> arg, String value) {
		ArgValueCount argumentValuesRange = arg.getNumberOfValues();

		if (value.length() == 0) {
			return this.executeArgParse(arg); // value is not present in the suffix. Continue parsing values.
		}

		// just skip the whole thing if it doesn't need any values
		if (argumentValuesRange.isZero()) {
			arg.parseValues(new String[]{});
			return ParseResult.CORRECT();
		}

		if (argumentValuesRange.max > 1) {
			return ParseResult.ERROR(ParseErrorType.ArgIncorrectValueNumber);
		}

		// pass the arg values to the argument subparser
		arg.parseValues(new String[]{value});

		return ParseResult.CORRECT();
	}

	protected HashMap<String, Object> parse() {
		short argumentAliasCount = 0;
		boolean foundNonPositionalArg = false;
		HashMap<String, Object> parsed_args = new HashMap<>();
		Argument<?, ?> last_pos_argument; // this will never be null when being used

		for (this.currentTokenIndex = 0; this.currentTokenIndex < tokens.length; ) {
			Token c_token = this.tokens[currentTokenIndex];

			if (c_token.type() == TokenType.ArgumentAlias) {
				currentTokenIndex++;
				if (!runForArgument(c_token.contents(), this::executeArgParse).isCorrect()) {
					System.out.println("FUCK");
				}
				foundNonPositionalArg = true;
			} else if (c_token.type() == TokenType.ArgumentNameList) {
				parseArgNameList(c_token.contents().substring(1));
				foundNonPositionalArg = true;
				currentTokenIndex++;
			} else if (
				(c_token.type() == TokenType.ArgumentValue || c_token.type() == TokenType.ArgumentValueTupleStart)
					&& !foundNonPositionalArg
					&& (last_pos_argument = getArgumentByPositionalIndex(argumentAliasCount)) != null
			) { // this is most likely a positional argument
				if (!executeArgParse(last_pos_argument).isCorrect()) {
					System.out.println("FUCK 2: ");
				}
				argumentAliasCount++;
			} else {
				System.out.println("PARSE: Unmatched token " + c_token.type() + ": " + c_token.contents());
				currentTokenIndex++;
			}
		}

		this.getArguments().forEach(argument -> {
			var result = argument.finishParsing();
			if (!result.isCorrect()) {
				System.out.println("error with argument " + argument.getAlias());
				return;
			}
			parsed_args.put(argument.getAlias(), result.unpack());
		});

		this.subCommands.forEach(sb -> {if (sb.tokens != null) sb.parse();});


		return parsed_args;
	}
}
package argparser;

import argparser.exceptions.Result;
import argparser.utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * An instance of this class represents the state of the parser while parsing the passed arguments.
 */
class ParserState {
	private final char[] cliArgs;
	private final ArrayList<Argument<?, ?>> specifiedArguments;

	/**
	 * The array that contains the arguments that are positional.
	 * This exists for later ease with looking for positional arguments.
	 */
	private final Argument<?, ?>[] positionalArguments;

	/**
	 * The characters that will represent the start and end of a tuple when tokenizing.
	 */
	private final Pair<Character, Character> tupleChars;

	/**
	 * All the possible argument prefixes that we can encounter.
	 */
	private final List<Character> possiblePrefixes;

	/**
	 * Array of all the tokens that we have parsed from the CLI arguments.
	 */
	private Token[] tokens;

	/**
	 * The index of the current token that we are parsing.
	 */
	private short currentTokenIndex = 0;

	enum ParseErrorType {
		ArgumentNotFound,
		ArgNameListTakeValues,
		ObligatoryArgumentNotUsed,
		ArgIncorrectValueNumber
	}

	static class ParseResult<TReturn> extends Result<ParseErrorType, TReturn> {
		public ParseResult(boolean isCorrect, int pos, ParseErrorType reason) {super(isCorrect, pos, reason);}

		public ParseResult() {}

		public ParseResult<TReturn> correctByAny() {
			return (ParseResult<TReturn>)super.correctByAny();
		}

		public ParseResult<TReturn> correctByAll() {
			return (ParseResult<TReturn>)super.correctByAll();
		}

		public void addSubResult(ParseResult<TReturn> r) {
			super.addSubResult(r);
		}

		public static <TReturn> ParseResult<TReturn> CORRECT() {return new ParseResult<>(true, 0, null);}

		public static <TReturn> ParseResult<TReturn> CORRECT(TReturn ret) {
			var x = new ParseResult<TReturn>(true, 0, null);
			x.returnValue = ret;
			return x;
		}

		public static <TReturn> ParseResult<TReturn> ERROR(ParseErrorType reason) {
			return new ParseResult<>(false, 0, reason);
		}

		public static <TReturn> ParseResult<TReturn> ERROR(ParseErrorType reason, int value) {
			return new ParseResult<>(false, value, reason);
		}
	}

	public ParserState(String cliArgs, ArrayList<Argument<?, ?>> u_args, TupleCharacter tc) {
		this.cliArgs = cliArgs.toCharArray();
		this.tupleChars = tc.getCharPair();
		this.specifiedArguments = u_args;
		this.possiblePrefixes = u_args.stream().map(Argument::getPrefix).distinct().toList();
		this.positionalArguments = u_args.stream().filter(Argument::isPositional).toArray(Argument[]::new);
	}

	public HashMap<String, Object> parse() throws Exception {
		this.tokens = this.tokenize().unpack();

		Arrays.stream(this.tokens).toList().forEach(System.out::println);

		short argumentAliasCount = 0;
		boolean foundNonPositionalArg = false;
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

		HashMap<String, Object> parsed_args = new HashMap<>();

		this.specifiedArguments.forEach(argument -> {
			var result = argument.finishParsing();
			if (!result.isCorrect()) {
				System.out.println("error with argument " + argument.getAlias());
				return;
			}
			parsed_args.put(argument.getAlias(), result.unpack());
		});

		return parsed_args;
	}

	private Argument<?, ?> getArgumentByPositionalIndex(short index) {
		for (short i = 0; i < this.positionalArguments.length; i++) {
			if (i == index) {
				return this.positionalArguments[i];
			}
		}
		return null;
	}

	private boolean isPossiblePrefix(String str) {
		return this.possiblePrefixes.contains(str.charAt(0));
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

		// only return a correct result if all subresults are ok
		return res_group.correctByAll();
	}

	/**
	 * Executes a callback for the argument found by the alias specified.
	 *
	 * @return <code>true</code> if an argument was found
	 */
	private ParseResult<Void> runForArgument(String argAlias, Function<Argument<?, ?>, ParseResult<Void>> f) {
		for (var argument : this.specifiedArguments) {
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
		for (var argument : this.specifiedArguments) {
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
				&& this.isPossiblePrefix(str) // okay lets check if the prefix is valid
		) {
			// now check if the alias actually exist
			return this.specifiedArguments.stream().anyMatch(a -> a.checkMatch(str));
		}

		return false;
	}

	private boolean isArgNames(String str) {
		if (!this.isPossiblePrefix(str)) {
			return false;
		}

		// TODO: This is not the proper way of doing this
		for (var character : str.substring(1).toCharArray()) {
			return this.specifiedArguments.stream().anyMatch(a -> a.checkMatch(character));
		}

		return false;
	}

	private ParseResult<Void> executeArgParse(Argument<?, ?> arg) {
		ArgValueCount argumentValuesRange = arg.getNumberOfValues();

		// just skip the whole thing if it doesn't need any values
		if (argumentValuesRange.isZero()) {
			arg.parseValues(new String[]{});
			return ParseResult.CORRECT();
		}

		boolean isInTuple = this.tokens[currentTokenIndex].type() == TokenType.ArgumentValueTupleStart;
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

	private ParseResult<Token[]> tokenize() throws Exception {
		var result = new ArrayList<Token>();
		final var currentValue = new StringBuilder();
		BiConsumer<TokenType, String> addToken = (t, c) -> result.add(new Token(t, c));
		Runnable tokenizeSection = () -> {
			result.add(this.tokenizeSection(currentValue.toString()));
			currentValue.setLength(0);
		};

		boolean stringOpen = false;
		boolean tupleOpen = false;

		char[] chars = this.cliArgs;

		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '"' || chars[i] == '\'') {
				if (stringOpen) {
					addToken.accept(TokenType.ArgumentValue, currentValue.toString());
					currentValue.setLength(0);
				} else if (!currentValue.isEmpty()) { // maybe a possible argNameList? tokenize it
					tokenizeSection.run();
				}
				stringOpen = !stringOpen;
			} else if (chars[i] == tupleChars.first() && !stringOpen) {
				if (tupleOpen) {
					throw new Exception("Tuple is already open");
				} else if (!currentValue.isEmpty()) {
					tokenizeSection.run();
				}
				addToken.accept(TokenType.ArgumentValueTupleStart, null);
				tupleOpen = true;
			} else if (chars[i] == tupleChars.second() && !stringOpen) {
				if (!tupleOpen) {
					throw new Exception("Unexpected tuple end");
				}
				if (!currentValue.isEmpty()) {
					addToken.accept(TokenType.ArgumentValue, currentValue.toString());
				}
				addToken.accept(TokenType.ArgumentValueTupleEnd, null);
				currentValue.setLength(0);
				tupleOpen = false;
			} else if (i == chars.length - 1) {
				if (tupleOpen) {
					throw new Exception("Unexpected EOL. Tuple isn't closed");
				}
				if (stringOpen) {
					throw new Exception("Unexpected EOL. String isn't closed");
				}
				currentValue.append(chars[i]);
				tokenizeSection.run();
			} else if (stringOpen) {
				if (chars[i] == '\\') i++; // user is trying to escape a character
				currentValue.append(chars[i]);
			} else if ((chars[i] == ' ' || chars[i] == '=') && !currentValue.isEmpty()) {
				tokenizeSection.run();
			} else if (chars[i] != ' ') {
				currentValue.append(chars[i]);
			}
		}

		return ParseResult.CORRECT(result.toArray(Token[]::new));
	}


	private Token tokenizeSection(String str) {
		TokenType type;

		if (this.isArgAlias(str)) {
			type = TokenType.ArgumentAlias;
		} else if (this.isArgNames(str)) {
			type = TokenType.ArgumentNameList;
		} else {
			type = TokenType.ArgumentValue;
		}

		return new Token(type, str);
	}
}
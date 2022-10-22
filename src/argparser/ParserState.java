package argparser;

import argparser.exceptions.Result;
import argparser.utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
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

		public static <TReturn> ParseResult<TReturn> ERROR(ParseErrorType reason, int position) {
			return new ParseResult<>(false, position, reason);
		}
	}

	public ParserState(String cliArgs, ArrayList<Argument<?, ?>> u_args, TupleCharacter tc) {
		this.cliArgs = cliArgs.toCharArray();
		this.tupleChars = tc.getCharPair();
		this.specifiedArguments = u_args;
		this.possiblePrefixes = u_args.stream().map(a -> a.prefix).distinct().toList();
		this.positionalArguments = u_args.stream().filter(Argument::isPositional).toArray(Argument[]::new);
	}

	public void parse() throws Exception {
		this.tokens = this.tokenize().unpack();

		short argumentAliasCount = 0;
		boolean foundNonPositionalArg = false;

		for (this.currentTokenIndex = 0; this.currentTokenIndex < tokens.length; this.currentTokenIndex++) {
			Token c_token = this.tokens[currentTokenIndex];

			if (c_token.type() == TokenType.ArgumentAlias) {
				runForArgument(c_token.contents(), this::executeArgParse);
				foundNonPositionalArg = true;
			} else if (c_token.type() == TokenType.ArgumentNameList) {
				parseSimpleArgs(c_token.contents().substring(1));
				foundNonPositionalArg = true;
			} else if (
				(c_token.type() == TokenType.ArgumentValue || c_token.type() == TokenType.ArgumentValueTupleStart)
					&& !foundNonPositionalArg
			) { // this is most likely a positional argument
				var a = getArgumentByPositionalIndex(argumentAliasCount);
				if (a != null) {
					if (c_token.type() == TokenType.ArgumentValue)
						this.currentTokenIndex--; // subtract one here because we need to start parsing from this index
					executeArgParse(a);
					argumentAliasCount++;
				}
			} else {
				System.out.println("PARSE: Unmatched token " + c_token.type());
			}
		}

		this.specifiedArguments.forEach(Argument::finishParsing);
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

	private ParseResult<Void> parseSimpleArgs(String args) {
		// if its only one, we can parse the arg without problem
		if (args.length() == 1) {
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
				}
				this.executeArgParse(a, args.substring(const_index + 1)); // if this arg accepts more values, treat the rest of chars as value
				return ParseResult.ERROR(ParseErrorType.ArgNameListTakeValues);
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
	 * @param argAlias
	 * @param f
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
	 * @param argName
	 * @param f
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

		for (var character : str.substring(1).toCharArray()) {
			return this.specifiedArguments.stream().anyMatch(a -> a.checkMatch(character));
		}

		return false;
	}

	private ParseResult<Void> executeArgParse(Argument<?, ?> arg) {
		ArgValueCount argumentValuesRange = arg.getNumberOfValues();

		boolean isInTuple = false;

		if (currentTokenIndex < this.tokens.length - 1) {
			isInTuple = this.tokens[currentTokenIndex + 1].type() == TokenType.ArgumentValueTupleStart;
		}

		// just skip the whole thing if it doesn't need any values
		if (argumentValuesRange.isZero()) {
			arg.parseValues(new String[]{});
			return ParseResult.CORRECT();
		}

		short skipCount = argumentValuesRange.min;

		// first capture the minimum required values...
		ArrayList<Token> temp_args = new ArrayList<>(Arrays.stream(
			Arrays.copyOfRange(this.tokens, currentTokenIndex + (isInTuple ? 2 : 1), currentTokenIndex + argumentValuesRange.min + 1)
		).toList());

		// next add more values until we get to the max of the type, or we encounter another argument specifier
		for (
			int i = argumentValuesRange.min + 1;
			i <= argumentValuesRange.max + (isInTuple ? 1 : 0) && currentTokenIndex <= this.tokens.length;
			i++, skipCount++
		) {
			var actual_token = this.tokens[currentTokenIndex + i];
			if ((!isInTuple && actual_token.isArgumentSpecifier()) || actual_token.type() == TokenType.ArgumentValueTupleEnd)
				break;
			temp_args.add(actual_token);
		}

		// pass the arg values to the argument subparser
		arg.parseValues(temp_args.stream().map(Token::contents).toArray(String[]::new));

		if (isInTuple) skipCount++;

		this.currentTokenIndex += skipCount;
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
			if (chars[i] == '"') {
				if (stringOpen) {
					addToken.accept(TokenType.ArgumentValue, currentValue.toString());
					currentValue.setLength(0);
				} else if (!currentValue.isEmpty()) { // maybe a possible argNameList? tokenize it
					tokenizeSection.run();
				}
				stringOpen = !stringOpen;
			} else if (chars[i] == tupleChars.first() && !stringOpen) {
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
			} else if (stringOpen) {
				currentValue.append(chars[i]);
			} else if (chars[i] == ' ' && !currentValue.isEmpty()) {
				tokenizeSection.run();
			} else if (i == chars.length - 1) {
				if (tupleOpen) {
					throw new Exception("Unexpected EOL. Tuple isn't closed");
				}
				currentValue.append(chars[i]);
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
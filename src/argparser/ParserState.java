package argparser;

import argparser.exceptions.ArgParserException;
import argparser.exceptions.ParseResult;
import argparser.utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

class ParserState {
	private final String[] cli_args;
	private final ArrayList<Argument<?, ?>> specified_arguments;
	private final Pair<Character, Character> TUPLE_CHARS;
	/**
	 * All the possible argument prefixes that we can encounter.
	 */
	private final List<Character> possiblePrefixes;
	private Token[] tokens;
	private short currentTokenIndex = 0;

	public ParserState(String[] cli_args, ArrayList<Argument<?, ?>> u_args, TupleCharacter tc) {
		this.cli_args = cli_args;
		this.specified_arguments = u_args;
		this.possiblePrefixes = u_args.stream().map(a -> a.prefix).distinct().toList();
		this.TUPLE_CHARS = tc.getCharPair();
	}

	public void parse() throws Exception {
		this.tokens = this.tokenize();

		for (this.currentTokenIndex = 0; this.currentTokenIndex < tokens.length; this.currentTokenIndex++) {
			Token c_token = this.tokens[currentTokenIndex];

			if (c_token.type() == TokenType.ArgumentAlias) {
				runForArgument(c_token.contents(), this::executeArgParse);
			} else if (c_token.type() == TokenType.ArgumentNameList) {
				if (!parseSimpleArgs(c_token.contents().substring(1)).correct) {
					System.out.println("ufkc");
				}
			}
		}

		this.specified_arguments.forEach(Argument::invokeCallback);
	}

	private boolean isPossiblePrefix(String str) {
		return this.possiblePrefixes.contains(str.charAt(0));
	}

	private ParseResult parseSimpleArgs(String args) {
		// if its only one, we can parse the arg without problem
		if (args.length() == 1) {
			return runForArgument(args.charAt(0), this::executeArgParse);
		}

		var res = new ParseResult();

		// its multiple of them. We can only do this with arguments that accept 0 values.
		for (char simple_arg : args.toCharArray()) {
			res.addSubResult(this.runForArgument(simple_arg, a -> {
				if (a.getNumberOfValues().isZero()) {
					return this.executeArgParse(a);
				}
				return new ParseResult("Multiple arguments in name list cannot accept values");
			}));
		}

		// only return a correct result if all subresults are ok
		return res.correctByAll();
	}

	/**
	 * Executes a callback for the argument found by the alias specified.
	 *
	 * @param argAlias
	 * @param f
	 * @return <code>true</code> if an argument was found
	 */
	private ParseResult runForArgument(String argAlias, Function<Argument<?, ?>, ParseResult> f) throws ArgParserException {
		for (var argument : this.specified_arguments) {
			if (argument.checkMatch(argAlias)) {
				return f.apply(argument);
			}
		}
		return new ParseResult(String.format("Could not find an argument with the alias '%s'", argAlias));
	}

	/**
	 * Executes a callback for the argument found by the name specified.
	 *
	 * @param argName
	 * @param f
	 * @return <code>true</code> if an argument was found
	 */
	private ParseResult runForArgument(char argName, Function<Argument<?, ?>, ParseResult> f) {
		for (var argument : this.specified_arguments) {
			if (argument.checkMatch(argName)) {
				return f.apply(argument);
			}
		}
		return new ParseResult(String.format("Could not find an argument with the name '%s'", argName));
	}


	private boolean isArgAlias(String str) {
		// first try to figure out if the prefix is used, to save time (does it start with '--'? (assuming the prefix is '-'))
		if (
			str.length() > 1 // make sure we are working with long enough strings
				&& str.charAt(0) == str.charAt(1) // first and second chars are equal?
				&& this.isPossiblePrefix(str) // okay lets check if the prefix is valid
		) {
			// now check if the alias actually exist
			return this.specified_arguments.stream().anyMatch(a -> a.checkMatch(str));
		}

		return false;
	}

	private boolean isArgNames(String str) {
		if (!this.isPossiblePrefix(str)) {
			return false;
		}

		for (var character : str.substring(1).toCharArray()) {
			return this.specified_arguments.stream().anyMatch(a -> a.checkMatch(character));
		}

		return false;
	}

	private boolean isArgumentSpecifier(String str) {
		return isArgAlias(str) || isArgNames(str);
	}

	private ParseResult executeArgParse(Argument<?, ?> arg) {
		ArgValueCount argumentValuesRange = arg.getNumberOfValues();

		// just skip the whole thing if it doesn't need any values
		if (argumentValuesRange.isZero()) {
			arg.parseValues(new String[]{});
			return ParseResult.CORRECT;
		}

		short skipCount = argumentValuesRange.min;

		// first capture the minimum required values...
		ArrayList<Token> temp_args = new ArrayList<>(Arrays.stream(
			Arrays.copyOfRange(this.tokens, currentTokenIndex + 1, currentTokenIndex + argumentValuesRange.min + 1)
		).toList());

		// next add more values until we get to the max of the type, or we encounter another argument specifier
		for (
			int i = argumentValuesRange.min + 1;
			i <= argumentValuesRange.max && currentTokenIndex + i < this.tokens.length;
			i++, skipCount++
		) {
			var actual_token = this.tokens[currentTokenIndex + i];
			if (actual_token.isArgumentSpecifier()) break;
			temp_args.add(actual_token);
		}

		// pass the arg values to the argument subparser
		arg.parseValues(temp_args.stream().map(Token::contents).toArray(String[]::new));

		this.currentTokenIndex += skipCount;
		return ParseResult.CORRECT;
	}

	private Token[] tokenize() throws Exception {
		var result = new ArrayList<Token>();
		final var currentValue = new StringBuilder();
		BiConsumer<TokenType, String> addToken = (t, c) -> result.add(new Token(t, c));
		Runnable tokenizeSection = () -> result.add(this.tokenizeSection(currentValue.toString()));

		boolean stringOpen = false;
		boolean tupleOpen = false;

		var chars = String.join(" ", this.cli_args).toCharArray();

		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '"') {
				if (stringOpen) {
					addToken.accept(TokenType.ArgumentValue, currentValue.toString());
					currentValue.setLength(0);
				}
				stringOpen = !stringOpen;
			} else if (chars[i] == TUPLE_CHARS.first() && !stringOpen) {
				addToken.accept(TokenType.ArgumentValueTupleStart, null);
				tupleOpen = true;
			} else if (chars[i] == TUPLE_CHARS.second() && !stringOpen) {
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
				currentValue.setLength(0);
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

		return result.toArray(Token[]::new);
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
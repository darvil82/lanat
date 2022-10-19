package argparser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

class ParserState {
	private final String[] cli_args;
	private final ArrayList<Argument<?, ?>> specified_arguments;
	private final char TUPLE_START = '[';
	private final char TUPLE_END = ']';
	/**
	 * All the possible argument prefixes that we can encounter.
	 */
	private final List<Character> possiblePrefixes;
	private short currentArgIndex = 0;

	public ParserState(String[] cli_args, ArrayList<Argument<?, ?>> specified_arguments) {
		this.cli_args = cli_args;
		this.specified_arguments = specified_arguments;
		this.possiblePrefixes = specified_arguments.stream().map(a -> a.prefix).distinct().toList();
	}

	public void parse() {
		for (this.currentArgIndex = 0; this.currentArgIndex < this.cli_args.length; this.currentArgIndex++) {
			String arg = this.cli_args[currentArgIndex];

			if (this.isArgNames(arg)) {
				this.parseSimpleArgs(arg.substring(1).toCharArray());
				continue;
			}

			if (!this.runForArgument(arg, this::executeArgParse)) {
				throw new IllegalArgumentException("Unknown argument " + arg);
			}
		}

		this.specified_arguments.forEach(Argument::invokeCallback);
	}

	private boolean isPossiblePrefix(String prefix) {
		return this.possiblePrefixes.contains(prefix.charAt(0));
	}

	private void parseSimpleArgs(char[] args) {
		for (char simple_arg : args) {
			this.runForArgument(simple_arg, this::executeArgParse);
		}
	}

	/**
	 * Executes a callback for the argument found by the alias specified.
	 *
	 * @param argAlias
	 * @param f
	 *
	 * @return <code>true</code> if an argument was found
	 */
	private boolean runForArgument(String argAlias, Consumer<Argument<?, ?>> f) {
		for (var argument : this.specified_arguments) {
			if (argument.checkMatch(argAlias)) {
				f.accept(argument);
				return true;
			}
		}
		return false;
	}

	/**
	 * Executes a callback for the argument found by the alias specified.
	 *
	 * @param argName
	 * @param f
	 *
	 * @return <code>true</code> if an argument was found
	 */
	private boolean runForArgument(char argName, Consumer<Argument<?, ?>> f) {
		for (var argument : this.specified_arguments) {
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

	private void executeArgParse(Argument<?, ?> arg) {
		ArgValueCount argumentValuesRange = arg.getNumberOfValues();

		// just skip the whole thing if it doesn't need any values
		if (argumentValuesRange.isZero()) {
			arg.parseValues(new String[]{});
			return;
		}

		short skipCount = argumentValuesRange.min;

		// first capture the minimum required values...
		ArrayList<String> temp_args = new ArrayList<>(Arrays.stream(
			Arrays.copyOfRange(this.cli_args, currentArgIndex + 1, currentArgIndex + argumentValuesRange.min + 1)
		).toList());

		// next add more values until we get to the max of the type, or we encounter another argument specifier
		for (
			int x = argumentValuesRange.min + 1;
			x <= argumentValuesRange.max && currentArgIndex + x < this.cli_args.length;
			x++, skipCount++
		) {
			var actual_value = this.cli_args[currentArgIndex + x];
			if (isArgumentSpecifier(actual_value)) break;
			temp_args.add(actual_value);
		}

		// pass the arg values to the argument subparser
		arg.parseValues(temp_args.toArray(String[]::new));

		this.currentArgIndex += skipCount;
	}

	public Token[] tokenize() throws Exception {
		var result = new ArrayList<Token>();
		BiConsumer<TokenType, String> addToken = (t, c) -> result.add(new Token(t, c));
		var currentValue = new StringBuilder();
		boolean stringOpen = false;
		boolean tupleOpen = false;

		var chars = String.join(" ", this.cli_args).toCharArray();

		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '"') {
				if (stringOpen) {
					addToken.accept(TokenType.ArgumentValue, currentValue.toString());
					currentValue = new StringBuilder();
				}
				stringOpen = !stringOpen;
			} else if (chars[i] == TUPLE_START && !stringOpen) {
				addToken.accept(TokenType.ArgumentValueTupleStart, String.valueOf(TUPLE_START));
				tupleOpen = true;
			} else if (chars[i] == TUPLE_END && !stringOpen) {
				addToken.accept(TokenType.ArgumentValue, currentValue.toString());
				addToken.accept(TokenType.ArgumentValueTupleEnd, String.valueOf(TUPLE_END));
				currentValue = new StringBuilder();
				tupleOpen = false;
			} else if (stringOpen) {
				currentValue.append(chars[i]);
			} else if (chars[i] == ' ' && !tupleOpen) {
				result.add(this.tokenizeSection(currentValue.toString()));
				currentValue = new StringBuilder();
			} else {
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
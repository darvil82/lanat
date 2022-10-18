package argparser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ParserState {
	private final String[] cli_args;
	private final ArrayList<Argument<?, ?>> specified_arguments;
	/**
	 * All the possible argument prefixes that we can encounter.
	 */
	private final List<Character> possiblePrefixes;
	private byte currentArgIndex = 0;

	public ParserState(String[] cli_args, ArrayList<Argument<?, ?>> specified_arguments) {
		this.cli_args = cli_args;
		this.specified_arguments = specified_arguments;
		this.possiblePrefixes = specified_arguments.stream().map(a -> a.prefix).distinct().toList();
	}

	public void parse() {
		for (this.currentArgIndex = 0; this.currentArgIndex < this.cli_args.length; this.currentArgIndex++) {
			String arg = this.cli_args[currentArgIndex];

			if (this.isArgumentSpecifier(arg)) {
				this.parseSimpleArgs(arg.substring(1).toCharArray());
				continue;
			}

			if (!checkMatchOfArg(arg)) {
				throw new IllegalArgumentException("Unknown argument " + arg);
			}
		}

		this.specified_arguments.forEach(Argument::invokeCallback);
	}

	private void parseSimpleArgs(char[] args) {
		for (char simple_arg : args) {
			checkMatchOfArg(simple_arg);
		}
	}

	private boolean checkMatchOfArg(String argAlias) {
		for (var argument : this.specified_arguments) {
			if (argument.checkMatch(argAlias)) {
				executeArgParse(argument);
				return true;
			}
		}
		return false;
	}

	private boolean checkMatchOfArg(char argName) {
		for (var argument : this.specified_arguments) {
			if (argument.checkMatch(argName)) {
				executeArgParse(argument);
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
				&& this.possiblePrefixes.contains(str.charAt(0)) // okay lets check if the prefix is valid
		) {
			// now check if the aliases actually exist
			return this.specified_arguments.stream().allMatch(a -> a.checkMatch(str));
		}

		return false;
	}

	private boolean isArgNames(String str) {
		if (!this.possiblePrefixes.contains(str.charAt(0))) {
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
		byte skipCount = argumentValuesRange.min;

		// first capture the minimum required values...
		ArrayList<String> temp_args = new ArrayList<>(Arrays.stream(
				Arrays.copyOfRange(this.cli_args, currentArgIndex + 1, currentArgIndex + argumentValuesRange.min + 1)
		).toList());

		// next add more values until we get to the max of the type, or we encounter another argument specifier
		for (int x = argumentValuesRange.min + 1; x < argumentValuesRange.max + 1; x++, skipCount++) {
			if (currentArgIndex + x > this.cli_args.length) break;

			var actual_value = this.cli_args[currentArgIndex + x];
			if (isArgumentSpecifier(actual_value)) break;
			temp_args.add(actual_value);
		}

		// pass the arg values to the argument subparser
		arg.parseValues(temp_args.toArray(String[]::new));

		this.currentArgIndex += skipCount;
	}

}
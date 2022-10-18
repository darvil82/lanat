package argparser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ParserState {
	private final String[] cli_args;
	private final ArrayList<Argument<?, ?>> specified_arguments;
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

			if (this.possiblePrefixes.contains(arg.charAt(0)) && !this.possiblePrefixes.contains(arg.charAt(1))) {
				this.parseSimpleArgs(arg.substring(1).toCharArray());
				continue;
			}

			checkMatchOfArg(arg);
		}

		this.specified_arguments.forEach(Argument::invokeCallback);
	}

	private void parseSimpleArgs(char[] args) {
		for (char simple_arg : args) {
			checkMatchOfArg(simple_arg);
		}
	}

	private void checkMatchOfArg(String argAlias) {
		for (var argument : this.specified_arguments) {
			if (argument.checkMatch(argAlias)) {
				executeArgParse(argument);
				break;
			}
		}
	}

	private void checkMatchOfArg(char argName) {
		for (var argument : this.specified_arguments) {
			if (argument.checkMatch(argName)) {
				executeArgParse(argument);
				break;
			}
		}
	}

	private void executeArgParse(Argument<?, ?> arg) {
		byte argValueSkipCount = arg.getNumberOfValues().max;
		arg.parseValues(Arrays.copyOfRange(this.cli_args, currentArgIndex + 1, currentArgIndex + argValueSkipCount + 1));
		this.currentArgIndex += argValueSkipCount;
	}
}
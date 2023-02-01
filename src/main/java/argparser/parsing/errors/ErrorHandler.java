package argparser.parsing.errors;

import argparser.*;
import argparser.utils.ErrorLevelProvider;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.*;


public class ErrorHandler {
	final List<Token> tokens;
	private final Command rootCmd;
	int cmdAbsoluteTokenIndex = 0;


	public ErrorHandler(Command rootCommand) {
		this.rootCmd = rootCommand;
		this.tokens = rootCommand.getFullTokenList();
	}

	/**
	 * Handles all errors and displays them to the user.
	 */
	public List<String> handleErrorsGetMessages() {
		final List<Command> commands = this.rootCmd.getTokenizer().getTokenizedSubCommands();
		final ArrayList<String> errors = new ArrayList<>();

		for (int i = 0; i < commands.size(); i++) {
			Command cmd = commands.get(i);
			this.cmdAbsoluteTokenIndex = this.getCommandTokenIndexByNestingLevel(i);

			new ArrayList<ParseStateErrorBase<?>>() {{
				addAll(cmd.getErrorsUnderDisplayLevel());
				addAll(cmd.getTokenizer().getErrorsUnderDisplayLevel());
				addAll(cmd.getParser().getCustomErrors());
				addAll(ParseError.filter(cmd.getParser().getErrorsUnderDisplayLevel()));
			}}.stream()
				.sorted(Comparator.comparingInt(x -> x.tokenIndex))
				.forEach(e -> errors.add(e.handle(this)));
		}

		return Collections.unmodifiableList(errors);
	}

	public void handleErrorsPrint() {
		for (String error : this.handleErrorsGetMessages()) {
			System.out.println(error);
		}
	}

	/**
	 * Returns the token at the specified index, offset by the current command's token index ({@link #cmdAbsoluteTokenIndex}).
	 */
	public Token getRelativeToken(int index) {
		return this.tokens.get(this.cmdAbsoluteTokenIndex + index);
	}

	/**
	 * Returns the index of a command in the token list by its nesting level by order of appearance.
	 * For example, in a token list like this:<br>
	 * <pre>{@code
	 * {
	 *   SUB_COMMAND,
	 *   ARGUMENT_NAME,
	 *   ARGUMENT_VALUE,
	 *   SUB_COMMAND, // <- here
	 *   ARGUMENT_NAME_LIST,
	 *   SUB_COMMAND,
	 *   ARGUMENT_NAME
	 * }}</pre>
	 * The nesting level of the second subcommand is <strong>1</strong> (starting at 0),
	 * and its index in the token list is <strong>3</strong>.
	 *
	 * @return <code>-1</code> if the command is not found.
	 */
	private int getCommandTokenIndexByNestingLevel(int level) {
		if (level <= 0) return 0;

		for (int i = 0, appearances = 0; i < this.tokens.size(); i++) {
			if (this.tokens.get(i).type() == TokenType.SUB_COMMAND) {
				appearances++;
			}
			if (appearances > level) {
				return i;
			}
		}

		return -1;
	}

	public int getErrorCode() {
		return this.rootCmd.getErrorCode();
	}

	public Command getRootCmd() {
		return rootCmd;
	}
}
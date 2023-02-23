package lanat.parsing.errors;

import lanat.Command;
import lanat.parsing.Token;
import lanat.parsing.TokenType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class ErrorHandler {
	public final @NotNull List<@NotNull Token> tokens;
	private final @NotNull Command rootCmd;
	private int absoluteCmdTokenIndex = 0;


	public ErrorHandler(@NotNull Command rootCommand) {
		this.rootCmd = rootCommand;
		this.tokens = Collections.unmodifiableList(rootCommand.getFullTokenList());
	}

	/**
	 * Handles all errors and returns a list of them.
	 */
	public @NotNull List<@NotNull String> handleErrorsGetMessages() {
		final List<Command> commands = this.rootCmd.getTokenizer().getTokenizedSubCommands();
		final ArrayList<String> errors = new ArrayList<>();

		for (int i = 0; i < commands.size(); i++) {
			final Command cmd = commands.get(i);
			this.absoluteCmdTokenIndex = this.getCommandTokenIndexByNestingLevel(i);

			new ArrayList<ParseStateErrorBase<?>>() {{
				this.addAll(cmd.getErrorsUnderDisplayLevel());
				this.addAll(cmd.getTokenizer().getErrorsUnderDisplayLevel());
				this.addAll(cmd.getParser().getCustomErrors());
				this.addAll(ParseError.filter(cmd.getParser().getErrorsUnderDisplayLevel()));
			}}.stream()
				.sorted(Comparator.comparingInt(x -> x.tokenIndex))
				.forEach(e -> errors.add(e.handle(this)));
		}

		return Collections.unmodifiableList(errors);
	}

	/**
	 * Returns the token at the specified index, offset by the current command's token index
	 * ({@link #absoluteCmdTokenIndex}).
	 */
	public @NotNull Token getRelativeToken(int index) {
		return this.tokens.get(this.absoluteCmdTokenIndex + index);
	}

	/**
	 * Returns the index of a command in the token list by its nesting level by order of appearance. For example, in a
	 * token list like this:<br>
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
	 * The nesting level of the second Sub-Command is <strong>1</strong> (starting at 0), and its index in the token list
	 * is <strong>3</strong>.
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

	public @NotNull Command getRootCmd() {
		return this.rootCmd;
	}

	public int getAbsoluteCmdTokenIndex() {
		return this.absoluteCmdTokenIndex;
	}
}
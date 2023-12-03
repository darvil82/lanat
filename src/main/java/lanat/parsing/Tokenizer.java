package lanat.parsing;

import lanat.Argument;
import lanat.Command;
import lanat.TupleChar;
import lanat.parsing.errors.Error;
import lanat.parsing.errors.TokenizeErrors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

/**
 * Tokenizes the input string given. When finished, the tokens can be retrieved using
 * {@link Tokenizer#getFinalTokens()}
 */
public final class Tokenizer extends ParsingStateBase<Error.TokenizeError> {
	/** Are we currently within a tuple? */
	private boolean tupleOpen = false;

	/** Are we currently within a string? */
	private boolean stringOpen = false;

	/** The index of the current character in the {@link Tokenizer#inputString} */
	private int currentCharIndex = 0;

	/** The tokens that have been parsed so far */
	private final @NotNull List<@NotNull Token> finalTokens = new ArrayList<>();

	/** The current value of the token that is being parsed */
	private final @NotNull StringBuilder currentValue = new StringBuilder();

	/** The input string that is being tokenized */
	private String inputString;

	/** The input string that is being tokenized, split into characters */
	private char[] inputChars;


	public Tokenizer(@NotNull Command command) {
		super(command);
	}


	private void setInputString(@NotNull String inputString, int nestingOffset, int lastCharIndex) {
		this.nestingOffset = lastCharIndex + nestingOffset;
		this.inputString = inputString.substring(lastCharIndex);
		this.inputChars = this.inputString.toCharArray();
	}

	/**
	 * Tokenizes the input string given. When finished, the tokens can be retrieved using
	 * {@link Tokenizer#getFinalTokens()}
	 */
	public void tokenize(@NotNull String input, @Nullable Tokenizer previousTokenizer) {
		assert !this.hasFinished : "Tokenizer has already finished tokenizing.";

		if (previousTokenizer == null)
			this.setInputString(input, 0, 0);
		else
			this.setInputString(input, previousTokenizer.nestingOffset, previousTokenizer.currentCharIndex);


		// nothing to tokenize. Just finish
		if (input.isBlank()) {
			this.hasFinished = true;
			return;
		}

		char currentStringChar = 0; // the character that opened the string
		int lastStringCharIndex = 0; // the index of the last character that opened the string
		int lastTupleCharIndex = 0; // the index of the last character that opened the tuple

		for (;
			this.currentCharIndex < this.inputChars.length && !this.hasFinished;
			this.currentCharIndex++
		) {
			char cChar = this.inputChars[this.currentCharIndex];

			// user is trying to escape a character
			if (cChar == '\\') {
				this.currentValue.append(this.inputChars[++this.currentCharIndex]); // skip the \ character and append the next character

				// reached a possible value wrapped in quotes
			} else if (cChar == '"' || cChar == '\'') {
				// if we are already in an open string, push the current value and close the string. Make sure
				// that the current char is the same as the one that opened the string
				if (this.stringOpen && currentStringChar == cChar) {
					// strings require a space after them
					if (!this.isCharAtRelativeIndex(1, Character::isWhitespace) && !this.isLastChar()) {
						this.addError(new TokenizeErrors.SpaceRequiredError(this.currentCharIndex));
						continue;
					}

					this.addToken(TokenType.ARGUMENT_VALUE, this.currentValue.toString());
					this.currentValue.setLength(0);
					this.stringOpen = false;

					// the string is open, but the character does not match, or there's something already in the current value.
					// Push it as a normal character
				} else if (this.stringOpen) {
					this.currentValue.append(cChar);

					// strings require a space behind them.
				} else if (!this.currentValue.isEmpty()) {
					this.addError(new TokenizeErrors.SpaceRequiredError(this.currentCharIndex - 1));

					// the string is not open, so open it and set the current string char to the current char
				} else {
					this.stringOpen = true;
					currentStringChar = cChar;
					lastStringCharIndex = this.currentCharIndex;
				}

				// append characters to the current value as long as we are in a string
			} else if (this.stringOpen) {
				this.currentValue.append(cChar);

				// reached a possible tuple start character
			} else if (cChar == this.getTupleChars().open) {
				// if we are already in a tuple, add error
				if (this.tupleOpen) {
					// push tuple start token so the user can see the incorrect tuple char
					this.addError(new TokenizeErrors.TupleAlreadyOpenError(this.currentCharIndex));
					continue;
				} else if (!this.currentValue.isEmpty()) { // if there was something before the tuple, tokenize it
					this.tokenizeCurrentValue();
				}

				// set the state to tuple open
				this.addToken(TokenType.ARGUMENT_VALUE_TUPLE_START, this.getTupleChars().open);
				this.tupleOpen = true;
				lastTupleCharIndex = this.currentCharIndex;

				// reached a possible tuple end character
			} else if (cChar == this.getTupleChars().close) {
				if (!this.isCharAtRelativeIndex(1, Character::isWhitespace) && !this.isLastChar()) {
					this.addError(new TokenizeErrors.SpaceRequiredError(this.currentCharIndex));
					continue;
				}

				// if we are not in a tuple, set error and stop tokenizing
				if (!this.tupleOpen) {
					// push tuple start token so the user can see the incorrect tuple char
					this.addError(new TokenizeErrors.UnexpectedTupleCloseError(this.currentCharIndex));
					continue;
				}

				// if there was something before the tuple, tokenize it
				if (!this.currentValue.isEmpty()) {
					this.addToken(TokenType.ARGUMENT_VALUE, this.currentValue.toString());
				}

				// set the state to tuple closed
				this.addToken(TokenType.ARGUMENT_VALUE_TUPLE_END, this.getTupleChars().close);
				this.currentValue.setLength(0);
				this.tupleOpen = false;

				// reached a "--". Push all the rest as a FORWARD_VALUE.
			} else if (
				cChar == '-'
					&& this.isCharAtRelativeIndex(1, '-')
					&& this.isCharAtRelativeIndex(2, Character::isWhitespace)
			)
			{
				this.addToken(TokenType.FORWARD_VALUE, this.inputString.substring(this.currentCharIndex + 3));
				break;

				// reached a possible separator
			} else if (
				(Character.isWhitespace(cChar) && !this.currentValue.isEmpty()) // there's a space and some value to tokenize
					// also check if this is defining the value of an argument, or we are in a tuple. If so, don't tokenize
					|| (cChar == '=' && !this.tupleOpen && this.isArgumentSpecifier(this.currentValue.toString()))
			)
			{
				this.tokenizeCurrentValue();

				// push the current char to the current value
			} else if (!Character.isWhitespace(cChar)) {
				this.currentValue.append(cChar);
			}
		}

		if (this.tupleOpen)
			this.addError(new TokenizeErrors.TupleNotClosedError(lastTupleCharIndex));
		if (this.stringOpen)
			this.addError(new TokenizeErrors.StringNotClosedError(lastStringCharIndex));

		// we left something in the current value, tokenize it
		if (!this.currentValue.isEmpty()) {
			this.tokenizeCurrentValue();
		}

		this.hasFinished = true;
	}

	/** Inserts a token into the final tokens list with the given type and contents */
	private void addToken(@NotNull TokenType type, @NotNull String contents) {
		this.finalTokens.add(new Token(type, contents));
	}

	/** Inserts a token into the final tokens list with the given type and contents */
	private void addToken(@NotNull TokenType type, char contents) {
		this.finalTokens.add(new Token(type, String.valueOf(contents)));
	}

	/**
	 * Returns {@code true} if the current char index is the last one in the input chars
	 * @return {@code true} if the current char index is the last one in the input chars
	 */
	private boolean isLastChar() {
		return this.currentCharIndex == this.inputChars.length - 1;
	}


	/**
	 * Tokenizes a single word and returns the token matching it. If no match could be found, returns
	 * {@link TokenType#ARGUMENT_VALUE}
	 */
	private @NotNull Token tokenizeWord(@NotNull String str) {
		final TokenType type;

		if (this.tupleOpen || this.stringOpen) {
			type = TokenType.ARGUMENT_VALUE;
		} else if (this.isArgName(str)) {
			type = TokenType.ARGUMENT_NAME;
		} else if (this.isArgNameList(str)) {
			type = TokenType.ARGUMENT_NAME_LIST;
		} else if (this.isSubCommand(str)) {
			type = TokenType.COMMAND;
		} else {
			type = TokenType.ARGUMENT_VALUE;
		}

		return new Token(type, str);
	}

	/**
	 * Tokenizes the {@link Tokenizer#currentValue} and adds it to the final tokens list.
	 * <p>
	 * If the token is a Sub-Command, it will forward the rest of the input string to the Sub-Command's tokenizer.
	 * </p>
	 */
	private void tokenizeCurrentValue() {
		final Token token = this.tokenizeWord(this.currentValue.toString());

		this.finalTokens.add(token);

		// if this is a Sub-Command, continue tokenizing next elements
		if (token.type() == TokenType.COMMAND) {
			// forward the rest of stuff to the Sub-Command
			this.command.getCommand(token.contents())
				.getTokenizer()
				.tokenize(this.inputString, this);

			this.hasFinished = true;
		}

		this.currentValue.setLength(0);
	}

	/**
	 * Returns {@code true} if the given string can be an argument name list, eg: <code>"-fbq"</code>.
	 * <p>
	 * This returns {@code true} if at least the first character is a valid argument prefix and at least one of the
	 * next characters is a valid argument name.
	 * <br><br>
	 * For a prefix to be valid, it must be a character used as a prefix on the next argument/s specified.
	 * </p>
	 */
	private boolean isArgNameList(@NotNull String str) {
		if (str.length() < 2 || !Character.isAlphabetic(str.charAt(1))) return false;

		// store the possible prefixes. Start with the common ones (single and double dash)
		// We add the common prefixes because it can be confusing for the user to have to put a specific prefix
		// used by any argument in the name list
		final var possiblePrefixes = new HashSet<>(Arrays.asList(Argument.PrefixChar.COMMON_PREFIXES));
		int foundArgs = 0; // how many characters in the string are valid arguments

		// iterate over the characters in the string, starting from the second one (the first one is the prefix)
		for (final char argName : str.substring(1).toCharArray()) {
			// if an argument is found with that char name, append its prefix to the possible prefixes
			// and increment the foundArgs counter.
			// If no argument is found, stop checking
			if (!this.runForMatchingArgument(argName, argument -> possiblePrefixes.add(argument.getPrefix())))
				break;
			foundArgs++;
		}

		// if there's at least one argument and the first character is a valid prefix, return true
		return foundArgs >= 1 && possiblePrefixes.stream().anyMatch(p -> p.character == str.charAt(0));
	}

	/**
	 * Returns {@code true} if the given string can be an argument name, eg: <code>"--help"</code>.
	 * <p>
	 * This returns {@code true} if the given string is a valid argument name with a double prefix.
	 * </p>
	 */
	private boolean isArgName(@NotNull String str) {
		// make sure we are working with long enough strings
		return str.length() > 1 && this.getMatchingArgument(str) != null;
	}

	/**
	 * Returns {@code true} whether the given string is an argument name {@link Tokenizer#isArgName(String)} or an
	 * argument name list {@link Tokenizer#isArgNameList(String)}.
	 */
	private boolean isArgumentSpecifier(@NotNull String str) {
		return this.isArgName(str) || this.isArgNameList(str);
	}

	private boolean isSubCommand(@NotNull String str) {
		return this.command.hasCommand(str);
	}

	/**
	 * Returns {@code true} if the character of {@link Tokenizer#inputChars} at a relative index from
	 * {@link Tokenizer#currentCharIndex} is equal to the specified character.
	 * <p>
	 * If the index is out of bounds, returns {@code false}.
	 * </p>
	 */
	private boolean isCharAtRelativeIndex(int index, char character) {
		return this.isCharAtRelativeIndex(index, cChar -> cChar == character);
	}

	private boolean isCharAtRelativeIndex(int index, @NotNull Predicate<@NotNull Character> predicate) {
		index += this.currentCharIndex;
		if (index >= this.inputChars.length || index < 0) return false;
		return predicate.test(this.inputChars[index]);
	}

	private @NotNull TupleChar getTupleChars() {
		return this.command.getTupleChars();
	}

	/**
	 * Returns a list of all tokenized Sub-Commands children of {@link Tokenizer#command}. (Including the current)
	 * <p>
	 * Note that a Command only has a single tokenized Sub-Command, so this will have one Command per nesting level.
	 * </p>
	 */
	public @NotNull List<@NotNull Command> getTokenizedCommands() {
		final List<Command> x = new ArrayList<>();
		final Command subCmd;

		x.add(this.command);
		if ((subCmd = this.getTokenizedSubCommand()) != null) {
			x.addAll(subCmd.getTokenizer().getTokenizedCommands());
		}
		return x;
	}

	/** Returns the tokenized Sub-Command of {@link Tokenizer#command}. */
	public @Nullable Command getTokenizedSubCommand() {
		return this.command.getCommands().stream()
			.filter(sb -> sb.getTokenizer().hasFinished)
			.findFirst()
			.orElse(null);
	}

	/** Returns the list of all tokens that have been tokenized. */
	public @NotNull List<@NotNull Token> getFinalTokens() {
		assert this.hasFinished : "Cannot get final tokens before tokenizing has finished";
		return this.finalTokens;
	}

	public String getInputString() {
		return this.inputString;
	}
}

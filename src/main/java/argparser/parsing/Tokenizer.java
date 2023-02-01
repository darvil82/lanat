package argparser.parsing;

import argparser.Argument;
import argparser.Command;
import argparser.Token;
import argparser.TokenType;
import argparser.parsing.errors.TokenizeError;
import argparser.utils.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class Tokenizer extends ParsingStateBase<TokenizeError> {
	protected boolean tupleOpen = false;
	protected boolean stringOpen = false;
	protected boolean finishedTokenizing = false;
	public final Pair<Character, Character> tupleChars;

	public Tokenizer(Command command) {
		super(command);
		this.tupleChars = command.getTupleChars().getCharPair();
	}

	void addError(TokenizeError.TokenizeErrorType type, int index) {
		this.addError(new TokenizeError(type, index));
	}


	public List<Token> tokenize(String content) {
		this.finishedTokenizing = false; // just in case we are tokenizing again for any reason

		final var finalTokens = new ArrayList<Token>();
		final var currentValue = new StringBuilder();
		final char[] chars = content.toCharArray();

		final var values = new Object() {
			int i;
			char currentStringChar = 0;
			TokenizeError.TokenizeErrorType errorType = null;
		};

		final BiConsumer<TokenType, String> addToken = (t, c) -> finalTokens.add(new Token(t, c));

		final Runnable tokenizeSection = () -> {
			Token token = this.tokenizeSection(currentValue.toString());
			Command subCmd;
			// if this is a subcommand, continue tokenizing next elements
			if (token.type() == TokenType.SUB_COMMAND && (subCmd = getSubCommandByName(token.contents())) != null) {
				// forward the rest of stuff to the subCommand
				subCmd.getTokenizer().tokenize(content.substring(values.i));
				this.finishedTokenizing = true;
			} else {
				finalTokens.add(token);
			}
			currentValue.setLength(0);
		};

		BiPredicate<Integer, Character> charAtRelativeIndex = (index, character) -> {
			index += values.i;
			if (index >= chars.length || index < 0) return false;
			return chars[index] == character;
		};


		for (values.i = 0; values.i < chars.length && !this.finishedTokenizing; values.i++) {
			char cChar = chars[values.i];

			// user is trying to escape a character
			if (cChar == '\\') {
				currentValue.append(chars[++values.i]); // skip the \ character and append the next character

				// reached a possible value wrapped in quotes
			} else if (cChar == '"' || cChar == '\'') {
				// if we are already in an open string, push the current value and close the string. Make sure
				// that the current char is the same as the one that opened the string
				if (this.stringOpen && values.currentStringChar == cChar) {
					addToken.accept(TokenType.ARGUMENT_VALUE, currentValue.toString());
					currentValue.setLength(0);
					this.stringOpen = false;

					// the string is open, but the character does not match. Push it as a normal character
				} else if (this.stringOpen) {
					currentValue.append(cChar);

					// the string is not open, so open it and set the current string char to the current char
				} else {
					this.stringOpen = true;
					values.currentStringChar = cChar;
				}

				// append characters to the current value as long as we are in a string
			} else if (this.stringOpen) {
				currentValue.append(cChar);

				// reached a possible tuple start character
			} else if (cChar == this.tupleChars.first()) {
				// if we are already in a tuple, set error and stop tokenizing
				if (this.tupleOpen) {
					values.errorType = TokenizeError.TokenizeErrorType.TUPLE_ALREADY_OPEN;
					break;
				} else if (!currentValue.isEmpty()) { // if there was something before the tuple, tokenize it
					tokenizeSection.run();
				}

				// push the tuple token and set the state to tuple open
				addToken.accept(TokenType.ARGUMENT_VALUE_TUPLE_START, this.tupleChars.first().toString());
				this.tupleOpen = true;

				// reached a possible tuple end character
			} else if (cChar == this.tupleChars.second()) {
				// if we are not in a tuple, set error and stop tokenizing
				if (!this.tupleOpen) {
					values.errorType = TokenizeError.TokenizeErrorType.UNEXPECTED_TUPLE_CLOSE;
					break;
				}

				// if there was something before the tuple, tokenize it
				if (!currentValue.isEmpty()) {
					addToken.accept(TokenType.ARGUMENT_VALUE, currentValue.toString());
				}

				// push the tuple token and set the state to tuple closed
				addToken.accept(TokenType.ARGUMENT_VALUE_TUPLE_END, this.tupleChars.second().toString());
				currentValue.setLength(0);
				this.tupleOpen = false;

				// reached a "--". Push all the rest as a FORWARD_VALUE.
			} else if (cChar == '-' && charAtRelativeIndex.test(1, '-') && charAtRelativeIndex.test(2, ' ')) {
				addToken.accept(TokenType.FORWARD_VALUE, content.substring(values.i + 3));
				break;

				// reached a possible separator
			} else if (
				(cChar == ' ' && !currentValue.isEmpty()) // there's a space and some value to tokenize
					// also check if this is defining the value of an argument, or we are in a tuple. If so, don't tokenize
					|| (cChar == '=' && !this.tupleOpen && this.isArgumentSpecifier(currentValue.toString()))
			)
			{
				tokenizeSection.run();

				// push the current char to the current value
			} else if (cChar != ' ') {
				currentValue.append(cChar);
			}
		}

		if (values.errorType == null)
			if (this.tupleOpen) {
				values.errorType = TokenizeError.TokenizeErrorType.TUPLE_NOT_CLOSED;
			} else if (this.stringOpen) {
				values.errorType = TokenizeError.TokenizeErrorType.STRING_NOT_CLOSED;
			}

		// we left something in the current value, tokenize it
		if (!currentValue.isEmpty()) {
			tokenizeSection.run();
		}

		if (values.errorType != null) {
			this.addError(values.errorType, finalTokens.size());
		}

		this.finishedTokenizing = true;
		return Collections.unmodifiableList(finalTokens);
	}

	private Token tokenizeSection(String str) {
		final TokenType type;

		if (this.tupleOpen || this.stringOpen) {
			type = TokenType.ARGUMENT_VALUE;
		} else if (this.isArgName(str)) {
			type = TokenType.ARGUMENT_NAME;
		} else if (this.isArgNameList(str)) {
			type = TokenType.ARGUMENT_NAME_LIST;
		} else if (this.isSubCommand(str)) {
			type = TokenType.SUB_COMMAND;
		} else {
			type = TokenType.ARGUMENT_VALUE;
		}

		return new Token(type, str);
	}

	public List<Command> getTokenizedSubCommands() {
		final List<Command> x = new ArrayList<>();
		final Command subCmd;

		x.add(this.command);
		if ((subCmd = this.getTokenizedSubCommand()) != null) {
			x.addAll(subCmd.getTokenizer().getTokenizedSubCommands());
		}
		return x;
	}

	private boolean isArgNameList(String str) {
		if (str.length() < 2) return false;

		final var possiblePrefixes = new ArrayList<Character>();
		final var charArray = str.substring(1).toCharArray();

		for (final char argName : charArray) {
			if (!runForArgument(argName, a -> possiblePrefixes.add(a.getPrefix())))
				break;
		}

		return possiblePrefixes.size() >= 1 && possiblePrefixes.contains(str.charAt(0));
	}

	private boolean isArgName(String str) {
		// first try to figure out if the prefix is used, to save time (does it start with '--'? (assuming the prefix is '-'))
		if (
			str.length() > 1 // make sure we are working with long enough strings
				&& str.charAt(0) == str.charAt(1) // first and second chars are equal?
		)
		{
			// now check if the name actually exist
			return this.command.getArguments().stream().anyMatch(a -> a.checkMatch(str));
		}

		return false;
	}

	private boolean isArgumentSpecifier(String str) {
		return this.isArgName(str) || this.isArgNameList(str);
	}

	private boolean isSubCommand(String str) {
		return this.getSubCommands().stream().anyMatch(c -> c.name.equals(str));
	}

	private Command getSubCommandByName(String name) {
		var x = this.getSubCommands().stream().filter(sc -> sc.name.equals(name)).toList();
		return x.isEmpty() ? null : x.get(0);
	}

	private Command getTokenizedSubCommand() {
		return this.getSubCommands().stream().filter(sb -> sb.getTokenizer().finishedTokenizing).findFirst().orElse(null);
	}
}

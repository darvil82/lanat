package lanat.parsing;

import lanat.Argument;
import lanat.ArgumentType;
import lanat.Command;
import lanat.parsing.errors.Error;
import lanat.parsing.errors.handlers.ParseErrors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.Range;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

/**
 * Parses the tokens that have been tokenized from the CLI input.
 * <p>
 * This class is responsible for parsing the tokens and delegating the parsing of the values to the {@link ArgumentType}s
 * of the arguments that are being parsed.
 * </p>
 * When finished parsing, this class will contain a map of the arguments to their parsed values. This map can be accessed
 * by calling {@link Parser#getParsedArgsMap()}.
 */
public final class Parser extends ParsingStateBase<Error.ParseError> {
	/**
	 * Array of all the tokens that we have tokenized from the CLI arguments.
	 */
	private List<@NotNull Token> tokens;

	/**
	 * The index of the current token that we are parsing.
	 */
	private int currentTokenIndex = 0;

	/* number of positional arguments that have been parsed.
	 * if this becomes -1, then we know that we are no longer parsing positional arguments */
	private int positionalArgCount = 0;

	/**
	 * Whether we are currently parsing values in a tuple.
	 */
	private boolean isInTuple = false;

	/**
	 * The parsed arguments. This is a map of the argument to the value that it parsed. The reason this is saved is that
	 * we don't want to run {@link Parser#getParsedArgsMap()} multiple times because that can break stuff badly
	 * in relation to error handling.
	 */
	private HashMap<@NotNull Argument<?, ?>, @Nullable Object> cachedParsedArgumentValues;

	/** Contains the forward value if one was found. */
	private @Nullable String forwardValue;


	public Parser(@NotNull Command command) {
		super(command);
	}



	/** Returns the index of the current token that is being parsed. */
	public int getCurrentTokenIndex() {
		return this.currentTokenIndex;
	}

	/**
	 * Returns the forward value if one was found.
	 * @return The forward value, or {@code null} if there is no forward value.
	 */
	public @Nullable String getForwardValue() {
		return this.forwardValue;
	}

	/** Sets the tokens that this parser will parse. */
	public void setTokens(@NotNull List<@NotNull Token> tokens) {
		this.tokens = tokens;
	}

	/**
	 * Parses the tokens that have been set. Delegates parsing of argument values to the {@link ArgumentType} of the
	 * argument that is being parsed.
	 */
	public void parseTokens(@Nullable Parser previousParser) {
		assert this.tokens != null : "Tokens have not been set yet.";
		assert !this.hasFinished : "This parser has already finished parsing.";

		this.nestingOffset = previousParser == null
			? 0
			: previousParser.currentTokenIndex + previousParser.nestingOffset;

		Argument<?, ?> lastPositionalArgument; // this will never be null when being used

		for (this.currentTokenIndex = 0; this.currentTokenIndex < this.tokens.size(); ) {
			final Token currentToken = this.getCurrentToken();

			if (currentToken.type() == TokenType.ARGUMENT_NAME) {
				// we encountered an argument name, so we know that we are no longer parsing positional arguments
				this.positionalArgCount = -1;
				// encountered an argument name. first skip the token of the name.
				this.currentTokenIndex++;
				// find the argument that matches that name and let it parse the values
				this.runForMatchingArgument(currentToken.contents(), this::executeArgParse);
			} else if (currentToken.type() == TokenType.ARGUMENT_NAME_LIST) {
				// we encountered a name list, so we know that we are no longer parsing positional arguments
				this.positionalArgCount = -1;
				// in a name list, skip the first character because it is the indicator that it is a name list
				this.parseArgNameList(currentToken.contents().substring(1));
			} else if (
				(currentToken.type() == TokenType.ARGUMENT_VALUE || currentToken.type() == TokenType.ARGUMENT_VALUE_TUPLE_START)
					&& this.positionalArgCount != -1
					&& (lastPositionalArgument = this.getArgumentByPositionalIndex(this.positionalArgCount)) != null
			) {
				// if we are here we encountered an argument value with no prior argument name or name list,
				// so this must be a positional argument
				this.executeArgParse(lastPositionalArgument);
				this.positionalArgCount++;
			} else if (currentToken.type() == TokenType.COMMAND) {
				// encountered a command. first skip the token of the command.
				this.currentTokenIndex++;
				// find the command that matches that name and let it parse the values
				this.command.getCommand(currentToken.contents())
					.getParser()
					.parseTokens(this);
				break;
			} else if (currentToken.type() == TokenType.FORWARD_VALUE) {
				this.forwardValue = currentToken.contents();
				this.currentTokenIndex++;
			} else {
				this.addError(new ParseErrors.UnmatchedTokenError(this.currentTokenIndex));

				if (currentToken.type() == TokenType.ARGUMENT_VALUE)
					this.checkForSimilarArgumentName(currentToken.contents());

				this.currentTokenIndex++;
			}
		}

		this.hasFinished = true;
	}

	/**
	 * Reads the next tokens and parses them as values for the given argument.
	 * <p>
	 * This keeps in mind the type of the argument, and will stop reading tokens when it reaches the max number of
	 * values, or if the end of a tuple is reached.
	 * </p>
	 */
	private void executeArgParse(@NotNull Argument<?, ?> arg) {
		final Range argNumValuesRange = arg.type.getValueCountBounds();

		// just skip the whole thing if it doesn't need any values
		if (argNumValuesRange.isZero()) {
			this.argumentTypeParseValues(arg);
			return;
		}

		this.isInTuple = (
			this.currentTokenIndex < this.tokens.size()
				&& this.getCurrentToken().type() == TokenType.ARGUMENT_VALUE_TUPLE_START
		);

		final byte ifTupleOffset = (byte)(this.isInTuple ? 1 : 0);

		final ArrayList<Token> values = new ArrayList<>(argNumValuesRange.start());
		int numValues = 0;

		// add more values until we get to the max of the type, or we encounter another argument specifier
		for (
			int tokenIndex = this.currentTokenIndex + ifTupleOffset;
			tokenIndex < this.tokens.size();
			numValues++, tokenIndex++
		) {
			final Token currentToken = this.tokens.get(tokenIndex);

			if (this.isInTuple) {
				// if we reach the end of the tuple, finish.
				if (currentToken.type().isTuple())
					break;
			} else {
				// no more values to gather. we reached a non-value token, or we got the max number of values
				if (!currentToken.type().isValue() || numValues >= argNumValuesRange.end())
					break;
			}

			values.add(currentToken);
		}

		// add 2 if we are in a tuple, because we need to skip the start and end tuple tokens
		final int skipIndexCount = numValues + ifTupleOffset*2;

		if (numValues > argNumValuesRange.end() || numValues < argNumValuesRange.start()) {
			this.addIncorrectValueNumberError(arg, numValues, false);
			this.currentTokenIndex += skipIndexCount;
			return;
		}

		// pass the arg values to the argument sub parser
		this.argumentTypeParseValues(arg, ifTupleOffset, values.stream().map(Token::contents).toArray(String[]::new));

		this.currentTokenIndex += skipIndexCount;
	}

	/**
	 * Parses the given string as an argument value for the given argument.
	 * <p>
	 * If the value passed in is present (not empty or {@code null}), the argument should only require 0 or 1 values.
	 * </p>
	 */
	private void executeArgParse(@NotNull Argument<?, ?> arg, @Nullable String value) {
		final Range argumentValuesRange = arg.type.getValueCountBounds();

		// just skip the whole thing if it doesn't need any values
		if (argumentValuesRange.isZero()) {
			this.argumentTypeParseValues(arg);
			return;
		}

		if (argumentValuesRange.start() > 1) {
			this.addIncorrectValueNumberError(arg, 1, true);
			return;
		}

		if (value == null || value.isEmpty()) {
			this.executeArgParse(arg); // value is not present in the suffix of the argList. Continue parsing values.
			return;
		}
		
		// pass the arg values to the argument subParser
		this.argumentTypeParseValues(arg, value);
	}

	/**
	 * Parses the given string as a list of single-char argument names.
	 */
	private void parseArgNameList(@NotNull String args) {
		var doSkipToken = true;
		Argument<?, ?> lastArgument = null;

		// its multiple of them. We can only do this with arguments that accept 0 values.
		for (int i = 0; i < args.length(); i++) {
			var argument = this.getMatchingArgument(args.charAt(i));

			if (argument == null) {
				assert lastArgument != null; // we know for sure that lastArgument is not null here

				this.addError(new ParseErrors.UnmatchedInArgNameListError(
					this.currentTokenIndex, lastArgument, args.substring(i)
				));
				break;
			}

			// if the argument accepts 0 values, then we can just parse it like normal
			if (argument.type.getValueCountBounds().isZero()) {
				this.executeArgParse(argument);

				// -- arguments now may accept 1 or more values from now on:

				// if this argument is the last one in the list, then we can parse the next values after it
			} else if (i == args.length() - 1) {
				this.currentTokenIndex++;
				this.executeArgParse(argument);
				doSkipToken = false; // we don't want to skip the next token because executeArgParse already did that

				// if this argument is not the last one in the list, then we can parse the rest of the chars as the value
			} else {
				this.executeArgParse(argument, args.substring(i + 1));
				break;
			}

			lastArgument = argument;
		}

		if (doSkipToken) this.currentTokenIndex++;
	}

	/**
	 * Checks if the given string is similar to any of the argument names.
	 * <p>
	 * If so, add an error to the error list.
	 * @param str The string to check.
	 */
	private void checkForSimilarArgumentName(@NotNull String str) {
		// if the string is too short, don't bother checking
		if (str.length() < 2) return;

		char prefix = str.charAt(0);

		// check for the common prefixes
		Stream.of(Argument.Prefix.COMMON_PREFIXES)
			.map(Argument.Prefix::getCharacter)
			.forEach(checkPrefix -> {
				// if not present, don't bother checking
				if (prefix != checkPrefix) return;

				// get rid of the prefix (single or double)
				final var nameToCheck = Argument.removePrefix(str, checkPrefix);

				for (var arg : this.command.getArguments()) {
					if (!arg.hasName(nameToCheck)) continue; // does not have the name

					/* if the prefix is the same, then we know that the token was wrapped in quotes, since
					 * this token is somehow of type value */
					this.addError(new ParseErrors.SimilarArgumentError(
						this.currentTokenIndex, arg, arg.getPrefix().getCharacter() == prefix
					));
				}
			});
	}

	/** Returns the positional argument at the given index of declaration. */
	private @Nullable Argument<?, ?> getArgumentByPositionalIndex(int index) {
		var posArgs = this.command.getPositionalArguments();

		if (index >= posArgs.size())
			return null;

		return posArgs.get(index);
	}

	/**
	 * Returns a hashmap of Arguments and their corresponding parsed values.
	 * This function invokes the {@link Argument#finishParsing()} method on each argument the first time it is called.
	 * After that, it will return the same hashmap.
	 * */
	public @NotNull HashMap<@NotNull Argument<?, ?>, @Nullable Object> getParsedArgsMap() {
		if (this.cachedParsedArgumentValues == null) {
			this.cachedParsedArgumentValues = new HashMap<@NotNull Argument<?, ?>, @Nullable Object>();
			this.command.getArguments().forEach(arg -> this.cachedParsedArgumentValues.put(arg, arg.finishParsing()));
		}
		return this.cachedParsedArgumentValues;
	}

	private void argumentTypeParseValues(@NotNull Argument<?, ?> argument, @NotNull String... values) {
		this.argumentTypeParseValues(argument, 0, values);
	}

	private void argumentTypeParseValues(@NotNull Argument<?, ?> argument, int offset, @NotNull String... values) {
		argument.type.parseAndUpdateValue(
			new ArgumentType.ParseStateSnapshot(
				this.currentTokenIndex + offset, values.length,
				this.isInTuple, this.positionalArgCount != -1
			),
			values
		);
	}

	private @NotNull Token getCurrentToken() {
		return this.tokens.get(this.currentTokenIndex);
	}

	// ------------------------------------------------ Error Handling ------------------------------------------------
	private void addIncorrectValueNumberError(@NotNull Argument<?, ?> argument, int valueCount, boolean isInArgNameList) {
		this.addError(new ParseErrors.IncorrectValueNumberError(
			this.currentTokenIndex, argument, valueCount, isInArgNameList, this.isInTuple
		));
	}
}
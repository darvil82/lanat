package lanat.parsing;

import lanat.ArgValueCount;
import lanat.Argument;
import lanat.ArgumentType;
import lanat.Command;
import lanat.parsing.errors.CustomError;
import lanat.parsing.errors.ParseError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Parser extends ParsingStateBase<ParseError> {
	/**
	 * List of all the custom errors that have been added to this parser. Custom errors are thrown by
	 * {@link ArgumentType}s
	 */
	private final @NotNull ArrayList<@NotNull CustomError> customErrors = new ArrayList<>();

	/**
	 * Array of all the tokens that we have tokenized from the CLI arguments.
	 */
	private List<@NotNull Token> tokens;

	/**
	 * The index of the current token that we are parsing.
	 */
	private short currentTokenIndex = 0;

	/**
	 * The parsed arguments. This is a map of the argument to the value that it parsed. The reason this is saved is that
	 * we don't want to run {@link Parser#getParsedArgumentsHashMap()} multiple times because that can break stuff badly
	 * in relation to error handling.
	 */
	private HashMap<@NotNull Argument<?, ?>, @Nullable Object> parsedArguments;


	public Parser(@NotNull Command command) {
		super(command);
	}


	// ------------------------------------------------ Error Handling ------------------------------------------------
	@Override
	public boolean hasExitErrors() {
		return super.hasExitErrors() || this.anyErrorInMinimum(this.customErrors, false);
	}

	@Override
	public boolean hasDisplayErrors() {
		return super.hasDisplayErrors() || this.anyErrorInMinimum(this.customErrors, true);
	}

	public @NotNull List<@NotNull CustomError> getCustomErrors() {
		return this.getErrorsInLevelMinimum(this.customErrors, true);
	}

	public void addError(@NotNull ParseError.ParseErrorType type, @Nullable Argument<?, ?> arg, int argValueCount, int currentIndex) {
		this.addError(new ParseError(type, currentIndex, arg, argValueCount));
	}

	public void addError(@NotNull ParseError.ParseErrorType type, @Nullable Argument<?, ?> arg, int argValueCount) {
		this.addError(type, arg, argValueCount, this.currentTokenIndex);
	}

	public void addError(@NotNull CustomError customError) {
		this.customErrors.add(customError);
	}
	// ------------------------------------------------ ////////////// ------------------------------------------------

	/** Returns the index of the current token that is being parsed. */
	public short getCurrentTokenIndex() {
		return this.currentTokenIndex;
	}

	/** Sets the tokens that this parser will parse. */
	public void setTokens(@NotNull List<@NotNull Token> tokens) {
		this.tokens = tokens;
	}

	public void parseTokens() {
		if (this.tokens == null)
			throw new IllegalStateException("Tokens have not been set yet.");

		if (this.hasFinished)
			throw new IllegalStateException("This parser has already finished parsing.");


		short argumentNameCount = 0;
		boolean foundNonPositionalArg = false;
		Argument<?, ?> lastPosArgument; // this will never be null when being used

		for (this.currentTokenIndex = 0; this.currentTokenIndex < this.tokens.size(); ) {
			final Token currentToken = this.tokens.get(this.currentTokenIndex);

			if (currentToken.type() == TokenType.ARGUMENT_NAME) {
				this.currentTokenIndex++;
				this.runForArgument(currentToken.contents(), this::executeArgParse);
				foundNonPositionalArg = true;
			} else if (currentToken.type() == TokenType.ARGUMENT_NAME_LIST) {
				this.parseArgNameList(currentToken.contents().substring(1));
				foundNonPositionalArg = true;
			} else if (
				(currentToken.type() == TokenType.ARGUMENT_VALUE || currentToken.type() == TokenType.ARGUMENT_VALUE_TUPLE_START)
					&& !foundNonPositionalArg
					&& (lastPosArgument = this.getArgumentByPositionalIndex(argumentNameCount)) != null
			)
			{ // this is most likely a positional argument
				this.executeArgParse(lastPosArgument);
				argumentNameCount++;
			} else {
				this.currentTokenIndex++;
				if (currentToken.type() != TokenType.FORWARD_VALUE)
					this.addError(ParseError.ParseErrorType.UNMATCHED_TOKEN, null, 0);
			}
		}

		this.hasFinished = true;

		// now parse the subcommands
		this.getSubCommands().stream()
			.filter(sb -> sb.getTokenizer().isFinishedTokenizing()) // only get the commands that were actually tokenized
			.forEach(sb -> sb.getParser().parseTokens()); // now parse them
	}

	/**
	 * Reads the next tokens and parses them as values for the given argument.
	 * <p>
	 * This keeps in mind the type of the argument, and will stop reading tokens when it reaches the max number of
	 * values, or if the end of a tuple is reached.
	 * </p>
	 */
	private void executeArgParse(@NotNull Argument<?, ?> arg) {
		final ArgValueCount argumentValuesRange = arg.argType.getArgValueCount();

		// just skip the whole thing if it doesn't need any values
		if (argumentValuesRange.isZero()) {
			arg.parseValues();
			return;
		}

		final boolean isInTuple = (
			this.currentTokenIndex < this.tokens.size()
				&& this.tokens.get(this.currentTokenIndex).type() == TokenType.ARGUMENT_VALUE_TUPLE_START
		);

		final int ifTupleOffset = isInTuple ? 1 : 0;
		int skipCount = ifTupleOffset;

		final ArrayList<Token> tempArgs = new ArrayList<>();

		// add more values until we get to the max of the type, or we encounter another argument specifier
		for (
			int i = this.currentTokenIndex + ifTupleOffset;
			i < this.tokens.size();
			i++, skipCount++
		) {
			final Token currentToken = this.tokens.get(i);
			if (
				(!isInTuple && (
					currentToken.type().isArgumentSpecifier() || i - this.currentTokenIndex >= argumentValuesRange.max
				))
					|| currentToken.type().isTuple()
			)
			{
				break;
			}
			tempArgs.add(currentToken);
		}

		final int tempArgsSize = tempArgs.size();
		final int newCurrentTokenIndex = skipCount + ifTupleOffset;

		if (tempArgsSize > argumentValuesRange.max || tempArgsSize < argumentValuesRange.min) {
			this.addError(ParseError.ParseErrorType.ARG_INCORRECT_VALUE_NUMBER, arg, tempArgsSize + ifTupleOffset);
			this.currentTokenIndex += newCurrentTokenIndex;
			return;
		}

		// pass the arg values to the argument sub parser
		arg.parseValues(tempArgs.stream().map(Token::contents).toArray(String[]::new), (short)(this.currentTokenIndex + ifTupleOffset));

		this.currentTokenIndex += newCurrentTokenIndex;
	}

	/**
	 * Parses the given string as an argument value for the given argument.
	 * <p>
	 * If the value passed in is present (not empty or null), the argument should only require 0 or 1 values.
	 * </p>
	 */
	private void executeArgParse(@NotNull Argument<?, ?> arg, @Nullable String value) {
		final ArgValueCount argumentValuesRange = arg.argType.getArgValueCount();

		if (value == null || value.isEmpty()) {
			this.executeArgParse(arg); // value is not present in the suffix of the argList. Continue parsing values.
			return;
		}

		// just skip the whole thing if it doesn't need any values
		if (argumentValuesRange.isZero()) {
			arg.parseValues();
			return;
		}

		if (argumentValuesRange.min > 1) {
			this.addError(ParseError.ParseErrorType.ARG_INCORRECT_VALUE_NUMBER, arg, 0);
			return;
		}

		// pass the arg values to the argument subParser
		arg.parseValues(new String[] { value }, this.currentTokenIndex);
	}

	/**
	 * Parses the given string as a list of single-char argument names.
	 */
	private void parseArgNameList(@NotNull String args) {
		// its multiple of them. We can only do this with arguments that accept 0 values.
		for (short i = 0; i < args.length(); i++) {
			final short constIndex = i; // this is because the lambda requires the variable to be final

			if (!this.runForArgument(args.charAt(i), a -> {
				// if the argument accepts 0 values, then we can just parse it like normal
				if (a.argType.getArgValueCount().isZero()) {
					this.executeArgParse(a);

					// -- arguments now may accept 1 or more values from now on:

					// if this argument is the last one in the list, then we can parse the next values after it
				} else if (constIndex == args.length() - 1) {
					this.currentTokenIndex++;
					this.executeArgParse(a);

					// if this argument is not the last one in the list, then we can parse the rest of the chars as the value
				} else {
					this.executeArgParse(a, args.substring(constIndex + 1));
				}
			}))
				return;
		}
		this.currentTokenIndex++;
	}

	/** Returns the positional argument at the given index of declaration. */
	private @Nullable Argument<?, ?> getArgumentByPositionalIndex(short index) {
		final var posArgs = this.command.getPositionalArguments();

		for (short i = 0; i < posArgs.size(); i++) {
			if (i == index) {
				return posArgs.get(i);
			}
		}
		return null;
	}

	/** Returns a hashmap of Arguments and their corresponding parsed values. */
	public @NotNull HashMap<@NotNull Argument<?, ?>, @Nullable Object> getParsedArgumentsHashMap() {
		if (this.parsedArguments == null) {
			this.parsedArguments = new HashMap<>() {{
				Parser.this.getArguments().forEach(arg -> this.put(arg, arg.finishParsing()));
			}};
		}
		return this.parsedArguments;
	}
}

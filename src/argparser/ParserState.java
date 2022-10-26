package argparser;

import argparser.utils.Pair;
import argparser.utils.Result;

import java.util.ArrayList;

/**
 * An instance of this class represents the state of the parser while parsing the passed arguments.
 */
class ParserState {
	private final char[] cliArgs;
	private Command currentCommand;

	/**
	 * The characters that will represent the start and end of a tuple when tokenizing.
	 */
	private final Pair<Character, Character> tupleChars;

	/**
	 * Array of all the tokens that we have parsed from the CLI arguments.
	 */
	private Token[] tokens;

	/**
	 * The index of the current token that we are parsing.
	 */
	private short currentTokenIndex = 0;

	enum ParseErrorType {
		ArgumentNotFound,
		ArgNameListTakeValues,
		ObligatoryArgumentNotUsed,
		ArgIncorrectValueNumber
	}

	static class ParseResult<TReturn> extends Result<ParseErrorType, TReturn> {
		public ParseResult(boolean isCorrect, int pos, ParseErrorType reason, TReturn ret) {super(isCorrect, pos, reason, ret);}

		public ParseResult() {}

		public ParseResult<TReturn> correctByAny() {
			return (ParseResult<TReturn>)super.correctByAny();
		}

		public ParseResult<TReturn> correctByAll() {
			return (ParseResult<TReturn>)super.correctByAll();
		}

		public ParseResult<TReturn> addSubResult(ParseResult<TReturn> r) {
			return (ParseResult<TReturn>)super.addSubResult(r);
		}

		public static <TReturn> ParseResult<TReturn> CORRECT() {return new ParseResult<>(true, 0, null, null);}

		public static <TReturn> ParseResult<TReturn> CORRECT(TReturn ret) {
			return new ParseResult<>(true, 0, null, ret);
		}

		public static <TReturn> ParseResult<TReturn> ERROR(ParseErrorType reason) {
			return new ParseResult<>(false, 0, reason, null);
		}

		public static <TReturn> ParseResult<TReturn> ERROR(ParseErrorType reason, int value) {
			return new ParseResult<>(false, value, reason, null);
		}
	}

	public ParserState(String cliArgs, Command command, TupleCharacter tc) {
		this.cliArgs = cliArgs.toCharArray();
		this.tupleChars = tc.getCharPair();
		this.currentCommand = command;
	}

	private ArrayList<Argument<?, ?>> getCurrentArguments() {
		return this.currentCommand.getArguments();
	}

	private Argument<?, ?>[] getCurrentPositionalArguments() {
		return this.currentCommand.getPositionalArguments();
	}
}
package argparser;

import argparser.utils.Result;

enum ParseErrorType {
	ArgumentNotFound,
	ArgNameListTakeValues,
	ObligatoryArgumentNotUsed,
	UnmatchedToken, ArgIncorrectValueNumber
}


class ParseResult<TReturn> extends Result<ParseErrorType, TReturn> {
	public ParseResult(boolean isCorrect, int pos, ParseErrorType reason, TReturn ret) {super(isCorrect, pos, reason, ret);}

	public ParseResult() {}

	public ParseResult<TReturn> correctByAny() {
		return (ParseResult<TReturn>)super.correctByAny();
	}

	public ParseResult<TReturn> correctByAll() {
		return (ParseResult<TReturn>)super.correctByAll();
	}


	public <T> ParseResult<TReturn> addSubResult(ParseResult<T> r) {
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
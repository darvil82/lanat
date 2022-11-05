package argparser;

import argparser.utils.Pair;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Predicate;


public class ParseResult<TReturn> {
	protected boolean correct;
	public final short simpleValue;
	protected final ParseErrorType reason;
	protected final ArrayList<ParseResult<TReturn>> subResults = new ArrayList<>();
	protected TReturn returnValue;


	public ParseResult(boolean isCorrect, int pos, ParseErrorType reason, TReturn retVal) {
		this.correct = isCorrect;
		this.simpleValue = (short)pos;
		this.reason = reason;
		this.returnValue = retVal;
	}

	public ParseResult() {
		this(false, 0, null, null);
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


	public ParseResult<TReturn> correctByAny() {
		if (!this.subResults.isEmpty()) {
			this.correct &= this.subResults.stream().anyMatch(r -> r.correctByAny().correct);
		}
		return this;
	}

	public ParseResult<TReturn> correctByAll() {
		if (!this.subResults.isEmpty()) {
			this.correct &= this.subResults.stream().allMatch(r -> r.correctByAll().correct);
		}
		return this;
	}

	@SuppressWarnings("unchecked cast")
	public <T> ParseResult<TReturn> addSubResult(ParseResult<T> r) {
		this.subResults.add((ParseResult<TReturn>)r);
		return this;
	}

	public ArrayList<ParseResult<TReturn>> getSubResults() {
		return subResults;
	}

	/**
	 * Get the underlying value of this result.
	 * The callback supplied will be called if the value is null. The callback will take the current Result
	 * instance, and it should return a new value for the caller.
	 */
	public TReturn unpack(Function<ParseResult<TReturn>, TReturn> fn) {
		if (this.returnValue == null)
			return fn.apply(this);
		return this.unpack();
	}

	public TReturn unpack() {
		return this.returnValue;
	}

	public ParseResult<TReturn> setReturnValue(TReturn value) {
		this.returnValue = value;
		return this;
	}

	public ParseErrorType getReason() {
		return reason;
	}

	public boolean isCorrect() {
		return correct;
	}

	public ArrayList<TReturn> getPackedReturnValues() {
		var result = new ArrayList<TReturn>();
		if (this.returnValue != null) {
			result.add(this.returnValue);
		}
		for (var subResult : this.subResults) {
			result.addAll(subResult.getPackedReturnValues());
		}
		return result;
	}

	private Pair<Integer, ParseResult<TReturn>> getFirstMatchingSubResult(int level, Predicate<ParseResult<TReturn>> fn) {
		if (this.isCorrect()) {
			return null;
		}

		ArrayList<ParseResult<TReturn>> subresults = this.getSubResults();
		ParseResult<TReturn> sub;

		if (!subresults.isEmpty() && fn.test(sub = subresults.get(0))) {
			return sub.getFirstMatchingSubResult(level + 1, fn);
		}
		return new Pair<>(level, this);
	}

	/**
	 * Get the first result that matches the given predicate.
	 *
	 * @param fn The predicate to match.
	 * @return A pair with the nesting level of first result that matches the predicate, and the result itself.
	 * Returns null if no result matches the predicate.
	 */
	public Pair<Integer, ParseResult<TReturn>> getFirstMatchingSubResult(Predicate<ParseResult<TReturn>> fn) {
		return this.getFirstMatchingSubResult(0, fn);
	}

	@Override
	public String toString() {
		return "Result{" +
			"correct=" + correct +
			", simpleValue=" + simpleValue +
			", reason=" + reason +
			", returnValue=" + returnValue +
			'}';
	}
}
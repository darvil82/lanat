package argparser.exceptions;

import java.util.ArrayList;
import java.util.function.Function;

public class Result<TErrorEnum extends Enum<TErrorEnum>, TReturn> {
	protected boolean correct = false;
	public final short simpleValue;
	protected final TErrorEnum reason;
	protected final ArrayList<Result<TErrorEnum, TReturn>> subResults = new ArrayList<>();
	protected final TReturn returnValue;

	public Result(boolean isCorrect, int pos, TErrorEnum reason, TReturn retVal) {
		this.correct = isCorrect;
		this.simpleValue = (short)pos;
		this.reason = reason;
		this.returnValue = retVal;
	}

	public Result() {
		this(false, 0, null, null);
	}

	public Result<TErrorEnum, TReturn> correctByAny() {
		if (!this.subResults.isEmpty()) {
			this.correct = this.subResults.stream().anyMatch(r -> r.correctByAny().correct);
		}
		return this;
	}

	public Result<TErrorEnum, TReturn> correctByAll() {
		if (!this.subResults.isEmpty()) {
			this.correct = this.subResults.stream().allMatch(r -> r.correctByAll().correct);
		}
		return this;
	}

	public void addSubResult(Result<TErrorEnum, TReturn> r) {
		this.subResults.add(r);
	}

	/**
	 * Get the underlying value of this result.
	 * The callback supplied will be called if the value is null. The callback will take the current Result
	 * instance, and it should return a new value for the caller.
	 */
	public TReturn unpack(Function<Result<TErrorEnum, TReturn>, TReturn> fn) {
		if (this.returnValue == null)
			return fn.apply(this);
		return this.unpack();
	}

	public TReturn unpack() {
		return this.returnValue;
	}

	public TErrorEnum getReason() {
		return reason;
	}

	public boolean isCorrect() {
		return correct;
	}
}

package argparser.utils;

import java.util.function.Function;


public class Result<TOk, TErr> {
	protected boolean correct;
	protected TOk okValue;
	protected TErr errValue;


	private Result(TOk okValue, TErr errValue, boolean correct) {
		this.okValue = okValue;
		this.errValue = errValue;
		this.correct = correct;
	}

	public static <TOk, TErr> Result<TOk, TErr> ok(TOk value) {
		return new Result<>(value, null, true);
	}

	public static <TOk, TErr> Result<TOk, TErr> err(TErr value) {
		return new Result<>(null, value, false);
	}

	/**
	 * Get the underlying value of this result.
	 * The callback supplied will be called if the value is null. The callback will take the current Result
	 * instance, and it should return a new value for the caller.
	 */
	public TOk unpack(Function<Result<TOk, TErr>, TOk> fn) {
		if (this.okValue == null)
			return fn.apply(this);
		return this.unpack();
	}

	public TOk unpack() {
		return this.okValue;
	}

	public boolean isCorrect() {
		return correct;
	}

	public TErr getErrValue() {
		return errValue;
	}
}
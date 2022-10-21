package argparser.exceptions;

import java.util.ArrayList;

public class Result<TErrorEnum extends Enum<TErrorEnum>> {
	public boolean correct = false;
	public short position;
	public TErrorEnum reason;
	public ArrayList<Result<TErrorEnum>> subResults = new ArrayList<>();

	public Result(boolean isCorrect, int pos, TErrorEnum reason) {
		this.correct = isCorrect;
		this.position = (short)pos;
		this.reason = reason;
	}

	public Result() {}

	public Result<TErrorEnum> correctByAny() {
		if (!this.subResults.isEmpty()) {
			this.correct = this.subResults.stream().anyMatch(r -> r.correctByAny().correct);
		}
		return this;
	}

	public Result<TErrorEnum> correctByAll() {
		if (!this.subResults.isEmpty()) {
			this.correct = this.subResults.stream().allMatch(r -> r.correctByAll().correct);
		}
		return this;
	}

	public void addSubResult(Result<TErrorEnum> r) {
		this.subResults.add(r);
	}

}

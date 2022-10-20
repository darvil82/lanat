package argparser.exceptions;

import java.util.ArrayList;

public class ParseResult {
	public boolean correct = false;
	public short position;
	public String reason;
	public ArrayList<ParseResult> subResults = new ArrayList<>();

	public ParseResult(boolean isCorrect, int pos, String reason) {
		this.correct = isCorrect;
		this.position = (short)pos;
		this.reason = reason;
	}

	public ParseResult(int pos, String reason) {
		this(false, pos, reason);
	}

	public ParseResult(String reason) {
		this(false, 0, reason);
	}

	public ParseResult() {}

	public ParseResult correctByAny() {
		if (!this.subResults.isEmpty()) {
			this.correct = this.subResults.stream().anyMatch(r -> r.correctByAny().correct);
		}
		return this;
	}

	public ParseResult correctByAll() {
		if (!this.subResults.isEmpty()) {
			this.correct = this.subResults.stream().allMatch(r -> r.correctByAll().correct);
		}
		return this;
	}

	public void addSubResult(ParseResult r) {
		this.subResults.add(r);
	}

	public static final ParseResult CORRECT = new ParseResult(true, 0, null);
}

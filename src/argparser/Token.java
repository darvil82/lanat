package argparser;

enum TokenType {
	ArgumentAlias,
	ArgumentNameList,
	ArgumentValue,
	ArgumentValueTupleStart,
	ArgumentValueTupleEnd,
	String,
}

public record Token(TokenType type, String contents) {
	public boolean isArgumentSpecifier() {
		return this.type == TokenType.ArgumentAlias || this.type == TokenType.ArgumentNameList;
	}
}
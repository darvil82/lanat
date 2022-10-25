package argparser;

enum TokenType {
	ArgumentAlias,
	ArgumentNameList,
	ArgumentValue,
	ArgumentValueTupleStart,
	ArgumentValueTupleEnd,
	String, SubCommand,
}

public record Token(TokenType type, String contents) {
	public boolean isArgumentSpecifier() {
		return this.type == TokenType.ArgumentAlias || this.type == TokenType.ArgumentNameList;
	}
}
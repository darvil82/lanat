package lanat.test;

import lanat.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TestingParser extends ArgumentParser {
	public TestingParser(String programName, String description) {
		super(programName, description);
	}

	public TestingParser(String programName) {
		super(programName);
	}

	public TestingParser(@NotNull Class<? extends CommandTemplate> templateClass) {
		super(templateClass);
	}

	public List<String> parseGetErrors(String args) {
		return this.parse(CLInput.from(args)).getErrors();
	}

	@Override
	public @NotNull AfterParseOptions parse(@NotNull CLInput input) {
		return super.parse(input).withActions(AfterParseOptions.AfterParseActions::printErrors);
	}

	public @NotNull AfterParseOptions parse(@NotNull String input) {
		return this.parse(CLInput.from(input));
	}

	public @NotNull ParseResultRoot parseGetValues(@NotNull String args) {
		return this.parse(CLInput.from(args))
			.withActions(AfterParseOptions.AfterParseActions::printErrors)
			.getResult();
	}
}
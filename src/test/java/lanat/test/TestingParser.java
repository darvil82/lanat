package lanat.test;

import lanat.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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

	public @NotNull ParseResultRoot parseGetValues(@NotNull String args) {
		var res = this.parse(CLInput.from(args))
			.withActions(AfterParseOptions.AfterParseActions::printErrors)
			.getResult();

		assertNotNull(res, "The result of the parsing was null (Arguments have failed)");
		return res;
	}
}
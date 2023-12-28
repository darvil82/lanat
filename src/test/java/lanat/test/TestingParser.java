package lanat.test;

import lanat.ArgumentParser;
import lanat.CLInput;
import lanat.CommandTemplate;
import lanat.ParseResultRoot;
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

	public @NotNull ParseResultRoot parseGetValues(@NotNull String args) {
		var res = this.parse(CLInput.from(args)).printErrors().getResult();
		assertNotNull(res, "The result of the parsing was null (Arguments have failed)");
		return res;
	}
}
import lanat.Argument;
import lanat.ArgumentType;
import lanat.Command;
import lanat.NamedWithDescription;
import lanat.utils.ErrorCallbacks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class TestErrors extends UnitTests {
	private final HashMap<String, Object> correct = new HashMap<>();
	private final HashMap<String, Object> invalid = new HashMap<>();

	@BeforeEach
	public void clear() {
		this.correct.clear();
		this.invalid.clear();
	}

	private <T extends ErrorCallbacks<?, ?> & NamedWithDescription> T addCallbacks(T obj) {
		obj.setOnCorrectCallback((v) -> this.correct.put(obj.getName(), v));
		obj.setOnErrorCallback((a) -> this.invalid.put(obj.getName(), a));
		return obj;
	}

	private void assertOk(String name, Object correct) {
		assertEquals(this.correct.get(name), correct);
		assertNull(this.invalid.get(name));
	}

	private void assertErr(String name) {
		assertNull(this.correct.get(name));
		assertNotNull(this.invalid.get(name));
	}

	@Override
	protected void assertNotPresent(String name) {
		assertNull(this.correct.get(name));
		assertNull(this.invalid.get(name));
	}

	@Override
	public void setParser() {
		this.parser = this.addCallbacks(new TestingParser("TestCallbacks") {{
			this.setErrorCode(5);

			this.addArgument(TestErrors.this.addCallbacks(Argument.create("bool-arg", ArgumentType.BOOLEAN())));
			this.addArgument(TestErrors.this.addCallbacks(Argument.create("int-arg", ArgumentType.INTEGER())));
			this.addArgument(TestErrors.this.addCallbacks(Argument.create("counter", ArgumentType.COUNTER())));
			this.addArgument(TestErrors.this.addCallbacks(Argument.create("will-work", ArgumentType.FLOAT())));

			this.addSubCommand(TestErrors.this.addCallbacks(new Command("sub") {{
				this.addArgument(TestErrors.this.addCallbacks(Argument.create("will-fail", ArgumentType.FLOAT())));
				this.setErrorCode(2);
			}}));
		}});
	}

	@Test
	public void testArgumentCallbacks() {
		this.parser.parseArgs("--bool-arg --int-arg foo --will-work 55.0 sub --will-fail bar");
		this.assertOk("bool-arg", true);
		this.assertErr("int-arg");
		this.assertNotPresent("counter");
		this.assertOk("will-work", 55.0f);

		this.assertErr("will-fail");
	}

	@Test
	public void testCommandCallbacks() {
		this.parser.parseArgs("sub --will-fail bar");
		this.assertErr("will-fail");
		this.assertErr(this.parser.getName());
	}

	@Test
	public void testCommandsErrorCode() {
		this.parser.parseArgs("sub --will-fail bar");
		assertEquals(this.parser.getErrorCode(), 7);
	}
}

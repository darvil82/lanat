package lanat.test.units;

import lanat.Argument;
import lanat.Command;
import lanat.argumentTypes.CounterArgumentType;
import lanat.argumentTypes.FloatArgumentType;
import lanat.argumentTypes.IntegerArgumentType;
import lanat.test.TestingParser;
import lanat.test.UnitTests;
import lanat.utils.NamedWithDescription;
import lanat.utils.errors.ErrorCallbacks;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class TestErrors extends UnitTests {
	private final @NotNull HashMap<String, Object> correct = new HashMap<>();
	private final @NotNull HashMap<String, Object> invalid = new HashMap<>();

	@BeforeEach
	public void clear() {
		this.correct.clear();
		this.invalid.clear();
	}

	private <T extends ErrorCallbacks<?, ?> & NamedWithDescription> @NotNull T addCallbacks(@NotNull T obj) {
		obj.setOnOkCallback(v -> this.correct.put(obj.getName(), v));
		obj.setOnErrorCallback(a -> this.invalid.put(obj.getName(), a));
		return obj;
	}

	private void assertOk(@NotNull String name, @NotNull Object correct) {
		assertEquals(this.correct.get(name), correct);
		assertNull(this.invalid.get(name));
	}

	private void assertErr(@NotNull String name) {
		assertNull(this.correct.get(name));
		assertNotNull(this.invalid.get(name));
	}

	@Override
	protected void assertNotPresent(@NotNull String name) {
		assertNull(this.correct.get(name));
		assertNull(this.invalid.get(name));
	}

	@Override
	protected TestingParser setParser() {
		return this.addCallbacks(new TestingParser("TestCallbacks") {{
			this.setErrorCode(5);

			this.addArgument(TestErrors.this.addCallbacks(Argument.createOfBoolType("bool-arg").build()));
			this.addArgument(TestErrors.this.addCallbacks(Argument.create(new IntegerArgumentType(), "int-arg").build()));
			this.addArgument(TestErrors.this.addCallbacks(Argument.create(new CounterArgumentType(), "counter").build()));
			this.addArgument(TestErrors.this.addCallbacks(Argument.create(new FloatArgumentType(), "float").build()));

			this.addCommand(TestErrors.this.addCallbacks(new Command("sub") {{
				this.addArgument(TestErrors.this.addCallbacks(Argument.create(new FloatArgumentType(), "sub-float").build()));
				this.setErrorCode(2);
			}}));
		}});
	}

	@Test
	@DisplayName("Test the argument callbacks (onOk and onErr) (ArgumentCallbacksOption.NO_ERROR_IN_ARGUMENT)")
	public void testArgumentCallbacks$NoErrorInArg() {
		this.parser.setCallbackInvocationOption(Command.CallbacksInvocationOption.NO_ERROR_IN_ARGUMENT);
		this.parser.parseGetValues("--bool-arg --int-arg foo --float 55.0 sub --sub-float bar");

		this.assertOk("bool-arg", true);
		this.assertErr("int-arg");
		this.assertNotPresent("counter");
		this.assertOk("float", 55.0f);
		this.assertErr("sub-float");
	}

	@Test
	@DisplayName("Test the argument callbacks (onOk and onErr) (ArgumentCallbacksOption.(DEFAULT)))")
	public void testArgumentCallbacks() {
		this.parser.parseGetValues("--bool-arg --float foo sub --sub-float 5.23");

		this.assertNotPresent("bool-arg");
		this.assertNotPresent("counter");
		this.assertErr("float");
		this.assertNotPresent("sub-float");
	}

	@Test
	@DisplayName("Test the command callbacks (onOk and onErr)")
	public void testCommandCallbacks() {
		this.parser.parseGetValues("sub --sub-float bar");
		this.assertErr("sub-float");
		this.assertErr(this.parser.getName());
	}

	@Test
	@DisplayName("The error code must be the result of 5 | 2 = 7")
	public void testCommandsErrorCode() {
		this.parser.parseGetValues("sub --sub-float bar");
		assertEquals(this.parser.getErrorCode(), 7);
	}
}
import lanat.*;
import lanat.argumentTypes.Parseable;
import lanat.helpRepresentation.HelpFormatter;
import lanat.utils.Range;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ManualTests {
	public static void main(String[] args) {
		HelpFormatter.lineWrapMax = 110;
		HelpFormatter.debugLayout = true;


		enum Something {
			ONE, TWO, THREE
		}

		new TestingParser("Testing", "description for main parser") {{
			this.addArgument(Argument.create("testing", ArgumentType.FROM_PARSEABLE(new TestClass()))
				.description("some description")
					.obligatory()
				.onOk(value -> System.out.println("ok: " + value))
			);

			this.addGroup(new ArgumentGroup("my group", "some description for the group") {{
				this.addArgument(Argument.create("double", ArgumentType.TRY_PARSE(Double.class))
					.description("some description")
					.onOk(value -> System.out.println("ok: " + value))
				);

				this.addArgument(Argument.create("test type", new RestrictedDoubleAdder())
					.onOk(value -> System.out.println("ok: " + value))
				);
			}});


			this.addSubCommand(new Command("hello", "Some description for the command") {{
				this.addNames("hi", "hey");
				this.addArgument(Argument.create("world", ArgumentType.INTEGER_RANGE(5, 10))
					.description("a range between 5 and 10")
					.onOk(value -> System.out.println("ok: " + value))
				);
			}});
		}}.parseArgsExpectErrorPrint("--help");
	}
}


class TestClass implements Parseable<Integer> {

	@Override
	public @NotNull Range getRequiredArgValueCount() {
		return Range.ONE;
	}

	@Override
	public @Nullable Integer parseValues(@NotNull String @NotNull [] args) {
		return Integer.parseInt(args[0]);
	}

	@Override
	public @Nullable TextFormatter getRepresentation() {
		return null;
	}
}
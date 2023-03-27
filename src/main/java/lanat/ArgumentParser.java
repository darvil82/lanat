package lanat;


import fade.mirror.MClass;
import fade.mirror.MField;
import fade.mirror.filter.Filter;
import lanat.parsing.TokenType;
import lanat.parsing.errors.ErrorHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static fade.mirror.Mirror.mirror;

public class ArgumentParser extends Command {
	private boolean isParsed = false;
	private @Nullable String license;
	private @Nullable String version;


	public ArgumentParser(@NotNull String programName, @Nullable String description) {
		super(programName, description);
	}

	public ArgumentParser(@NotNull String programName) {
		this(programName, null);
	}

	public ArgumentParser(@NotNull Class<? extends CommandTemplate> templateClass) {
		super(templateClass);
	}

	/**
	 * Constructs a new {@link ArgumentParser} based on the given {@link CommandTemplate}, parses the given input,
	 * and populates the template with the parsed values.
	 * <p>
	 * This is basically a shortcut for the following code:
	 * <pre>{@code
	 * new ArgumentParser(clazz).parse(input).into(clazz);
	 * }</pre>
	 * <p>
	 * <h4>Example:</h4>
	 * This code:
	 * <pre>{@code
	 * MyTemplate parsed = new ArgumentParser(MyTemplate.class) {{
	 *     addCommand(new Command(MyTemplate.SubTemplate.class));
	 * }}
	 *     .parse(input)
	 *     .printErrors()
	 *     .exitIfErrors()
	 *     .into(MyTemplate.class);
	 * }</pre>
	 * <p>
	 * Is equivalent to this code:
	 * <pre>{@code
	 * MyTemplate parsed = ArgumentParser.parseFromInto(MyTemplate.class, input);
	 * }
	 * </pre>
	 * @param templateClass The class to use as a template.
	 * @param input The input to parse.
	 * @param options A consumer that can be used for configuring the parsing process.
	 * @return The parsed template.
	 * @param <T> The type of the template.
	 * @see #parseFromInto(Class, CLInput)
	 */
	public static <T extends CommandTemplate> @NotNull T parseFromInto(
		@NotNull Class<T> templateClass,
		@NotNull CLInput input,
		@NotNull Consumer<@NotNull AfterParseOptions> options
	) {
		final var argParser = new ArgumentParser(templateClass);

		// add all commands recursively
		ArgumentParser.parseFromInto$setCommands(templateClass, argParser);

		final AfterParseOptions opts = argParser.parse(input);
		options.accept(opts);

		return opts.into(templateClass);
	}

	/**
	 * Constructs a new {@link ArgumentParser} based on the given {@link CommandTemplate}, parses the given input,
	 * and populates the template with the parsed values.
	 * @param templateClass The class to use as a template.
	 * @param input The input to parse.
	 * @return The parsed template.
	 * @param <T> The type of the template.
	 */
	public static <T extends CommandTemplate>
	@NotNull T parseFromInto(@NotNull Class<T> templateClass, @NotNull CLInput input) {
		return ArgumentParser.parseFromInto(templateClass, input, opts -> opts.printErrors().exitIfErrors());
	}

	/**
	 * Adds all commands defined with {@link Command.Define} in the given class to the given parent command.
	 * This method is recursive and will add all sub-commands of the given class.
	 * @param templateClass The class to search for commands in.
	 * @param parentCommand The command to add the found commands to.
	 * @param <T> The type of the class to search for commands in.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends CommandTemplate>
	void parseFromInto$setCommands(@NotNull Class<T> templateClass, @NotNull Command parentCommand) {
		final var commandDefs = Arrays.stream(templateClass.getDeclaredClasses())
			.filter(c -> c.isAnnotationPresent(Command.Define.class))
			.filter(c -> Modifier.isStatic(c.getModifiers()))
			.filter(CommandTemplate.class::isAssignableFrom)
			.map(c -> (Class<? extends CommandTemplate>)c)
			.toList();

		for (var commandDef : commandDefs) {
			var command = new Command(commandDef);
			parentCommand.addCommand(command);
			ArgumentParser.parseFromInto$setCommands(commandDef, command);
		}
	}


	/**
	 * Parses the given command line arguments and returns a {@link ParsedArguments} object.
	 *
	 * @param input The command line arguments to parse.
	 */
	public @NotNull ArgumentParser.AfterParseOptions parse(@NotNull CLInput input) {
		if (this.isParsed) {
			// reset all parsing related things to the initial state
			this.resetState();
		}

		// pass the properties of this Sub-Command to its children recursively (most of the time this is what the user will want)
		this.passPropertiesToChildren();
		this.tokenize(input.args); // first. This will tokenize all Sub-Commands recursively
		var errorHandler = new ErrorHandler(this);
		this.parseTokens(); // same thing, this parses all the stuff recursively

		this.invokeCallbacks();

		this.isParsed = true;

		return new AfterParseOptions(errorHandler);
	}


	@Override
	@NotNull
	ParsedArgumentsRoot getParsedArguments() {
		return new ParsedArgumentsRoot(
			this,
			this.getParser().getParsedArgumentsHashMap(),
			this.subCommands.stream().map(Command::getParsedArguments).toList(),
			this.getForwardValue()
		);
	}

	private @Nullable String getForwardValue() {
		final var tokens = this.getFullTokenList();
		final var lastToken = tokens.get(tokens.size() - 1);

		if (lastToken.type() == TokenType.FORWARD_VALUE)
			return lastToken.contents();

		return null;
	}

	public @Nullable String getLicense() {
		return this.license;
	}

	public void setLicense(@NotNull String license) {
		this.license = license;
	}

	public @Nullable String getVersion() {
		return this.version;
	}

	public void setVersion(@NotNull String version) {
		this.version = version;
	}


	public class AfterParseOptions {
		private final List<@NotNull String> errors;
		private final int errorCode;

		private AfterParseOptions(ErrorHandler errorHandler) {
			this.errorCode = ArgumentParser.this.getErrorCode();
			this.errors = errorHandler.handleErrors();
		}

		public @NotNull List<@NotNull String> getErrors() {
			return this.errors;
		}

		public boolean hasErrors() {
			return this.errorCode != 0;
		}

		public AfterParseOptions printErrors() {
			for (var error : this.errors) {
				System.err.println(error);
			}
			return this;
		}

		public AfterParseOptions exitIfErrors() {
			if (this.errorCode != 0)
				System.exit(this.errorCode);

			return this;
		}

		public @NotNull ParsedArgumentsRoot getParsedArguments() {
			return ArgumentParser.this.getParsedArguments();
		}

		public <T extends CommandTemplate> T into(@NotNull Class<T> clazz) {
			return AfterParseOptions.into(mirror(clazz), this.getParsedArguments(), ArgumentParser.this);
		}


		@SuppressWarnings("unchecked")
		private static <T extends CommandTemplate> T into(
			@NotNull MClass<T> clazz,
			@NotNull ParsedArguments parsedArgs,
			@NotNull Command cmd
		) {
			final var ctor = clazz.getConstructor();

			if (ctor.isEmpty())
				throw new IllegalArgumentException("the given class does not have a public constructor without parameters");

			final T instance = ctor.get().invokeWithNoInstance();

			assert instance != null;

			clazz.getFields(
				Filter.forFields().withAnnotation(Argument.Define.class), MClass.IncludeSuperclasses.Yes
			).forEach(f -> {
				@SuppressWarnings("OptionalGetWithoutIsPresent") // we know that the field has the annotation (see above)
				final var annotation = f.getAnnotationOfType(Argument.Define.class).get();

				// get the name of the argument from the annotation or field name
				final String argName = annotation.names().length == 0 ? f.getName() : annotation.names()[0];

				final @Nullable Object parsedValue = parsedArgs.get(argName).get();

				// if the type of the field is a ParsedArgumentValue, wrap the value in it.
				// otherwise, just set the value
				((MField<Object>)f).setValue(
					instance,
					mirror(ParsedArgumentValue.class).isSuperclassOf(f.getType())
						? new ParsedArgumentValue<>(parsedValue)
						: parsedValue
				);
			});

			// now handle the sub-command attribute accessors (if any)
			clazz.getFields(Filter.forFields().withAnnotation(CommandTemplate.CommandAccessor.class))
				.forEach(f -> AfterParseOptions.into$handleCommandAccessor(instance, (MField<T>)f, parsedArgs, cmd));

			return instance;
		}

		private static <T extends CommandTemplate> void into$handleCommandAccessor(
			@NotNull T instance,
			@NotNull MField<T> field,
			@NotNull ParsedArguments parsedArgs,
			@NotNull Command cmd
		) {
			final MClass<T> fieldType = mirror(field.getType());

			if (!mirror(CommandTemplate.class).isSuperclassOf(fieldType))
				throw new IllegalArgumentException(
					"The field '" + field.getName() + "' is annotated with @CommandAccessor but is not of type CommandTemplate"
				);

			final Command.Define annotation = fieldType.getAnnotationOfType(Command.Define.class).orElseThrow(
				() -> new IllegalArgumentException(
					"The field '" + field.getName() + "' is annotated with @CommandAccessor but the type of the field is not annotated with @Command"
				)
			);
			final String cmdName = annotation.names().length == 0 ? field.getName() : annotation.names()[0];

			field.setValue(
				instance,
				AfterParseOptions.into(fieldType, parsedArgs.getSubParsedArgs(cmdName), cmd.getCommand(cmdName))
			);
		}
	}
}
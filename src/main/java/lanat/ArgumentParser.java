package lanat;

import lanat.exceptions.CommandTemplateException;
import lanat.exceptions.IncompatibleCommandTemplateType;
import lanat.parsing.TokenType;
import lanat.parsing.errors.ErrorHandler;
import lanat.utils.UtlMisc;
import lanat.utils.UtlReflection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;


/**
 * <h2>Argument Parser</h2>
 * <p>
 * Provides the ability to parse a command line input and later gather the values of the parsed arguments.
 */
public class ArgumentParser extends Command {
	private boolean isParsed = false;
	private @Nullable String license;
	private @Nullable String version;


	/**
	 * Creates a new command with the given name and description.
	 * @param programName The name of the command. This is the name the user will use to indicate that they want to use this
	 * 		   command.
	 * @param description The description of the command.
	 * @see #setDescription(String)
	 */
	public ArgumentParser(@NotNull String programName, @Nullable String description) {
		super(programName, description);
	}

	/**
	 * Creates a new command with the given name and no description. This is the name the user will use to
	 * indicate that they want to use this command.
	 * @param programName The name of the command.
	 */
	public ArgumentParser(@NotNull String programName) {
		this(programName, null);
	}

	/**
	 * Creates a new command based on the given {@link CommandTemplate}. This does not take Sub-Commands into account.
	 * If you want to add Sub-Commands, use {@link #from(Class)} instead.
	 * @param templateClass The class of the template to use.
	 */
	public ArgumentParser(@NotNull Class<? extends CommandTemplate> templateClass) {
		super(templateClass);
	}

	/**
	 * Constructs a new {@link ArgumentParser} based on the given {@link CommandTemplate}, taking Sub-Commands into
	 * account.
	 * @param templateClass The class of the {@link CommandTemplate} to use.
	 * @return A new {@link ArgumentParser} based on the given {@link CommandTemplate}.
	 * @see CommandTemplate
	 */
	public static ArgumentParser from(@NotNull Class<? extends CommandTemplate> templateClass) {
		final var argParser = new ArgumentParser(templateClass);

		// add all commands recursively
		ArgumentParser.from$setCommands(templateClass, argParser);

		return argParser;
	}

	/**
	 * Constructs a new {@link ArgumentParser} based on the given {@link CommandTemplate}, parses the given input, and
	 * populates the template with the parsed values.
	 * <p>
	 * This is basically a shortcut for the following code:
	 * <pre>{@code
	 * new ArgumentParser(clazz).parse(input).into(clazz);
	 * }</pre>
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
	 *
	 * @param templateClass The class to use as a template.
	 * @param input The input to parse.
	 * @param options A consumer that can be used for configuring the parsing process.
	 * @param <T> The type of the template.
	 * @return The parsed template.
	 * @see #parseFromInto(Class, CLInput)
	 * @see CommandTemplate
	 */
	public static <T extends CommandTemplate> @NotNull T parseFromInto(
		@NotNull Class<T> templateClass,
		@NotNull CLInput input,
		@NotNull Consumer<@NotNull AfterParseOptions> options
	)
	{
		final AfterParseOptions opts = ArgumentParser.from(templateClass).parse(input);
		options.accept(opts);

		return opts.into(templateClass);
	}

	/**
	 * Constructs a new {@link ArgumentParser} based on the given {@link CommandTemplate}, parses the given input, and
	 * populates the template with the parsed values.
	 *
	 * @param templateClass The class to use as a template.
	 * @param input The input to parse.
	 * @param <T> The type of the template.
	 * @return The parsed template.
	 * @see CommandTemplate
	 */
	public static <T extends CommandTemplate>
	@NotNull T parseFromInto(@NotNull Class<T> templateClass, @NotNull CLInput input) {
		return ArgumentParser.parseFromInto(
			templateClass,
			input,
			opts -> opts.printErrors().exitIfErrors().printHelpIfNoInput().exitIfNoInput()
		);
	}

	/**
	 * Adds all commands defined with {@link Command.Define} in the given class to the given parent command. This method
	 * is recursive and will add all sub-commands of the given class.
	 *
	 * @param templateClass The class to search for commands in.
	 * @param parentCommand The command to add the found commands to.
	 * @param <T> The type of the class to search for commands in.
	 */
	@SuppressWarnings("unchecked")
	private static <T extends CommandTemplate>
	void from$setCommands(@NotNull Class<T> templateClass, @NotNull Command parentCommand) {
		final var commandDefs = Arrays.stream(templateClass.getDeclaredClasses())
			.filter(c -> c.isAnnotationPresent(Command.Define.class))
			.filter(c -> Modifier.isStatic(c.getModifiers()))
			.filter(CommandTemplate.class::isAssignableFrom)
			.map(c -> (Class<? extends CommandTemplate>)c)
			.toList();

		for (var commandDef : commandDefs) {
			var command = new Command(commandDef);
			parentCommand.addCommand(command);
			ArgumentParser.from$setCommands(commandDef, command);
		}
	}


	/**
	 * Parses the given command line arguments and returns a {@link AfterParseOptions} object.
	 *
	 * @param input The command line arguments to parse.
	 * @see AfterParseOptions
	 */
	public @NotNull AfterParseOptions parse(@NotNull CLInput input) {
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

		return new AfterParseOptions(errorHandler, !input.isEmpty());
	}


	@Override
	@NotNull ParsedArgumentsRoot getParsedArguments() {
		return new ParsedArgumentsRoot(
			this,
			this.getParser().getParsedArgumentsHashMap(),
			this.getCommands().stream().map(Command::getParsedArguments).toList(),
			this.getForwardValue()
		);
	}

	/**
	 * Returns the forward value token (if any) of the full token list.
	 */
	private @Nullable String getForwardValue() {
		final var tokens = this.getFullTokenList();
		final var lastToken = tokens.get(tokens.size() - 1);

		if (lastToken.type() == TokenType.FORWARD_VALUE)
			return lastToken.contents();

		return null;
	}

	/**
	 * Sets the license of this program. By default, this is shown in the help message.
	 * @param license The license information to set.
	 */
	public void setLicense(@NotNull String license) {
		this.license = license;
	}

	/**
	 * Returns the license of this program.
	 * @see #setLicense(String)
	 */
	public @Nullable String getLicense() {
		return this.license;
	}

	/**
	 * Sets the version of this program. By default, this is shown in the help message.
	 * @param version The version information to set.
	 */
	public void setVersion(@NotNull String version) {
		this.version = version;
	}

	/**
	 * Adds a 'version' argument which shows the version of the program
	 * (provided by the {@link ArgumentParser#getVersion()} method).
	 */
	public void addVersionArgument() {
		this.addArgument(Argument.createOfBoolType("version")
			.onOk(t ->
				System.out.println("Version: " + UtlMisc.nonNullOrElse(this.getVersion(), "unknown"))
			)
			.withDescription("Shows the version of this program.")
			.allowsUnique()
		);
	}

	/**
	 * Returns the version of this program.
	 * @see #setVersion(String)
	 */
	public @Nullable String getVersion() {
		return this.version;
	}


	/**
	 * Provides utilities for the parsed arguments after parsing is done.
	 */
	public class AfterParseOptions {
		private final List<@NotNull String> errors;
		private final int errorCode;
		private final boolean receivedArguments;

		private AfterParseOptions(ErrorHandler errorHandler, boolean receivedArguments) {
			this.errorCode = ArgumentParser.this.getErrorCode();
			this.errors = errorHandler.handleErrors();
			this.receivedArguments = receivedArguments;
		}

		/**
		 * Returns a list of all the error messages that occurred during parsing.
		 */
		public @NotNull List<@NotNull String> getErrors() {
			return this.errors;
		}

		/**
		 * @see Command#getErrorCode()
		 */
		public int getErrorCode() {
			return this.errorCode;
		}

		/**
		 * Returns whether any errors occurred during parsing.
		 * @return {@code true} if any errors occurred, {@code false} otherwise.
		 */
		public boolean hasErrors() {
			return this.errorCode != 0;
		}

		/**
		 * Prints all errors that occurred during parsing to {@link System#err}.
		 */
		public AfterParseOptions printErrors() {
			for (var error : this.errors) {
				System.err.println(error);
			}
			return this;
		}

		/** Prints the help message to {@link System#out} if no arguments were passed to the program. */
		public AfterParseOptions printHelpIfNoInput() {
			if (!this.receivedArguments)
				System.out.println(ArgumentParser.this.getHelp());
			return this;
		}

		/**
		 * Exits the program with the error code returned by {@link #getErrorCode()} if any errors occurred during
		 * parsing.
		 */
		public AfterParseOptions exitIfErrors() {
			if (this.hasErrors())
				System.exit(this.errorCode);

			return this;
		}

		/** Exits the program with a code of {@code 0} if no arguments were passed to the program. */
		public AfterParseOptions exitIfNoInput() {
			if (!this.receivedArguments)
				System.exit(0);

			return this;
		}

		/**
		 * Returns a {@link ParsedArgumentsRoot} object that contains all the parsed arguments.
		 */
		public @NotNull ParsedArgumentsRoot getParsedArguments() {
			return ArgumentParser.this.getParsedArguments();
		}

		/**
		 * Instantiates the given Command Template class and sets all the fields annotated with {@link Argument.Define}
		 * corresponding to their respective parsed arguments.
		 * This method will also instantiate all the sub-commands recursively if defined in the template class properly.
		 * @param clazz The Command Template class to instantiate.
		 * @return The instantiated Command Template class.
		 * @param <T> The type of the Command Template class.
		 * @see CommandTemplate
		 */
		public <T extends CommandTemplate> T into(@NotNull Class<T> clazz) {
			return AfterParseOptions.into(clazz, this.getParsedArguments());
		}

		/**
		 * {@link #into(Class)} helper method.
		 * @param clazz The Command Template class to instantiate.
		 * @param parsedArgs The parsed arguments to set the fields of the Command Template class.
		 */
		private static <T extends CommandTemplate> T into(
			@NotNull Class<T> clazz,
			@NotNull ParsedArguments parsedArgs
		)
		{
			final T instance = UtlReflection.instantiate(clazz);

			Stream.of(clazz.getFields())
				.filter(f -> f.isAnnotationPresent(Argument.Define.class))
				.forEach(f -> {
					final var annotation = f.getAnnotation(Argument.Define.class);

					// get the name of the argument from the annotation or field name
					final String argName = annotation.names().length == 0 ? f.getName() : annotation.names()[0];

					final @NotNull Optional<?> parsedValue = parsedArgs.get(argName);

					try {
						// if the field has a value already set and the parsed value is empty, skip it (keep the old value)
						if (parsedValue.isEmpty() && f.get(instance) != null)
							return;

						// if the type of the field is an Optional, wrap the value in it.
						// otherwise, just set the value
						f.set(
							instance,
							f.getType().isAssignableFrom(Optional.class)
								? parsedValue
								: AfterParseOptions.into$getNewFieldValue(f, parsedValue)
						);
					} catch (IllegalArgumentException e) {
						if (parsedValue.isEmpty())
							throw new IncompatibleCommandTemplateType(
								"Field '" + f.getName() + "' of type '" + f.getType().getSimpleName() + "' does not"
									+ " accept null values, but the parsed argument '" + argName + "' is null"
							);

						throw new IncompatibleCommandTemplateType(
							"Field '" + f.getName() + "' of type '" + f.getType().getSimpleName() + "' is not "
								+ "compatible with the type (" + parsedValue.get().getClass().getSimpleName() + ") of the "
								+ "parsed argument '" + argName + "'"
						);

					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				});

			// now handle the sub-command field accessors (if any)
			final var declaredClasses = Stream.of(clazz.getDeclaredClasses())
				.filter(c -> c.isAnnotationPresent(Command.Define.class))
				.toList();

			for (var cls : declaredClasses) {
				final var field = Stream.of(clazz.getDeclaredFields())
					.filter(f -> f.isAnnotationPresent(CommandTemplate.CommandAccessor.class))
					.filter(f -> f.getType() == cls)
					.findFirst()
					.orElseThrow(() -> {
						throw new CommandTemplateException(
							"The class '" + cls.getSimpleName() + "' is annotated with @Command.Define but it's "
								+ "enclosing class does not have a field annotated with @CommandAccessor"
						);
					});

				AfterParseOptions.into$handleCommandAccessor(instance, field, parsedArgs);
			}

			return instance;
		}

		/**
		 * {@link #into(Class)} helper method. Handles the {@link CommandTemplate.CommandAccessor} annotation.
		 * @param parsedTemplateInstance The instance of the current Command Template class.
		 * @param commandAccesorField The field annotated with {@link CommandTemplate.CommandAccessor}.
		 * @param parsedArgs The parsed arguments to set the fields of the Command Template class.
		 */
		@SuppressWarnings("unchecked")
		private static <T extends CommandTemplate> void into$handleCommandAccessor(
			@NotNull T parsedTemplateInstance,
			@NotNull Field commandAccesorField,
			@NotNull ParsedArguments parsedArgs
		)
		{
			final Class<?> fieldType = commandAccesorField.getType();

			if (!CommandTemplate.class.isAssignableFrom(fieldType))
				throw new CommandTemplateException(
					"The field '" + commandAccesorField.getName() + "' is annotated with @CommandAccessor "
						+ "but its type is not a subclass of CommandTemplate"
				);

			final String cmdName = CommandTemplate.getTemplateNames((Class<? extends CommandTemplate>)fieldType)[0];

			try {
				commandAccesorField.set(parsedTemplateInstance,
					AfterParseOptions.into(
						(Class<T>)fieldType,
						parsedArgs.getSubParsedArgs(cmdName)
					)
				);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * {@link #into(Class)} helper method. Returns the new value for the given field based on the parsed value.
		 * If the parsed value is {@code null}, this method will return {@code null} as well.
		 * If both the field and the parsed value are arrays, this method will return a new array with the same type.
		 * @param commandAccesorField The field to get the new value for.
		 * @param parsedValue The parsed value to get the new value from.
		 * @return The new value for the given field based on the parsed value. This will be {@code null} if the parsed
		 *  value is {@code null}.
		 */
		private static Object into$getNewFieldValue(
			@NotNull Field commandAccesorField,
			@NotNull Optional<?> parsedValue
		) {
			if (parsedValue.isEmpty())
				return null;

			final Object value = parsedValue.get();

			if (!(commandAccesorField.getType().isArray() && value.getClass().isArray()))
				return value;


			// handle array types
			final var fieldType = commandAccesorField.getType().getComponentType();
			final var originalArray = (Object[])value; // to get rid of warnings

			// create a new array of the same type as the field.
			var newArray = (Object[])Array.newInstance(fieldType, Array.getLength(originalArray));
			// copy the values from the original array to the new array
			System.arraycopy(originalArray, 0, newArray, 0, originalArray.length);
			return newArray;
		}
	}
}
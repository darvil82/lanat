package lanat;

import io.github.darvil.utils.UtlReflection;
import lanat.exceptions.CommandTemplateException;
import lanat.exceptions.IncompatibleCommandTemplateTypeException;
import lanat.parsing.errors.ErrorCollector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Provides utilities for the parsed arguments after parsing is done.
 */
public class AfterParseOptions {
	private final @NotNull ArgumentParser argumentParser;
	private final @NotNull ErrorCollector errorCollector;
	private @Nullable List<@NotNull String> errors;
	private final int errorCode;
	/** Whether any input was received during parsing. */
	private final boolean receivedInput;
	private @NotNull Consumer<@NotNull AfterParseActions> actions = DEFAULT_ACTIONS;
	/** Whether the actions have been run already. */
	private boolean ranActions = false;

	public static final Consumer<@NotNull AfterParseActions> DEFAULT_ACTIONS = a -> a
		.printHelpIfNoInput()
		.exitIfNoInput()
		.printErrors()
		.exitIfErrors();


	AfterParseOptions(
		@NotNull ArgumentParser argumentParser,
		@NotNull ErrorCollector errorCollector,
		boolean receivedInput
	) {
		this.argumentParser = argumentParser;
		this.errorCollector = errorCollector;
		this.errorCode = argumentParser.getErrorCode();
		this.receivedInput = receivedInput;
	}

	/**
	 * Runs all the actions that need to be done before the parse result is returned.
	 * <p>
	 * This method will only run the actions once. If it's called again, it will be skipped.
	 */
	private void runActions() {
		// if the actions have already been run, skip this
		if (this.ranActions)
			return;

		this.actions.accept(new AfterParseActions());
		this.argumentParser.invokeCallbacks();
		this.ranActions = true;
	}

	/**
	 * Returns a list of all the error messages that occurred during parsing.
	 */
	public @NotNull List<@NotNull String> getErrors() {
		if (this.errors == null)
			this.errors = this.errorCollector.handleErrors();
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
	 *
	 * @return {@code true} if any errors occurred, {@code false} otherwise.
	 */
	public boolean hasErrors() {
		return this.errorCode != 0;
	}

	/**
	 * Returns whether any input was received during parsing.
	 * @return {@code true} if any input was received, {@code false} otherwise.
	 */
	public boolean hasReceivedInput() {
		return this.receivedInput;
	}

	/**
	 * Sets the actions to be executed after parsing is done. The actions will be executed in the order they are
	 * specified in the method chain.
	 * <p>
	 * By default, the actions are:
	 * <ul>
	 * <li>Print the help message to {@link System#out} if no arguments were passed to the program.</li>
	 * <li>Exit the program with a code of {@code 0} if no arguments were passed to the program.</li>
	 * <li>Print all errors to {@link System#err}.</li>
	 * <li>
	 *     Exit the program with the error code returned by {@link #getErrorCode()} if any errors occurred during
	 *     parsing.
	 * </li>
	 * </ul>
	 * @param actions The actions to be executed after parsing is done.
	 */
	public AfterParseOptions withActions(@NotNull Consumer<@NotNull AfterParseActions> actions) {
		this.actions = actions;
		return this;
	}

	/**
	 * Returns a {@link ParseResultRoot} object that contains all the parsed arguments.
	 * <p>
	 * This method will run all the operations specified with {@link #withActions(Consumer)} before returning the
	 * result.
	 */
	public @NotNull ParseResultRoot getResult() {
		this.runActions();
		return this.argumentParser.getParseResult();
	}

	/**
	 * Instantiates the given Command Template class and sets all the fields annotated with {@link Argument.Define}
	 * corresponding to their respective parsed arguments. This method will also instantiate all the sub-commands
	 * recursively if defined in the template class properly.
	 * <p>
	 * This method will run all the operations specified with {@link #withActions(Consumer)} before returning the
	 * result.
	 * @param clazz The Command Template class to instantiate.
	 * @param <T> The type of the Command Template class.
	 * @return The instantiated Command Template class.
	 * @see CommandTemplate
	 */
	public <T extends CommandTemplate> T into(@NotNull Class<T> clazz) {
		var ret = AfterParseOptions.into(clazz, this.argumentParser.getParseResult());
		this.runActions(); // make sure to run the actions after instantiating the template
		return ret;
	}

	/**
	 * {@link #into(Class)} helper method.
	 *
	 * @param templateClass The Command Template class to instantiate.
	 * @param parseResult The parsed arguments to set the fields of the Command Template class.
	 */
	private static <T extends CommandTemplate> T into(
		@NotNull Class<T> templateClass,
		@NotNull ParseResult parseResult
	)
	{
		final T instance = UtlReflection.instantiate(templateClass);

		// set the values of the fields
		Stream.of(templateClass.getFields())
			.filter(f -> f.isAnnotationPresent(Argument.Define.class))
			.forEach(field -> AfterParseOptions.into$setFieldValue(field, parseResult, instance));

		// now handle the sub-command field accessors (if any)
		Stream.of(templateClass.getDeclaredClasses())
			.filter(c -> c.isAnnotationPresent(Command.Define.class))
			.forEach(cmdDef -> {
				var commandAccesorField = Stream.of(templateClass.getDeclaredFields())
					.filter(f -> f.isAnnotationPresent(CommandTemplate.CommandAccessor.class))
					.filter(f -> f.getType() == cmdDef)
					.findFirst()
					.orElseThrow(() -> new CommandTemplateException(
						"The class '" + cmdDef.getSimpleName() + "' is annotated with @Command.Define but it's "
							+ "enclosing class does not have a field annotated with @CommandAccessor"
					));

				AfterParseOptions.into$handleCommandAccessor(instance, commandAccesorField, parseResult);
			});

		instance.afterInstantiation(parseResult);
		return instance;
	}

	/**
	 * {@link #into(Class)} helper method. Sets the value of the given field based on the parsed arguments.
	 *
	 * @param field The field to set the value of.
	 * @param parseResult The parsed arguments to set the field value from.
	 * @param instance The instance of the current Command Template class.
	 * @param <T> The type of the Command Template class.
	 */
	private static <T extends CommandTemplate> void into$setFieldValue(
		@NotNull Field field,
		@NotNull ParseResult parseResult,
		@NotNull T instance
	)
	{
		final var annotation = field.getAnnotation(Argument.Define.class);

		// get the name of the argument from the annotation or field name
		final String argName = annotation.names().length == 0 ? field.getName() : annotation.names()[0];

		final @NotNull Optional<?> parsedValue = parseResult.get(argName);

		try {
			// if the field has a value already set and the parsed value is empty, skip it (keep the old value)
			if (parsedValue.isEmpty() && field.get(instance) != null)
				return;

			// if the type of the field is an Optional, wrap the value in it.
			// otherwise, just set the value
			field.set(
				instance,
				field.getType().isAssignableFrom(Optional.class)
					? parsedValue
					: AfterParseOptions.into$getNewFieldValue(field, parsedValue)
			);
		} catch (IllegalArgumentException e) {
			if (parsedValue.isEmpty())
				throw new IncompatibleCommandTemplateTypeException(
					"For field of argument '" + argName + "':\n"
						+ "Field '" + field.getName() + "' of type '" + field.getType().getSimpleName() + "' does not"
						+ " accept null values, but the parsed value is null"
				);

			throw new IncompatibleCommandTemplateTypeException(
				"For field of argument '" + argName + "':\n"
					+ "Field '" + field.getName() + "' of type '" + field.getType().getSimpleName() + "' is not "
					+ "compatible with the type (" + parsedValue.get().getClass().getSimpleName() + ")"
			);
		} catch (IncompatibleCommandTemplateTypeException e) {
			throw new IncompatibleCommandTemplateTypeException("For field of argument '" + argName + "':\n" + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@link #into(Class)} helper method. Handles the {@link CommandTemplate.CommandAccessor} annotation.
	 *
	 * @param parsedTemplateInstance The instance of the current Command Template class.
	 * @param commandAccessorField The field annotated with {@link CommandTemplate.CommandAccessor}.
	 * @param parseResult The parsed arguments to set the fields of the Command Template class.
	 */
	@SuppressWarnings("unchecked")
	private static <T extends CommandTemplate> void into$handleCommandAccessor(
		@NotNull T parsedTemplateInstance,
		@NotNull Field commandAccessorField,
		@NotNull ParseResult parseResult
	)
	{
		final Class<?> fieldType = commandAccessorField.getType();

		if (!CommandTemplate.class.isAssignableFrom(fieldType))
			throw new CommandTemplateException(
				"The field '" + commandAccessorField.getName() + "' is annotated with @CommandAccessor "
					+ "but its type is not a subclass of CommandTemplate"
			);

		final String cmdName = CommandTemplate.getTemplateNames((Class<? extends CommandTemplate>)fieldType)[0];

		try {
			commandAccessorField.set(parsedTemplateInstance,
				AfterParseOptions.into(
					(Class<T>)fieldType,
					parseResult.getSubResult(cmdName)
				)
			);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@link #into(Class)} helper method. Returns the new value for the given field based on the parsed value. If the
	 * parsed value is {@code null}, this method will return {@code null} as well. If both the field and the parsed
	 * value are arrays, this method will return a new array with the same type.
	 *
	 * @param commandAccessorField The field to get the new value for.
	 * @param parsedValue The parsed value to get the new value from.
	 * @return The new value for the given field based on the parsed value. This will be {@code null} if the parsed
	 * 	value is {@code null}.
	 */
	private static @Nullable Object into$getNewFieldValue(
		@NotNull Field commandAccessorField,
		@NotNull Optional<?> parsedValue
	)
	{
		if (parsedValue.isEmpty())
			return null;

		final Object value = parsedValue.get();

		if (!(commandAccessorField.getType().isArray() && value.getClass().isArray()))
			return value;


		// handle array types
		final var fieldType = commandAccessorField.getType().getComponentType();
		final var originalArray = (Object[])value; // to get rid of warnings

		try {
			// create a new array of the same type as the field.
			var newArray = (Object[])Array.newInstance(fieldType, originalArray.length);

			// copy the values from the original array to the new array
			System.arraycopy(originalArray, 0, newArray, 0, originalArray.length);

			return newArray;
		} catch (ClassCastException e) {
			throw new IncompatibleCommandTemplateTypeException(
				"Field '" + commandAccessorField.getName() + "' with array type '" + commandAccessorField.getType().getSimpleName()
				+ "' is not compatible with the array type (" + fieldType.arrayType() + ") of the parsed argument"
			);
		}
	}

	/**
	 * Provides actions to be executed after parsing is done.
	 * <p>
	 * This class cannot be instantiated directly. Use {@link AfterParseOptions#withActions(Consumer)} to set
	 * the actions to be executed after parsing is done.
	 */
	public final class AfterParseActions {
		private AfterParseActions() {}

		/**
		 * Prints all errors that occurred during parsing to {@link System#err}.
		 */
		public AfterParseActions printErrors() {
			for (var error : AfterParseOptions.this.getErrors()) {
				System.err.println(error);
			}
			return this;
		}

		/** Prints the help message to {@link System#out} if no arguments were passed to the program. */
		public AfterParseActions printHelpIfNoInput() {
			if (!AfterParseOptions.this.receivedInput)
				System.out.println(AfterParseOptions.this.argumentParser.getHelp());
			return this;
		}

		/**
		 * Exits the program with the error code returned by {@link #getErrorCode()} if any errors occurred during parsing.
		 */
		public AfterParseActions exitIfErrors() {
			if (AfterParseOptions.this.hasErrors())
				System.exit(AfterParseOptions.this.errorCode);
			return this;
		}

		/** Exits the program with a code of {@code 0} if no arguments were passed to the program. */
		public AfterParseActions exitIfNoInput() {
			if (!AfterParseOptions.this.receivedInput)
				System.exit(0);
			return this;
		}
	}
}
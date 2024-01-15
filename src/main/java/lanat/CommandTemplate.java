package lanat;

import lanat.exceptions.ArgumentNotFoundException;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;


/**
 * <h2>Command Template</h2>
 * Define the arguments and attributes of a command by defining the structure of a class.
 * <br><br>
 * <h3>Creating a Template</h3>
 * <p>
 * In order to define a new Command Template, create a new class that extends {@link CommandTemplate} and annotate it
 * with {@link Command.Define}. The class must be public and static.
 * </p>
 * <p>
 * A Command Template can inherit from another Command Template, which will also inherit the arguments and sub-commands
 * of the parent. Command Templates are initialized from the parent to the child, so the parent's arguments will be
 * initialized before the child's.
 * </p>
 *
 * <strong>Example:</strong>
 * <pre>{@code
 * @Command.Define(description = "My cool program")
 * public class MyProgram extends CommandTemplate {
 *   // ...
 * }
 * }</pre>
 *
 * <h4>Defining the Arguments</h4>
 * To define an argument that will belong to the command, create a public field with the
 * type that the parsed argument value should be. Annotate it with {@link Argument.Define}.
 * <p>
 * The name of the argument may be specified in the annotation with the {@link Argument.Define#names()} parameter.
 * If no name is specified, the name of the field will be used.
 * </p>
 * <p>
 * The type of the argument (that extends {@link ArgumentType}) may be specified in the annotation with the
 * {@link Argument.Define#argType()} parameter. Note that any type specified in the annotation must have a public,
 * no-argument constructor. If the Argument Type to use has a constructor with arguments, the type must be then
 * specified in {@link CommandTemplate#beforeInit(CommandBuildHelper)} instead, by setting
 * {@link ArgumentBuilder#withArgType(ArgumentType)} to the argument builder corresponding to the argument
 * being defined.
 * </p>
 * <p>
 * If no Argument Type is specified on the annotation, the Argument Type will be attempted to be inferred from the
 * field type if possible, which is the case for some built-in types, such as
 * {@link String}, {@link Integer}, {@link java.io.File}, etc.
 * </p>
 *
 * <strong>Example:</strong>
 * <pre>{@code
 * @Command.Define
 * public class ParentCommand extends CommandTemplate {
 *   @Argument.Define(names = {"name", "n"}, argType = StringArgumentType.class)
 *   public String name;
 *
 *   @Argument.Define // name: "numbers". argType: MultipleNumbersArgumentType<Integer>
 *   public Integer[] numbers;
 *
 *   @Argument.Define(required = true) // name: "file". argType: NumberRangeArgumentType<Integer>
 *   public Integer number;
 *
 *   @InitDef
 *   public static void beforeInit(CommandBuildHelper helper) {
 *      // set the argument type to NumberRangeArgumentType
 *      helper.<NumberRangeArgumentType<Integer>, Integer>getArgument("number")
 *         .withArgType(new NumberRangeArgumentType<>(0, 10);
 *   }
 * }}</pre>
 *
 * <h4>Defining Sub-Commands</h4>
 * <p>
 * To define a sub-command, create a new inner class inside the parent Command Template class already defined
 * (with same rules as for any Command Template class).
 * Then, create a public field in the parent command template class with the type of the sub-command
 * Command Template just created. Annotate it with {@link CommandAccessor}. This will properly link the sub-command
 * to the parent command when the parent command is initialized.
 * </p>
 * <strong>Example:</strong>
 * <pre>{@code
 * @Command.Define
 * public class ParentCommand extends CommandTemplate {
 *   @CommandAccessor
 *   public SubCommand subCommand;
 *
 *   public static class SubCommand extends CommandTemplate {
 *      // ...
 *   }
 * }
 * }</pre>
 *
 * <h4>Other actions</h4>
 * <p>
 * In order to configure the command more precisely, two public static methods with the {@link InitDef} annotation
 * may be defined in the Command Template class:
 * </p>
 * <ul>
 *     <li>{@link CommandTemplate#beforeInit(CommandBuildHelper)}: Called before adding the Arguments to the Command.</li>
 *     <li>{@link CommandTemplate#afterInit(Command)}: Called after the Command is initialized.</li>
 * </ul>
 * @see CommandTemplate.Default
 */
@Command.Define
public abstract class CommandTemplate {
	/** The parsed arguments of the command. */
	private ParseResult parseResult;

	/**
	 * Called right after the Command Template is instantiated by the Argument Parser.
	 * Sets the {@link #parseResult} field and calls {@link #onValuesReceived()} if the command was used.
	 * <p>
	 * The reason this is used instead of a constructor is because we don't want to force inheritors to call
	 * {@code super()} in their constructors. Also, this method is called first by the innermost Command in
	 * the hierarchy, and then by the parent Commands.
	 * @param parseResult The parsed arguments of the command.
	 */
	void afterInstantiation(@NotNull ParseResult parseResult) {
		this.parseResult = parseResult;
		if (this.wasUsed()) this.onValuesReceived();
	}

	/**
	 * Returns the {@link ParseResult} instance used by this Command Template. This instance is the one that was
	 * used to initialize this Command Template.
	 * @return The {@link ParseResult} instance used by this Command Template.
	 */
	public final @NotNull ParseResult getParseResult() {
		if (this.parseResult == null)
			throw new IllegalStateException("Command Template was not properly initialized by the Argument Parser.");
		return this.parseResult;
	}

	/**
	 * Returns the {@link Command} instance used by this Command Template. This instance is the one that was
	 * used to initialize this Command Template.
	 * @return The {@link Command} instance used by this Command Template.
	 */
	public final @NotNull Command getCommand() {
		return this.getParseResult().getCommand();
	}

	/**
	 * Returns {@code true} if the Command of this Template was used in the command line.
	 * @return {@code true} if the Command of this Template was used in the command line, {@code false} otherwise.
	 */
	public final boolean wasUsed() {
		return this.getParseResult().wasUsed();
	}

	/**
	 * Called when the Command of this Template is used in the command line.
	 * This method is called after the parsed values are set.
	 * <p>
	 * This method may be overridden to perform actions when the command is used.
	 * </p>
	 * This method should not be called manually. It is called automatically by the Argument Parser once the Command
	 * Template is initialized.
	 */
	public void onValuesReceived() {}


	/**
	 * Annotation used to define an init method for a Command Template.
	 * @see CommandTemplate#beforeInit(CommandBuildHelper)
	 * @see CommandTemplate#afterInit(Command)
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	protected @interface InitDef {}

	/**
	 * Annotation used to define a sub-command field in a Command Template.
	 * @see CommandTemplate
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	protected @interface CommandAccessor {}

	/**
	 * Helper class that contains the command being initialized and the list of argument builders that may be altered.
	 * @param cmd The command being initialized.
	 * @param args The list of argument builders that may be altered. Use {@link CommandBuildHelper#arg(String)}
	 * 		   to get the argument builder corresponding to an argument with a given name.
	 */
	public record CommandBuildHelper(@NotNull Command cmd, @NotNull List<ArgumentBuilder<?, ?>> args) {
		/**
		 * Returns the argument builder corresponding to the argument with the given name.
		 * This is a helper method to get the argument builder from the list of argument builders ({@link #args}).
		 * @param name The name of the argument.
		 * @return The argument builder corresponding to the argument with the given name.
		 * @param <T> The type of the argument.
		 * @param <TInner> The type of the value passed to the argument.
		 * @throws ArgumentNotFoundException If there is no argument with the given name.
		 */
		@SuppressWarnings("unchecked")
		public <T extends ArgumentType<TInner>, TInner>
		ArgumentBuilder<T, TInner> arg(@NotNull String name) {
			return (ArgumentBuilder<T, TInner>)this.args.stream()
				.filter(a -> a.hasName(name))
				.findFirst()
				.orElseThrow(() -> new ArgumentNotFoundException(name));
		}
	}

	// Dummy methods so that we prevent the user from creating an instance method with the same name.

	/**
	 * This method is called after the Command Template builder reads all the field arguments defined.
	 * This is before the Arguments are instantiated and finally added to the command.
	 * <p>
	 * <strong>Example:</strong>
	 * <pre>{@code
	 * @Command.Define
	 * public class ParentCommand extends CommandTemplate {
	 *   @Argument.Define
	 *   public Integer numberRange;
	 *
	 *   @InitDef
	 *   public static void beforeInit(CommandBuildHelper helper) {
	 *      // set the argument type to NumberRangeArgumentType
	 *      helper.<NumberRangeArgumentType<Integer>, Integer>getArgument("numberRange")
	 * 			.withArgType(new NumberRangeArgumentType<>(0, 10);
	 *   }
	 * }
	 * }</pre>

	 * @param helper A helper object that contains the command being initialized and the list of argument builders that may
	 * 		  be altered.
	 */
	@InitDef
	public static void beforeInit(@NotNull CommandBuildHelper helper) {}

	/**
	 * This method is called after the Command is initialized. This is after the Arguments are instantiated and added
	 * to the command. This method may be used to create groups of arguments, for example.
	 * @param cmd The command that was fully initialized.
	 */
	@InitDef
	public static void afterInit(@NotNull Command cmd) {}

	/**
	 * Returns the names of the command template. If no names are specified in the annotation, the simple name of the
	 * class will be used.
	 * <strong>Note: </strong> Expects the field to be annotated with {@link Command.Define}
	 *
	 * @param cmdTemplate The command template class. Must be annotated with {@link Command.Define}.
	 * @return The names of the command template.
	 */
	public static @NotNull String @NotNull [] getTemplateNames(@NotNull Class<? extends CommandTemplate> cmdTemplate) {
		final var annotation = cmdTemplate.getAnnotation(Command.Define.class);
		assert annotation != null : "Command Template class must be annotated with @Command.Define";

		final var annotationNames = annotation.names();

		// if no names are specified, use the simple name of the class
		return annotationNames.length == 0 ?
			new String[] { cmdTemplate.getSimpleName() }
			: annotationNames;
	}

	/**
	 * A default command template that adds the 'help' and 'version' arguments to the command.
	 * @see Command#addHelpArgument(int)
	 * @see ArgumentParser#addVersionArgument(int)
	 */
	@Command.Define
	public static class Default extends CommandTemplate {
		/*
		 * The reason we add these arguments here is so that they do not "physically" appear in the
		 * actual class that extends this one. 'help' and 'version' are just
		 * arguments that execute actions, and they don't really provide any useful values.
		 */
		@InitDef
		public static void afterInit(@NotNull Command cmd) {
			cmd.addHelpArgument(0);

			if (cmd instanceof ArgumentParser ap)
				ap.addVersionArgument(0);
		}
	}
}
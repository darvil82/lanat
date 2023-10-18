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
 *   @Argument.Define(argType = IntegerArgumentType.class, required = true)
 *   public Integer number;
 * }
 * }</pre>
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
	 *   public static void beforeInit(CommandBuildHelper cmdBuildHelper) {
	 *      // set the argument type to NumberRangeArgumentType
	 *      cmdBuildHelper.<NumberRangeArgumentType<Integer>, Integer>getArgument("numberRange")
	 * 			.withArgType(new NumberRangeArgumentType<>(0, 10);
	 *   }
	 * }
	 * }</pre>

	 * @param cmdBuildHelper A helper object that contains the command being initialized and the list of argument builders that may
	 * 		  be altered.
	 */
	@InitDef
	public static void beforeInit(@NotNull CommandBuildHelper cmdBuildHelper) {}

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
	 * @see Command#addHelpArgument()
	 * @see ArgumentParser#addVersionArgument()
	 */
	@Command.Define
	public static class Default extends CommandTemplate {
		/*
		 * The reason we add these arguments here is so that they do not "physically" appear in the
		 * actual class that extends this one. 'help' and 'version' are just
		 * arguments that execute actions, and they not really provide any useful values.
		 */
		@InitDef
		public static void afterInit(@NotNull Command cmd) {
			cmd.addHelpArgument();

			if (cmd instanceof ArgumentParser ap)
				ap.addVersionArgument();
		}
	}
}
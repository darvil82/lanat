package lanat;

import lanat.exceptions.CommandAlreadyExistsException;
import lanat.exceptions.CommandTemplateException;
import lanat.helpRepresentation.HelpFormatter;
import lanat.parsing.Parser;
import lanat.parsing.Token;
import lanat.parsing.TokenType;
import lanat.parsing.Tokenizer;
import lanat.parsing.errors.Error;
import lanat.utils.*;
import lanat.utils.errors.ErrorCallbacks;
import lanat.utils.errors.ErrorContainerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import textFormatter.color.Color;
import textFormatter.color.SimpleColor;
import utils.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * <h2>Command</h2>
 * <p>
 * A command is a container for {@link Argument}s, other Sub{@link Command}s and {@link Group}s.
 *
 * @see Group
 * @see Argument
 */
public class Command
	extends ErrorContainerImpl<Error<?>>
	implements ErrorCallbacks<ParseResult, Command>,
		ArgumentAdder,
		GroupAdder,
		CommandAdder,
		CommandUser,
		Resettable,
		MultipleNamesAndDescription,
		ParentElementGetter<Command>
{
	private @NotNull List<@NotNull String> names = new ArrayList<>(1);
	private @Nullable String description;
	private final @NotNull ArrayList<@NotNull Argument<?, ?>> arguments = new ArrayList<>();
	private final @NotNull ArrayList<@NotNull Command> subCommands = new ArrayList<>(0);
	private @Nullable Command parentCommand;
	private final @NotNull ArrayList<@NotNull Group> groups = new ArrayList<>(0);
	private final @NotNull ModifyRecord<@NotNull Integer> errorCode = ModifyRecord.of(1);

	// error handling callbacks
	private @Nullable Consumer<Command> onErrorCallback;
	private @Nullable Consumer<ParseResult> onCorrectCallback;

	private @Nullable ParseResult cachedParseResult;
	private @Nullable Consumer<@NotNull ParseResult> onUsedCallback;

	private final @NotNull ModifyRecord<HelpFormatter> helpFormatter = ModifyRecord.of(new HelpFormatter());
	private final @NotNull ModifyRecord<@NotNull CallbackInvocationOption> callbackInvocationOption =
		ModifyRecord.of(CallbackInvocationOption.NO_ERROR_IN_ALL_COMMANDS);

	/** A pool of the colors that an argument may have when being represented on the help. */
	final @NotNull LoopPool<@NotNull Color> colorsPool = LoopPool.atRandomIndex(SimpleColor.BRIGHT_COLORS);


	/**
	 * Creates a new command with the given name and description.
	 * @param name The name of the command. This is the name the user will use to indicate that they want to use this
	 * 		   command. Must be unique among all the commands in the same parent command.
	 * @param description The description of the command.
	 * @see #setDescription(String)
	 */
	public Command(@NotNull String name, @Nullable String description) {
		this.setNames(List.of(name));
		this.description = description;
	}

	/**
	 * Creates a new command with the given name and no description. This is the name the user will use to
	 * indicate that they want to use this command.
	 * @param name The name of the command. Must be unique among all the commands in the same parent command.
	 */
	public Command(@NotNull String name) {
		this(name, null);
	}

	/**
	 * Creates a new command based on the given {@link CommandTemplate}. This does not take Sub-Commands into account.
	 * If you want to add Sub-Commands, use {@link ArgumentParser#from(Class)} instead.
	 * @param templateClass The class of the template to use.
	 * @see CommandTemplate
	 */
	public Command(@NotNull Class<? extends CommandTemplate> templateClass) {
		final var annotation = templateClass.getAnnotation(Command.Define.class);
		if (annotation == null) {
			throw new CommandTemplateException("The class '" + templateClass.getName()
				+ "' is not annotated with @Command.Define");
		}

		// add the names and description from the annotation
		this.setNames(List.of(CommandTemplate.getTemplateNames(templateClass)));
		if (!annotation.description().isBlank()) this.setDescription(annotation.description());

		this.from(templateClass);
	}


	@Override
	public <Type extends ArgumentType<TInner>, TInner>
	void addArgument(@NotNull Argument<Type, TInner> argument) {
		argument.registerToCommand(this);
		this.arguments.add(argument);
		this.checkUniqueArguments();
	}

	@Override
	public void addGroup(@NotNull Group group) {
		group.registerToCommand(this);
		this.groups.add(group);
		this.checkUniqueGroups();
	}

	@Override
	public @NotNull List<@NotNull Group> getGroups() {
		return Collections.unmodifiableList(this.groups);
	}

	@Override
	public void addCommand(@NotNull Command cmd) {
		if (cmd instanceof ArgumentParser) {
			throw new IllegalArgumentException("cannot add root command as Sub-Command");
		}

		if (cmd == this) {
			throw new IllegalArgumentException("cannot add command to itself");
		}

		cmd.registerToCommand(this);
		this.subCommands.add(cmd);
		this.checkUniqueSubCommands();
	}

	@Override
	public void registerToCommand(@NotNull Command parentCommand) {
		if (this.parentCommand != null) {
			throw new CommandAlreadyExistsException(this, this.parentCommand);
		}

		this.parentCommand = parentCommand;
	}

	/**
	 * Ensures that all groups in this command tree are properly linked to their parent groups.
	 * @see Group#linkHierarchyToCommand(Command)
	 */
	void linkGroupHierarchy() {
		this.groups.forEach(g -> g.linkHierarchyToCommand(this));

		// for sub-commands as well
		this.subCommands.forEach(Command::linkGroupHierarchy);
	}

	/**
	 * Returns a list of all the Sub-Commands that belong to this command.
	 *
	 * @return a list of all the Sub-Commands in this command
	 */
	@Override
	public @NotNull List<@NotNull Command> getCommands() {
		return Collections.unmodifiableList(this.subCommands);
	}


	/**
	 * Specifies the error code that the program should return when this command failed to parse. When multiple commands
	 * fail, the program will return the result of the OR bit operation that will be applied to all other command
	 * results. For example:
	 * <ul>
	 *     <li>Command 'foo' has a return value of 2. {@code (0b010)}</li>
	 *     <li>Command 'bar' has a return value of 5. {@code (0b101)}</li>
	 * </ul>
	 * Both commands failed, so in this case the resultant return value would be 7 {@code (0b111)}.
	 * @param errorCode The error code to return when this command fails.
	 */
	public void setErrorCode(int errorCode) {
		if (errorCode <= 0) throw new IllegalArgumentException("error code cannot be 0 or below");
		this.errorCode.set(errorCode);
	}

	@Override
	public void setNames(@NotNull List<@NotNull String> names) {
		if (names.isEmpty())
			throw new IllegalArgumentException("at least one name must be specified");

		for (var name : names)
			UtlString.requireValidName(name);

		UtlMisc.requireUniqueElements(
			names, n -> new IllegalArgumentException("Name '" + n + "' is already used by this command"
		));

		this.names = Collections.unmodifiableList(names);

		// now let the parent command know that this command has been modified. This is necessary to check
		// for duplicate names
		if (this.parentCommand != null)
			this.parentCommand.checkUniqueSubCommands();
	}

	@Override
	public @NotNull List<String> getNames() {
		return this.names;
	}

	@Override
	public void setDescription(@NotNull String description) {
		this.description = description;
	}

	@Override
	public @Nullable String getDescription() {
		return this.description;
	}

	/**
	 * Sets the help formatter that will be used to generate the help message of this command.
	 * @param helpFormatter The help formatter to set.
	 */
	public void setHelpFormatter(@NotNull HelpFormatter helpFormatter) {
		this.helpFormatter.set(helpFormatter);
	}

	public @NotNull HelpFormatter getHelpFormatter() {
		return this.helpFormatter.get();
	}

	/**
	 * Specifies in which cases the {@link Argument#setOnOkCallback(Consumer)} should be invoked.
	 * <p>By default, this is set to {@link CallbackInvocationOption#NO_ERROR_IN_ALL_COMMANDS}.</p>
	 *
	 * @param option The option to set.
	 * @see CallbackInvocationOption
	 */
	public void setCallbackInvocationOption(@NotNull Command.CallbackInvocationOption option) {
		this.callbackInvocationOption.set(option);
	}

	public @NotNull Command.CallbackInvocationOption getCallbackInvocationOption() {
		return this.callbackInvocationOption.get();
	}

	/**
	 * Sets the callback that will be invoked when this command is used by the user.
	 * @param onUsedCallback The callback to set.
	 */
	public void setOnUsedCallback(@NotNull Consumer<@NotNull ParseResult> onUsedCallback) {
		this.onUsedCallback = onUsedCallback;
	}

	/**
	 * Generates and returns the help message of this command.
	 * @return The help message of this command.
	 */
	public @NotNull String getHelp() {
		return this.helpFormatter.get().generate(this);
	}

	@Override
	public @NotNull List<Argument<?, ?>> getArguments() {
		return Collections.unmodifiableList(this.arguments);
	}

	/**
	 * Returns a list of all the positional arguments of this command. Order is preserved.
	 * @return A list of all the positional arguments of this command.
	 */
	public @NotNull List<@NotNull Argument<?, ?>> getPositionalArguments() {
		return this.getArguments().stream().filter(Argument::isPositional).toList();
	}

	/**
	 * Returns {@code true} if an argument with unique set in this command and its Sub-Commands was used.
	 * @param exclude The argument to exclude from the check.
	 * @return {@code true} if an argument with {@link Argument#setUnique(boolean)} in the command was used.
	 */
	boolean uniqueArgumentWasUsed(@Nullable Argument<?, ?> exclude) {
		return this.arguments.stream()
			.filter(a -> a != exclude)
			.anyMatch(a -> a.getUsageCount() >= 1 && a.isUnique())
		|| this.subCommands.stream().anyMatch(cmd -> cmd.uniqueArgumentWasUsed(exclude));
	}


	@Override
	public @NotNull String toString() {
		return "Command{name='%s', description='%s', arguments=%s, sub-commands=%s}"
			.formatted(
				this.getName(), this.description, this.arguments, this.subCommands
			);
	}

	/**
	 * Returns a new {@link ParseResult} object that contains all the parsed arguments of this command and all its
	 * Sub-Commands.
	 */
	@NotNull ParseResult getParseResult() {
		if (this.cachedParseResult == null) {
			this.cachedParseResult = new ParseResult(
				this,
				this.parser.getParsedArgsMap(),
				this.subCommands.stream().map(Command::getParseResult).toList()
			);
		}

		return this.cachedParseResult;
	}

	/** Generates the parsed arguments map of this command and all its Sub-Commands. */
	void generateParsedArgsMap() {
		this.parser.getParsedArgsMap(); // caches it
		this.subCommands.forEach(Command::generateParsedArgsMap);
	}

	/**
	 * Get all the tokens of all Sub-Commands (the ones that we can get without errors) into one single list. This
	 * includes the {@link TokenType#COMMAND} tokens.
	 * @return A list of all the tokens of all Sub-Commands.
	 */
	public @NotNull List<@NotNull Token> getFullTokenList() {
		final List<Token> list = Command.this.getTokenizer().getFinalTokens();

		Optional.ofNullable(this.getTokenizer().getTokenizedSubCommand())
			.ifPresent(c -> list.addAll(c.getFullTokenList()));

		return Collections.unmodifiableList(list);
	}

	/**
	 * Inherits certain properties from another command, only if they are not already set to something.
	 * <p>
	 * This method is automatically called right before parsing begins.
	 * @param command The command to inherit the properties from. Usually the parent command.
	 */
	protected void inheritProperties(@NotNull Command command) {
		this.getMinimumExitErrorLevel().setIfNotModified(command.getMinimumExitErrorLevel());
		this.getMinimumDisplayErrorLevel().setIfNotModified(command.getMinimumDisplayErrorLevel());
		this.errorCode.setIfNotModified(command.errorCode);
		this.helpFormatter.setIfNotModified(command.helpFormatter);
		this.callbackInvocationOption.setIfNotModified(command.callbackInvocationOption);
	}

	/**
	 * Passes certain properties to all the Sub-Commands of this command.
	 * @see #inheritProperties(Command)
	 */
	void passPropertiesToChildren() {
		this.subCommands.forEach(c -> {
			c.inheritProperties(this);
			c.passPropertiesToChildren();
		});
	}

	/**
	 * Returns {@code true} if the argument specified by the given name is equal to this argument.
	 * <p>
	 * Equality is determined by the argument's name and the command it belongs to.
	 * </p>
	 *
	 * @param obj the argument to compare to
	 * @return {@code true} if the argument specified by the given name is equal to this argument
	 */
	@Override
	public boolean equals(@NotNull Object obj) {
		if (obj == this) return true;
		if (obj instanceof Command cmd)
			return UtlMisc.equalsByNamesAndParentCmd(this, cmd);
		return false;
	}

	/**
	 * Checks that all the sub-commands in this container are unique.
	 * @throws CommandAlreadyExistsException if there are two commands with the same name
	 */
	void checkUniqueSubCommands() {
		UtlMisc.requireUniqueElements(this.subCommands, c -> new CommandAlreadyExistsException(c, this));
	}

	@Override
	public void resetState() {
		super.resetState();
		
		this.cachedParseResult = null;
		this.tokenizer = new Tokenizer(this);
		this.parser = new Parser(this);

		this.arguments.forEach(Argument::resetState);
		this.groups.forEach(Group::resetState);
		this.subCommands.forEach(Command::resetState);
	}

	@Override
	public @Nullable Command getParent() {
		return this.parentCommand;
	}

	@Override
	public @Nullable Command getParentCommand() {
		return this.getParent();
	}


	// ----------------------------------------------- Command Templates -----------------------------------------------

	/**
	 * Adds all the arguments from the given command template class to this command.
	 * This method is recursive, so it will add all the arguments from the parent template classes as well.
	 * @param cmdTemplate The command template class to add the arguments from.
	 */
	private void from(@NotNull Class<?> cmdTemplate) {
		if (!CommandTemplate.class.isAssignableFrom(cmdTemplate)) return;

		// don't allow classes without the @Command.Define annotation
		assert cmdTemplate.isAnnotationPresent(Command.Define.class) :
			"Command Template class must be annotated with @Command.Define";

		// get to the top of the hierarchy
		Optional.ofNullable(cmdTemplate.getSuperclass()).ifPresent(this::from);


		var argumentBuildersFieldPairs = Stream.of(cmdTemplate.getDeclaredFields())
			.filter(f -> f.isAnnotationPresent(Argument.Define.class))
			.map(f -> new Pair<>(f, ArgumentBuilder.fromField(f)))
			.toList();

		// invoke the beforeInit method
		this.from$invokeBeforeInitMethod(cmdTemplate, argumentBuildersFieldPairs.stream()
			.map(Pair::second)
			.toList());

		// set the argument types from the fields (if they are not already set)
		argumentBuildersFieldPairs.forEach(pair -> pair.second().setTypeFromField(pair.first()));

		// add the arguments to the command and the groups
		this.from$addArguments(argumentBuildersFieldPairs);

		// invoke the afterInit method
		this.from$invokeAfterInitMethod(cmdTemplate);
	}

	/**
	 * Adds all the arguments from the given list of argument builders to this command.
	 * The arguments with the same group name will be added to the same group.
	 * @param argumentBuildersFieldPairs The list of argument builders to add the arguments from.
	 */
	private void from$addArguments(
		List<Pair<Field, ArgumentBuilder<ArgumentType<Object>, Object>>> argumentBuildersFieldPairs
	) {
		final var groupsMap = new Hashtable<String, Group>();

		argumentBuildersFieldPairs.forEach(pair -> {
			Argument<?, ?> builtArgument;

			try {
				builtArgument = pair.second().build();
			} catch (IllegalStateException e) {
				throw new CommandTemplateException(
					"Could not build argument from field '" + pair.first().getName() + "': " + e.getMessage()
				);
			}

			var annotationGroupName = pair.first().getAnnotation(Argument.Define.class).group();
			if (annotationGroupName.isBlank()) {
				// the argument does not belong to a group, so add it directly.
				this.addArgument(builtArgument);
				return;
			}

			// the argument belongs to a group, so add it to it
			var groupToAddInto = groupsMap.get(annotationGroupName);
			if (groupToAddInto == null) {
				// the group does not exist, so create it and add it to the command
				groupToAddInto = new Group(annotationGroupName);
				groupsMap.put(annotationGroupName, groupToAddInto);
				this.addGroup(groupToAddInto);
			}

			groupToAddInto.addArgument(builtArgument);
		});
	}

	/**
	 * Invokes the {@link CommandTemplate#beforeInit(CommandTemplate.CommandBuildContext)} method of the given command
	 * template class, if it exists.
	 * @param cmdTemplate The command template class to invoke the method from.
	 * @param argumentBuilders The argument builders that will be passed to the method.
	 */
	private void from$invokeBeforeInitMethod(
		@NotNull Class<?> cmdTemplate,
		@NotNull List<? extends ArgumentBuilder<?, ?>> argumentBuilders
	) {
		Stream.of(cmdTemplate.getDeclaredMethods())
			.filter(m -> m.isAnnotationPresent(CommandTemplate.InitDef.class))
			.filter(m -> m.getName().equals("beforeInit"))
			.filter(m -> UtlReflection.hasParameters(m, CommandTemplate.CommandBuildContext.class))
			.findFirst()
			.ifPresent(m -> {
				try {
					m.invoke(null, new CommandTemplate.CommandBuildContext(
						this, Collections.unmodifiableList(argumentBuilders)
					));
				} catch (IllegalAccessException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			});
	}

	/**
	 * Invokes the {@link CommandTemplate#afterInit(Command)} method of the given command template class, if it exists.
	 * @param cmdTemplate The command template class to invoke the method from.
	 */
	private void from$invokeAfterInitMethod(@NotNull Class<?> cmdTemplate) {
		Stream.of(cmdTemplate.getDeclaredMethods())
			.filter(m -> m.isAnnotationPresent(CommandTemplate.InitDef.class))
			.filter(m -> m.getName().equals("afterInit"))
			.filter(m -> UtlReflection.hasParameters(m, Command.class))
			.findFirst()
			.ifPresent(m -> {
				try {
					m.invoke(null, this);
				} catch (IllegalAccessException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			});
	}

	// ------------------------------------------------ Error Handling ------------------------------------------------

	@Override
	public void setOnErrorCallback(@Nullable Consumer<@NotNull Command> callback) {
		this.onErrorCallback = callback;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * By default this callback is called only if all commands succeed, but you can change this behavior with
	 * {@link Command#setCallbackInvocationOption(CallbackInvocationOption)}
	 * </p>
	 */
	@Override
	public void setOnOkCallback(@Nullable Consumer<@NotNull ParseResult> callback) {
		this.onCorrectCallback = callback;
	}

	@Override
	public void invokeCallbacks() {
		if (this.shouldExecuteCorrectCallback()) {
			if (this.onCorrectCallback != null) this.onCorrectCallback.accept(this.getParseResult());
		} else {
			if (this.onErrorCallback != null) this.onErrorCallback.accept(this);
		}

		if (this.onUsedCallback != null && this.getParseResult().wasUsed())
			this.onUsedCallback.accept(this.getParseResult());

		this.parser.getParsedArgsMap()
			.entrySet()
			.stream()
			.sorted((x, y) -> Argument.compareByPriority(x.getKey(), y.getKey())) // sort by priority when invoking callbacks!
			.forEach(e -> e.getKey().invokeCallbacks(e.getValue()));

		// invoke the callbacks of the Sub-Commands recursively
		this.subCommands.forEach(Command::invokeCallbacks);
	}

	/**
	 * Returns {@code true} if the {@link #onCorrectCallback} should be executed.
	 */
	boolean shouldExecuteCorrectCallback() {
		return switch (this.getCallbackInvocationOption()) {
			case NO_ERROR_IN_COMMAND -> !this.hasExitErrorsNotIncludingSubCommands();
			case NO_ERROR_IN_COMMAND_AND_SUBCOMMANDS -> !this.hasExitErrors();
			case NO_ERROR_IN_ALL_COMMANDS -> !this.getRoot().hasExitErrors();
			case NO_ERROR_IN_ARGUMENT -> true;
		};
	}

	private boolean hasExitErrorsNotIncludingSubCommands() {
		return super.hasExitErrors()
			|| this.arguments.stream().anyMatch(Argument::hasExitErrors)
			|| this.parser.hasExitErrors()
			|| this.tokenizer.hasExitErrors();
	}

	@Override
	public boolean hasExitErrors() {
		var tokenizedSubCommand = this.getTokenizer().getTokenizedSubCommand();

		return this.hasExitErrorsNotIncludingSubCommands()
			|| tokenizedSubCommand != null && tokenizedSubCommand.hasExitErrors();
	}

	private boolean hasDisplayErrorsNotIncludingSubCommands() {
		return super.hasDisplayErrors()
			|| this.arguments.stream().anyMatch(Argument::hasDisplayErrors)
			|| this.parser.hasDisplayErrors()
			|| this.tokenizer.hasDisplayErrors();
	}

	@Override
	public boolean hasDisplayErrors() {
		var tokenizedSubCommand = this.getTokenizer().getTokenizedSubCommand();

		return this.hasDisplayErrorsNotIncludingSubCommands()
			|| tokenizedSubCommand != null && tokenizedSubCommand.hasDisplayErrors();
	}

	/**
	 * Get the error code of this Command. This is the OR of all the error codes of all the Sub-Commands that have
	 * failed.
	 *
	 * @return The error code of this command.
	 * @see #setErrorCode(int)
	 */
	public int getErrorCode() {
		final int thisErrorCode = this.errorCode.get();

		// get all the error codes of the Sub-Commands recursively
		int finalErrorCode = this.subCommands.stream()
			.filter(c -> c.tokenizer.hasFinished())
			.map(Command::getErrorCode)
			.reduce(0, (a, b) -> a | b);

		/* If we have errors, or the Sub-Commands had errors, do OR with our own error level.
		 * By doing this, the error code of a Sub-Command will be OR'd with the error codes of all its parents. */
		if (thisErrorCode != 0 && this.hasExitErrors()) {
			finalErrorCode |= thisErrorCode;
		}

		return finalErrorCode;
	}


	// --------------------------------------------------- Parsing ----------------------------------------------------

	private @NotNull Tokenizer tokenizer = new Tokenizer(this);
	private @NotNull Parser parser = new Parser(this);

	/** Returns the current tokenizer of this command. */
	public @NotNull Tokenizer getTokenizer() {
		return this.tokenizer;
	}

	/** Returns the current parser of this command. */
	public @NotNull Parser getParser() {
		return this.parser;
	}

	// -----------------------------------------------------------------------------------------------------------------


	/**
	 * Annotation used to define a command template.
	 * @see CommandTemplate
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface Define {
		/** @see Command#setNames(List)  */
		@NotNull String[] names() default {};

		/** @see Command#setDescription(String) */
		@NotNull String description() default "";
	}

	/**
	 * @see Command#setCallbackInvocationOption(CallbackInvocationOption)
	 */
	public enum CallbackInvocationOption {
		/** The callback will only be invoked when there are no errors in the argument. */
		NO_ERROR_IN_ARGUMENT,

		/** The callback will only be invoked when there are no errors in the command it belongs to. */
		NO_ERROR_IN_COMMAND,

		/**
		 * The callback will only be invoked when there are no errors in the command it belongs to, and all its
		 * Sub-Commands.
		 */
		NO_ERROR_IN_COMMAND_AND_SUBCOMMANDS,

		/** The callback will only be invoked when there are no errors in the whole command tree. */
		NO_ERROR_IN_ALL_COMMANDS,
	}
}
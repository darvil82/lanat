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
import lanat.utils.errors.ErrorsContainerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import textFormatter.Color;
import utils.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * <h2>Command</h2>
 * <p>
 * A command is a container for {@link Argument}s, other Sub{@link Command}s and {@link ArgumentGroup}s.
 *
 * @see ArgumentGroup
 * @see Argument
 */
public class Command
	extends ErrorsContainerImpl<Error.CustomError>
	implements ErrorCallbacks<ParseResult, Command>,
				   ArgumentAdder,
	ArgumentGroupAdder,
	CommandAdder,
	CommandUser,
	Resettable,
	MultipleNamesAndDescription,
	ParentElementGetter<Command>
{
	private final @NotNull List<@NotNull String> names = new ArrayList<>();
	private @Nullable String description;
	private final @NotNull ArrayList<@NotNull Argument<?, ?>> arguments = new ArrayList<>();
	private final @NotNull ArrayList<@NotNull Command> subCommands = new ArrayList<>();
	private Command parentCommand;
	private final @NotNull ArrayList<@NotNull ArgumentGroup> argumentGroups = new ArrayList<>();
	private final @NotNull ModifyRecord<@NotNull TupleChar> tupleChars = ModifyRecord.of(TupleChar.SQUARE_BRACKETS);
	private final @NotNull ModifyRecord<@NotNull Integer> errorCode = ModifyRecord.of(1);

	// error handling callbacks
	private @Nullable Consumer<Command> onErrorCallback;
	private @Nullable Consumer<ParseResult> onCorrectCallback;

	private final @NotNull ModifyRecord<HelpFormatter> helpFormatter = ModifyRecord.of(new HelpFormatter());
	private final @NotNull ModifyRecord<@NotNull CallbacksInvocationOption> callbackInvocationOption =
		ModifyRecord.of(CallbacksInvocationOption.NO_ERROR_IN_ALL_COMMANDS);

	/** A pool of the colors that an argument may have when being represented on the help. */
	final @NotNull LoopPool<@NotNull Color> colorsPool = LoopPool.atRandomIndex(Color.BRIGHT_COLORS);


	/**
	 * Creates a new command with the given name and description.
	 * @param name The name of the command. This is the name the user will use to indicate that they want to use this
	 * 		   command. Must be unique among all the commands in the same parent command.
	 * @param description The description of the command.
	 * @see #setDescription(String)
	 */
	public Command(@NotNull String name, @Nullable String description) {
		this.addNames(name);
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
		this.addNames(CommandTemplate.getTemplateNames(templateClass));
		if (annotation.description() != null) this.setDescription(annotation.description());

		this.from$recursive(templateClass);
	}

	@Override
	public <T extends ArgumentType<TInner>, TInner>
	void addArgument(@NotNull Argument<T, TInner> argument) {
		argument.registerToCommand(this);
		this.arguments.add(argument);
		this.checkUniqueArguments();
	}

	/**
	 * Adds a 'help' argument which shows the help message of the command
	 * (provided by the {@link Command#getHelp()} method).
	 */
	public void addHelpArgument() {
		this.addArgument(Argument.createOfBoolType("help", "h")
			.onOk(t -> {
				System.out.println(this.getHelp());
//				System.exit(0);
			})
			.withDescription("Shows this message.")
			.allowsUnique()
		);
	}

	@Override
	public void addGroup(@NotNull ArgumentGroup group) {
		group.registerToCommand(this);
		this.argumentGroups.add(group);
		this.checkUniqueGroups();
	}

	@Override
	public @NotNull List<@NotNull ArgumentGroup> getGroups() {
		return Collections.unmodifiableList(this.argumentGroups);
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

	/**
	 * Sets the set of characters that the user should use to indicate a start/end of a tuple.
	 * @param tupleChars The tuple characters to set.
	 */
	public void setTupleChars(@NotNull TupleChar tupleChars) {
		this.tupleChars.set(tupleChars);
	}

	public @NotNull TupleChar getTupleChars() {
		return this.tupleChars.get();
	}

	@Override
	public void addNames(@NotNull String... names) {
		if (names.length == 0)
			throw new IllegalArgumentException("at least one name must be specified");

		Stream.of(names)
			.map(UtlString::requireValidName)
			.peek(newName -> {
				if (this.hasName(newName))
					throw new IllegalArgumentException("Name '" + newName + "' is already used by this command.");
			})
			.forEach(this.names::add);

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
	 * <p>By default, this is set to {@link CallbacksInvocationOption#NO_ERROR_IN_ALL_COMMANDS}.</p>
	 *
	 * @param option The option to set.
	 * @see CallbacksInvocationOption
	 */
	public void setCallbackInvocationOption(@NotNull CallbacksInvocationOption option) {
		this.callbackInvocationOption.set(option);
	}

	public @NotNull CallbacksInvocationOption getCallbackInvocationOption() {
		return this.callbackInvocationOption.get();
	}

	/**
	 * Generates and returns the help message of this command.
	 * @return The help message of this command.
	 */
	public @NotNull String getHelp() {
		return this.helpFormatter.get().generate(this) + "\n";
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
	 * Returns {@code true} if an argument with allowsUnique set in the command was used.
	 * @return {@code true} if an argument with {@link Argument#setAllowUnique(boolean)} in the command was used.
	 */
	boolean uniqueArgumentReceivedValue(@Nullable Argument<?, ?> exclude) {
		return this.arguments.stream()
			.filter(a -> a != exclude)
			.anyMatch(a -> a.getUsageCount() >= 1 && a.isUniqueAllowed())
		|| this.subCommands.stream().anyMatch(cmd -> cmd.uniqueArgumentReceivedValue(exclude));
	}


	@Override
	public @NotNull String toString() {
		return "Command[name='%s', description='%s', arguments=%s, Sub-Commands=%s]"
			.formatted(
				this.getName(), this.description, this.arguments, this.subCommands
			);
	}

	/**
	 * Returns a new {@link ParseResult} object that contains all the parsed arguments of this command and all its
	 * Sub-Commands.
	 */
	@NotNull ParseResult getParseResult() {
		return new ParseResult(
			this,
			this.parser.getParsedArgsMap(),
			this.subCommands.stream().map(Command::getParseResult).toList()
		);
	}

	/**
	 * Get all the tokens of all Sub-Commands (the ones that we can get without errors) into one single list. This
	 * includes the {@link TokenType#COMMAND} tokens.
	 * @return A list of all the tokens of all Sub-Commands.
	 */
	public @NotNull List<@NotNull Token> getFullTokenList() {
		final List<Token> list = Command.this.getTokenizer().getFinalTokens();

		final Command subCmd = this.getTokenizer().getTokenizedSubCommand();

		if (subCmd != null) {
			list.addAll(subCmd.getFullTokenList());
		}

		return Collections.unmodifiableList(list);
	}

	/**
	 * Inherits certain properties from another command, only if they are not already set to something.
	 */
	private void inheritProperties(@NotNull Command parent) {
		this.tupleChars.setIfNotModified(parent.tupleChars);
		this.getMinimumExitErrorLevel().setIfNotModified(parent.getMinimumExitErrorLevel());
		this.getMinimumDisplayErrorLevel().setIfNotModified(parent.getMinimumDisplayErrorLevel());
		this.errorCode.setIfNotModified(parent.errorCode);
		this.helpFormatter.setIfNotModified(parent.helpFormatter);
		this.callbackInvocationOption.setIfNotModified(parent.callbackInvocationOption);

		this.passPropertiesToChildren();
	}

	/**
	 * Adds all the arguments from the given command template class to this command.
	 * This method is recursive, so it will add all the arguments from the parent class as well.
	 * @param cmdTemplate The command template class to add the arguments from.
	 */
	private void from$recursive(@NotNull Class<?> cmdTemplate) {
		if (!CommandTemplate.class.isAssignableFrom(cmdTemplate)) return;

		// don't allow classes without the @Command.Define annotation
		assert cmdTemplate.isAnnotationPresent(Command.Define.class) :
			"Command Template class must be annotated with @Command.Define";

		// get to the top of the hierarchy
		Optional.ofNullable(cmdTemplate.getSuperclass()).ifPresent(this::from$recursive);


		var argumentBuildersFieldPairs = Stream.of(cmdTemplate.getDeclaredFields())
			.filter(f -> f.isAnnotationPresent(Argument.Define.class))
			.map(f -> new Pair<>(f, ArgumentBuilder.fromField(f)))
			.toList();

		var argumentBuilders = argumentBuildersFieldPairs.stream().map(Pair::second).toList();

		this.from$invokeBeforeInitMethod(cmdTemplate, argumentBuilders);

		// set the argument types from the fields (if they are not already set)
		argumentBuildersFieldPairs.forEach(pair -> pair.second().setArgTypeFromField(pair.first()));

		// add the arguments to the command
		argumentBuilders.forEach(this::addArgument);

		this.from$invokeAfterInitMethod(cmdTemplate);
	}

	/**
	 * Invokes the {@link CommandTemplate#beforeInit(CommandTemplate.CommandBuildHelper)} method of the given command
	 * template class, if it exists.
	 * @param cmdTemplate The command template class to invoke the method from.
	 * @param argumentBuilders The argument builders that will be passed to the method.
	 */
	private void from$invokeBeforeInitMethod(
		@NotNull Class<?> cmdTemplate,
		@NotNull List<? extends ArgumentBuilder<?, ?>> argumentBuilders
	) {
		Stream.of(cmdTemplate.getDeclaredMethods())
			.filter(m -> UtlReflection.hasParameters(m, CommandTemplate.CommandBuildHelper.class))
			.filter(m -> m.isAnnotationPresent(CommandTemplate.InitDef.class))
			.filter(m -> m.getName().equals("beforeInit"))
			.findFirst()
			.ifPresent(m -> {
				try {
					m.invoke(null, new CommandTemplate.CommandBuildHelper(
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
			.filter(m -> UtlReflection.hasParameters(m, Command.class))
			.filter(m -> m.isAnnotationPresent(CommandTemplate.InitDef.class))
			.filter(m -> m.getName().equals("afterInit"))
			.findFirst()
			.ifPresent(m -> {
				try {
					m.invoke(null, this);
				} catch (IllegalAccessException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			});
	}

	/**
	 * Passes certain properties to all the Sub-Commands of this command.
	 * @see #inheritProperties(Command)
	 */
	void passPropertiesToChildren() {
		this.subCommands.forEach(c -> c.inheritProperties(this));
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


	// ------------------------------------------------ Error Handling ------------------------------------------------

	@Override
	public void setOnErrorCallback(@Nullable Consumer<@NotNull Command> callback) {
		this.onErrorCallback = callback;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * By default this callback is called only if all commands succeed, but you can change this behavior with
	 * {@link Command#setCallbackInvocationOption(CallbacksInvocationOption)}
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


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//                                         Argument tokenization and parsing    							      //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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


	@Override
	public void resetState() {
		super.resetState();
		this.tokenizer = new Tokenizer(this);
		this.parser = new Parser(this);
		this.arguments.forEach(Argument::resetState);
		this.argumentGroups.forEach(ArgumentGroup::resetState);

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

	/**
	 * Annotation used to define a command template.
	 * @see CommandTemplate
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface Define {
		/** @see Command#addNames(String...) */
		String[] names() default {};

		/** @see Command#setDescription(String) */
		String description() default "";
	}
}
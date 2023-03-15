package lanat;

import fade.mirror.MClass;
import fade.mirror.filter.Filter;
import lanat.commandTemplates.DefaultCommandTemplate;
import lanat.exceptions.ArgumentAlreadyExistsException;
import lanat.exceptions.ArgumentGroupAlreadyExistsException;
import lanat.exceptions.CommandAlreadyExistsException;
import lanat.helpRepresentation.HelpFormatter;
import lanat.parsing.Parser;
import lanat.parsing.Token;
import lanat.parsing.TokenType;
import lanat.parsing.Tokenizer;
import lanat.parsing.errors.CustomError;
import lanat.utils.*;
import lanat.utils.displayFormatter.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static fade.mirror.Mirror.mirror;

/**
 * <h2>Command</h2>
 * <p>
 * A command is a container for {@link Argument}s, other Sub{@link Command}s and {@link ArgumentGroup}s.
 *
 * @see ArgumentGroup
 * @see Argument
 */
public class Command
	extends ErrorsContainerImpl<CustomError>
	implements ErrorCallbacks<ParsedArguments, Command>,
		ArgumentAdder,
		ArgumentGroupAdder,
		Resettable,
		MultipleNamesAndDescription,
		ParentElementGetter<Command>,
		CommandUser
{
	private final @NotNull List<@NotNull String> names = new ArrayList<>();
	public @Nullable String description;
	final @NotNull ArrayList<@NotNull Argument<?, ?>> arguments = new ArrayList<>();
	final @NotNull ArrayList<@NotNull Command> subCommands = new ArrayList<>();
	private Command parentCommand;
	final @NotNull ArrayList<@NotNull ArgumentGroup> argumentGroups = new ArrayList<>();
	private final @NotNull ModifyRecord<@NotNull TupleCharacter> tupleChars = new ModifyRecord<>(TupleCharacter.SQUARE_BRACKETS);
	private final @NotNull ModifyRecord<@NotNull Integer> errorCode = new ModifyRecord<>(1);

	// error handling callbacks
	private @Nullable Consumer<Command> onErrorCallback;
	private @Nullable Consumer<ParsedArguments> onCorrectCallback;

	private final @NotNull ModifyRecord<HelpFormatter> helpFormatter = new ModifyRecord<>(new HelpFormatter(this));
	private final @NotNull ModifyRecord<@NotNull CallbacksInvocationOption> callbackInvocationOption =
		new ModifyRecord<>(CallbacksInvocationOption.NO_ERROR_IN_ALL_COMMANDS);

	/** A pool of the colors that an argument may have when being represented on the help. */
	final @NotNull LoopPool<@NotNull Color> colorsPool = LoopPool.atRandomIndex(Color.getBrightColors());


	public Command(@NotNull String name, @Nullable String description, CommandTemplate template) {
		this.addNames(name);
		this.description = description;
//		template.applyTo(this);
	}

	public Command(@NotNull String name, @Nullable String description) {
		this(name, description, new DefaultCommandTemplate());
	}

	public Command(@NotNull String name) {
		this(name, null);
	}

	@Override
	public <T extends ArgumentType<TInner>, TInner>
	void addArgument(@NotNull Argument<T, TInner> argument) {
		argument.setParentCommand(this); // has to be done before checking for duplicates
		if (this.arguments.stream().anyMatch(a -> a.equals(argument))) {
			throw new ArgumentAlreadyExistsException(argument, this);
		}
		this.arguments.add(argument);
	}

	@Override
	public void addGroup(@NotNull ArgumentGroup group) {
		if (this.argumentGroups.stream().anyMatch(g -> g.name.equals(group.name))) {
			throw new ArgumentGroupAlreadyExistsException(group, this);
		}
		group.registerGroup(this);
		this.argumentGroups.add(group);
	}

	@Override
	public @NotNull List<@NotNull ArgumentGroup> getGroups() {
		return Collections.unmodifiableList(this.argumentGroups);
	}

	public void addCommand(@NotNull Command cmd) {
		if (this.subCommands.stream().anyMatch(a -> a.hasName(cmd.names.get(0)))) {
			throw new CommandAlreadyExistsException(cmd, this);
		}

		if (cmd instanceof ArgumentParser) {
			throw new IllegalArgumentException("cannot add root command as Sub-Command");
		}

		this.subCommands.add(cmd);
		cmd.parentCommand = this;
	}

	public @NotNull List<@NotNull Command> getCommands() {
		return Collections.unmodifiableList(this.subCommands);
	}

	/**
	 * Specifies the error code that the program should return when this command failed to parse. When multiple commands
	 * fail, the program will return the result of the OR bit operation that will be applied to all other command
	 * results. For example:
	 * <ul>
	 *     <li>Command 'foo' has a return value of 2. <code>(0b010)</code></li>
	 *     <li>Command 'bar' has a return value of 5. <code>(0b101)</code></li>
	 * </ul>
	 * Both commands failed, so in this case the resultant return value would be 7 <code>(0b111)</code>.
	 */
	public void setErrorCode(int errorCode) {
		if (errorCode <= 0) throw new IllegalArgumentException("error code cannot be 0 or below");
		this.errorCode.set(errorCode);
	}

	public void setTupleChars(@NotNull TupleCharacter tupleChars) {
		this.tupleChars.set(tupleChars);
	}

	public @NotNull TupleCharacter getTupleChars() {
		return this.tupleChars.get();
	}

	@Override
	public void addNames(String... names) {
		Arrays.stream(names)
			.forEach(n -> {
				if (!UtlString.matchCharacters(n, Character::isAlphabetic))
					throw new IllegalArgumentException("Name " + UtlString.surround(n) + " contains non-alphabetic characters.");

				if (this.hasName(n))
					throw new IllegalArgumentException("Name " + UtlString.surround(n) + " is already used by this command.");

				this.names.add(n);
			});
	}

	@Override
	public @NotNull List<String> getNames() {
		return this.names;
	}

	public void setDescription(@NotNull String description) {
		this.description = description;
	}

	@Override
	public @Nullable String getDescription() {
		return this.description;
	}

	public void setHelpFormatter(@NotNull HelpFormatter helpFormatter) {
		helpFormatter.setParentCmd(this);
		this.helpFormatter.set(helpFormatter);
	}

	public @NotNull HelpFormatter getHelpFormatter() {
		return this.helpFormatter.get();
	}

	/**
	 * Specifies in which cases the {@link Argument#setOnCorrectCallback(Consumer)} should be invoked.
	 * <p>By default, this is set to {@link CallbacksInvocationOption#NO_ERROR_IN_ALL_COMMANDS}.</p>
	 *
	 * @see CallbacksInvocationOption
	 */
	public void invokeCallbacksWhen(@NotNull CallbacksInvocationOption option) {
		this.callbackInvocationOption.set(option);
	}

	public @NotNull CallbacksInvocationOption getCallbackInvocationOption() {
		return this.callbackInvocationOption.get();
	}

	public void addError(@NotNull String message, @NotNull ErrorLevel level) {
		this.addError(new CustomError(message, level));
	}

	public @NotNull String getHelp() {
		return this.helpFormatter.get().toString();
	}

	@Override
	public @NotNull List<Argument<?, ?>> getArguments() {
		return Collections.unmodifiableList(this.arguments);
	}

	public @NotNull List<@NotNull Argument<?, ?>> getPositionalArguments() {
		return this.getArguments().stream().filter(Argument::isPositional).toList();
	}

	/**
	 * Returns <code>true</code> if an argument with {@link Argument#setAllowUnique(boolean)} in the command was used.
	 */
	public boolean uniqueArgumentReceivedValue() {
		return this.arguments.stream().anyMatch(a -> a.getUsageCount() >= 1 && a.isUniqueAllowed())
			|| this.subCommands.stream().anyMatch(Command::uniqueArgumentReceivedValue);
	}


	@Override
	public @NotNull String toString() {
		return "Command[name='%s', description='%s', arguments=%s, Sub-Commands=%s]"
			.formatted(
				this.getName(), this.description, this.arguments, this.subCommands
			);
	}

	@NotNull ParsedArguments getParsedArguments() {
		return new ParsedArguments(
			this,
			this.parser.getParsedArgumentsHashMap(),
			this.subCommands.stream().map(Command::getParsedArguments).toList()
		);
	}

	/**
	 * Get all the tokens of all Sub-Commands (the ones that we can get without errors) into one single list. This
	 * includes the {@link TokenType#COMMAND} tokens.
	 */
	public @NotNull ArrayList<@NotNull Token> getFullTokenList() {
		final ArrayList<Token> list = new ArrayList<>() {{
			this.add(new Token(TokenType.COMMAND, Command.this.getName()));
			this.addAll(Command.this.getTokenizer().getFinalTokens());
		}};

		final Command subCmd = this.getTokenizer().getTokenizedSubCommand();

		if (subCmd != null) {
			list.addAll(subCmd.getFullTokenList());
		}

		return list;
	}

	/**
	 * Inherits certain properties from another command, only if they are not already set to something.
	 */
	private void inheritProperties(@NotNull Command parent) {
		this.tupleChars.setIfNotModified(parent.tupleChars);
		this.getMinimumExitErrorLevel().setIfNotModified(parent.getMinimumExitErrorLevel());
		this.getMinimumDisplayErrorLevel().setIfNotModified(parent.getMinimumDisplayErrorLevel());
		this.errorCode.setIfNotModified(parent.errorCode);
		this.helpFormatter.setIfNotModified(() -> {
			/* NEED TO BE COPIED!! If we don't then all commands will have the same formatter,
			 * which causes lots of problems.
			 *
			 * Stuff like the layout generators closures are capturing the reference to the previous Command
			 * and will not be updated properly when the parent command is updated. */
			var fmt = new HelpFormatter(parent.helpFormatter.get());
			fmt.setParentCmd(this); // we need to update the parent command!
			return fmt;
		});
		this.callbackInvocationOption.setIfNotModified(parent.callbackInvocationOption);

		this.passPropertiesToChildren();
	}

	@SuppressWarnings("unchecked")
	private <T extends CommandTemplate>
	void from(@NotNull MClass<T> clazz, ArrayList<Argument.ArgumentBuilder<?, ?>> argBuilders) {
		clazz.getSuperclass().ifPresent(superClass -> this.from((MClass<T>)superClass, argBuilders));

		clazz.getFields(Filter.forFields()
			.withAnnotation(Argument.Define.class))
			.forEach(f ->
				f.getAnnotationOfType(Argument.Define.class)
					.ifPresent(a -> argBuilders.add(Argument.ArgumentBuilder.fromField(f, a)))
			);

		clazz.getMethod(Filter.forMethods()
			.withAnnotation(CommandTemplate.InitDef.class)
			.withName("init")
			.withParameter(CommandTemplate.CommandBuildHelper.class)
		).ifPresent(m -> m.invoke(new CommandTemplate.CommandBuildHelper(this, argBuilders)));
	}

	public void from(@NotNull Class<? extends CommandTemplate> clazz) {
		var argBuilders = new ArrayList<Argument.ArgumentBuilder<?, ?>>();
		this.from(mirror(clazz), argBuilders);
		argBuilders.forEach(b -> this.addArgument(b.build()));
	}

	void passPropertiesToChildren() {
		this.subCommands.forEach(c -> c.inheritProperties(this));
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
	 * {@link Command#invokeCallbacksWhen(CallbacksInvocationOption)}
	 * </p>
	 */
	@Override
	public void setOnCorrectCallback(@Nullable Consumer<@NotNull ParsedArguments> callback) {
		this.onCorrectCallback = callback;
	}

	@Override
	public void invokeCallbacks() {
		this.subCommands.forEach(Command::invokeCallbacks);

		if (this.shouldExecuteCorrectCallback()) {
			if (this.onCorrectCallback != null) this.onCorrectCallback.accept(this.getParsedArguments());
		} else {
			if (this.onErrorCallback != null) this.onErrorCallback.accept(this);
		}

		this.parser.getParsedArgumentsHashMap().forEach(Argument::invokeCallbacks);
	}

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
	 * Get the error code of this Command. This is the OR of all the error codes of all the Sub-Commands that
	 * have failed.
	 * @see #setErrorCode(int)
	 * @return The error code of this command.
	 */
	public int getErrorCode() {
		int errCode = this.subCommands.stream()
			.filter(c -> c.tokenizer.isFinishedTokenizing())
			.map(sc ->
				sc.getMinimumExitErrorLevel().get().isInErrorMinimum(this.getMinimumExitErrorLevel().get())
					? sc.getErrorCode()
					: 0
			)
			.reduce(0, (a, b) -> a | b);

		/* If we have errors, or the Sub-Commands had errors, do OR with our own error level.
		 * By doing this, the error code of a Sub-Command will be OR'd with the error codes of all its parents. */
		if (this.hasExitErrors() || errCode != 0) {
			errCode |= this.errorCode.get();
		}

		return errCode;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//                                         Argument tokenization and parsing    							      //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private @NotNull Tokenizer tokenizer = new Tokenizer(this);
	private @NotNull Parser parser = new Parser(this);

	public @NotNull Tokenizer getTokenizer() {
		return this.tokenizer;
	}

	public @NotNull Parser getParser() {
		return this.parser;
	}

	void tokenize(@NotNull String input) {
		// this tokenizes recursively!
		this.tokenizer.tokenize(input);
	}

	void parseTokens() {
		// first we need to set the tokens of all tokenized subCommands
		Command cmd = this;
		do {
			cmd.parser.setTokens(cmd.tokenizer.getFinalTokens());
		} while ((cmd = cmd.getTokenizer().getTokenizedSubCommand()) != null);

		// this parses recursively!
		this.parser.parseTokens();
	}

	/**
	 * Returns <code>true</code> if the argument specified by the given name is equal to this argument.
	 * <p>
	 * Equality is determined by the argument's name and the command it belongs to.
	 * </p>
	 * @param obj the argument to compare to
	 * @return <code>true</code> if the argument specified by the given name is equal to this argument
	 */
	public boolean equals(@NotNull Command obj) {
		return Command.equalsByNamesAndParentCmd(this, obj);
	}

	public static <T extends MultipleNamesAndDescription & CommandUser>
	boolean equalsByNamesAndParentCmd(@NotNull T a, @NotNull T b) {
		return a.getParentCommand() == b.getParentCommand() && (
			a.getNames().stream().anyMatch(name -> {
				for (var otherName : b.getNames()) {
					if (name.equals(otherName)) return true;
				}
				return false;
			})
		);
	}

	@Override
	public void resetState() {
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

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface Define {
		String[] names() default {};
		String description() default "";
	}
}
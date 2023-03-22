package lanat;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

@Command.Define
public abstract class CommandTemplate {
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	protected @interface InitDef {}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	protected @interface CommandAccessor {}


	// Dummy method so that we prevent the user from creating an instance method with the same name.
	@InitDef
	public static void init(@NotNull Command cmd) {}
}
package argparser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class HelpFormatter {
	private Command parentCmd;
	private byte indentSize = 3;
	private ArrayList<Function<Command, String>> layout = new ArrayList<>();

	HelpFormatter(Command parentCmd) {
		this.parentCmd = parentCmd;
		this.setLayout();
	}

	HelpFormatter(HelpFormatter other) {
		this.parentCmd = other.parentCmd;
		this.indentSize = other.indentSize;
		this.layout.addAll(other.layout);
	}

	public HelpFormatter() {
		this.setLayout();
	}

	void setParentCmd(Command parentCmd) {
		this.parentCmd = parentCmd;
	}

	protected List<Function<Command, String>> getLayout() {
		return layout;
	}

	public void setLayout() {
		this.changeLayout(LayoutGenerators::title);
	}

	@SafeVarargs
	protected final void addToLayout(Function<Command, String>... layoutGenerator) {
		this.layout.addAll(Arrays.asList(layoutGenerator));
	}

	@SafeVarargs
	protected final void changeLayout(Function<Command, String>... layoutGenerator) {
		this.layout = new ArrayList<>(Arrays.asList(layoutGenerator));
	}


	protected static class LayoutGenerators {
		public static String title(Command cmd) {
			return cmd.name + (cmd.description == null ? "" : ": " + cmd.description);
		}
	}


	@Override
	public String toString() {
		var buffer = new StringBuilder();

		for (var generator : this.layout) {
			buffer.append(generator.apply(this.parentCmd)).append('\n');
		}

		return buffer.toString();
	}
}

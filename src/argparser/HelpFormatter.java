package argparser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class HelpFormatter {
	private Command parentCmd;
	private byte indentSize = 3;
	protected final LayoutGenerators LAYOUT_GENERATORS = new LayoutGenerators();
	private final ArrayList<Supplier<String>> layout = new ArrayList<>();

	HelpFormatter(Command parentCmd) {
		this.parentCmd = parentCmd;
		this.setLayout();
	}

	public HelpFormatter() {
		this.setLayout();
	}

	void setParentCmd(Command parentCmd) {
		this.parentCmd = parentCmd;
	}

	protected ArrayList<Supplier<String>> getLayout() {
		return layout;
	}

	public void setLayout() {
		this.layout.add(LAYOUT_GENERATORS::title);
	}

	protected final class LayoutGenerators {
		public String title() {
			return HelpFormatter.this.parentCmd.name + ": " + HelpFormatter.this.parentCmd.description;
		}

		public String text(String content) {
			return content;
		}
	}

	@Override
	public String toString() {
		var buffer = new StringBuilder();

		for (var generator : this.layout) {
			buffer.append(generator.get()).append('\n');
		}

		return buffer.toString();
	}
}

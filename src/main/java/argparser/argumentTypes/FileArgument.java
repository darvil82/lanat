package argparser.argumentTypes;

import argparser.ArgumentType;

import java.io.File;

public class FileArgument extends ArgumentType<File> {
	@Override
	public File parseValues(String[] args) {
		try {
			return new File(args[0]);
		} catch (Exception e) {
			this.addError("File not found: '" + args[0] + "'.");
			return null;
		}
	}
}

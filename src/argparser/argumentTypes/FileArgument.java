package argparser.argumentTypes;

import argparser.ArgumentType;

import java.io.FileReader;

public class FileArgument extends ArgumentType<FileReader> {
	@Override
	public void parseValues(String[] args) {
		try {
			this.value = new FileReader(args[0]);
		} catch (Exception e) {
			this.addError("File not found: '" + args[0] + "'.");
		}
	}
}

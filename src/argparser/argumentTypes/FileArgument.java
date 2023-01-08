package argparser.argumentTypes;

import argparser.ArgumentType;

import java.io.FileReader;

public class FileArgument extends ArgumentType<FileReader> {
	@Override
	public FileReader parseValues(String[] args) {
		try {
			return new FileReader(args[0]);
		} catch (Exception e) {
			this.addError("File not found: '" + args[0] + "'.");
			return null;
		}
	}
}

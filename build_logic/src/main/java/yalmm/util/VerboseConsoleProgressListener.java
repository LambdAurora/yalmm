package yalmm.util;

import cuchaz.enigma.command.Command;

public class VerboseConsoleProgressListener extends Command.ConsoleProgressListener {
	@Override
	public void step(int numDone, String message) {
		System.out.printf("\t> %s%n", message);
		super.step(numDone, message);
	}
}

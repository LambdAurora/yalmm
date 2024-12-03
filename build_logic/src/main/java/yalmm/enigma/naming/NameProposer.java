package yalmm.enigma.naming;

import cuchaz.enigma.translation.mapping.EntryRemapper;
import cuchaz.enigma.translation.representation.entry.Entry;

import java.util.Optional;

public abstract class NameProposer {
	public abstract boolean canPropose(Entry<?> obfEntry);

	public abstract Optional<String> proposeName(Entry<?> obfEntry, EntryRemapper remapper);
}

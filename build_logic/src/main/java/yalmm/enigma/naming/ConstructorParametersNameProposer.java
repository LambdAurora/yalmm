package yalmm.enigma.naming;

import cuchaz.enigma.translation.mapping.EntryRemapper;
import cuchaz.enigma.translation.representation.entry.Entry;
import cuchaz.enigma.translation.representation.entry.FieldEntry;
import cuchaz.enigma.translation.representation.entry.LocalVariableEntry;
import yalmm.enigma.JarIndexer;
import yalmm.enigma.index.ConstructorParametersIndex;

import java.util.Optional;

/**
 * Represents a name proposer for constructor parameters from their associated fields.
 *
 * @author LambdAurora
 */
public class ConstructorParametersNameProposer extends NameProposer {
	private final ConstructorParametersIndex index;

	public ConstructorParametersNameProposer(JarIndexer indexer) {
		this.index = indexer.constructorParametersIndex;
	}

	@Override
	public boolean canPropose(Entry<?> obfEntry) {
		return obfEntry instanceof LocalVariableEntry paramEntry && this.index.getLinkedField(paramEntry) != null;
	}

	@Override
	public Optional<String> proposeName(Entry<?> obfEntry, EntryRemapper remapper) {
		return this.index.getLinkedField((LocalVariableEntry) obfEntry).resolve(remapper);
	}
}

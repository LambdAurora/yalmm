package yalmm.enigma.naming;

import cuchaz.enigma.translation.mapping.EntryRemapper;
import cuchaz.enigma.translation.representation.entry.Entry;
import cuchaz.enigma.translation.representation.entry.LocalVariableEntry;
import yalmm.enigma.JarIndexer;
import yalmm.enigma.index.SimpleTypeSingleIndex;

import java.util.Optional;

public class SimpleTypeParamNameProposer extends NameProposer {
	private final SimpleTypeSingleIndex index;

	public SimpleTypeParamNameProposer(JarIndexer indexer) {
		this.index = indexer.simpleTypeSingleIndex;
	}

	@Override
	public boolean canPropose(Entry<?> entry) {
		return entry instanceof LocalVariableEntry;
	}

	@Override
	public Optional<String> proposeName(Entry<?> obfEntry, EntryRemapper remapper) {
		if (obfEntry instanceof LocalVariableEntry localVariableEntry) {
			var paramEntry = SimpleTypeSingleIndex.ParameterEntry.fromLocalVariableEntry(localVariableEntry);
			return Optional.ofNullable(this.index.getParam(paramEntry));
		}

		return Optional.empty();
	}
}
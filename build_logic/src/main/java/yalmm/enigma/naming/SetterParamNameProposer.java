package yalmm.enigma.naming;

import cuchaz.enigma.translation.mapping.EntryRemapper;
import cuchaz.enigma.translation.representation.entry.Entry;
import cuchaz.enigma.translation.representation.entry.FieldEntry;
import cuchaz.enigma.translation.representation.entry.LocalVariableEntry;
import yalmm.enigma.JarIndexer;
import yalmm.enigma.index.GetterSetterIndex;

import java.util.Optional;

public class SetterParamNameProposer extends NameProposer {
	private final GetterSetterIndex index;

	public SetterParamNameProposer(JarIndexer indexer) {
		this.index = indexer.getterSetterIndex;
	}

	@Override
	public boolean canPropose(Entry<?> obfEntry) {
		return obfEntry instanceof LocalVariableEntry paramEntry && this.index.getLinkedField(paramEntry) != null;
	}

	@Override
	public Optional<String> proposeName(Entry<?> obfEntry, EntryRemapper remapper) {
		if (obfEntry instanceof LocalVariableEntry paramEntry) {
			FieldEntry linkedField = this.index.getLinkedField(paramEntry);

			var deobfField = remapper.extendedDeobfuscate(linkedField);

			if (deobfField != null && deobfField.isDeobfuscated()) {
				return Optional.of(deobfField.getValue().getName());
			} else {
				return Optional.of(linkedField.getName());
			}
		}

		return Optional.empty();
	}
}

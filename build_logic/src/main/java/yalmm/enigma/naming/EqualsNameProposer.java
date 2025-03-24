package yalmm.enigma.naming;

import cuchaz.enigma.translation.mapping.EntryRemapper;
import cuchaz.enigma.translation.representation.MethodDescriptor;
import cuchaz.enigma.translation.representation.entry.Entry;
import cuchaz.enigma.translation.representation.entry.LocalVariableEntry;

import java.util.Optional;

public class EqualsNameProposer extends NameProposer {
	private static final MethodDescriptor EQUALS_DESCRIPTOR = new MethodDescriptor("(Ljava/lang/Object;)Z");

	@Override
	public boolean canPropose(Entry<?> obfEntry) {
		if (obfEntry instanceof LocalVariableEntry paramEntry) {
			var parent = paramEntry.getParent();
			if (parent == null) {
				return false;
			}

			String methodName = parent.getName();
			return methodName.equals("equals") && parent.getDesc().equals(EQUALS_DESCRIPTOR);
		}

		return false;
	}

	@Override
	public Optional<String> proposeName(Entry<?> obfEntry, EntryRemapper remapper) {
		return Optional.of("other");
	}
}

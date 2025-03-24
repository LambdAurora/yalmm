package yalmm.enigma;

import cuchaz.enigma.api.service.EnigmaServiceContext;
import cuchaz.enigma.api.service.NameProposalService;
import cuchaz.enigma.translation.mapping.EntryRemapper;
import cuchaz.enigma.translation.representation.entry.Entry;
import yalmm.enigma.naming.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NameProposerService implements NameProposalService {
	private final List<NameProposer> nameProposers = new ArrayList<>();

	public NameProposerService(JarIndexer indexer, EnigmaServiceContext<NameProposalService> context) {
		this.nameProposers.add(new EqualsNameProposer());
		this.nameProposers.add(new ConstructorParametersNameProposer(indexer));
		this.nameProposers.add(new SetterParamNameProposer(indexer));
		this.nameProposers.add(new SimpleTypeParamNameProposer(indexer));
	}

	@Override
	public Optional<String> proposeName(Entry<?> obfEntry, EntryRemapper remapper) {
		for (var proposer : this.nameProposers) {
			if (proposer.canPropose(obfEntry)) {
				var name = proposer.proposeName(obfEntry, remapper);

				if (name.isPresent()) {
					return name;
				}
			}
		}

		return Optional.empty();
	}
}

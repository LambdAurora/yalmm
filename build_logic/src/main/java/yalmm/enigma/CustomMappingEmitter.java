package yalmm.enigma;

import cuchaz.enigma.EnigmaProject;
import cuchaz.enigma.api.service.NameProposalService;
import cuchaz.enigma.translation.ProposingTranslator;
import cuchaz.enigma.translation.mapping.EntryMapping;
import cuchaz.enigma.translation.mapping.tree.EntryTree;
import cuchaz.enigma.translation.representation.entry.Entry;

import java.util.Optional;

public class CustomMappingEmitter {
	private final YalmmEnigmaPlugin plugin;
	private final EnigmaProject project;
	private final ProposingTranslator baseTranslator;

	public CustomMappingEmitter(YalmmEnigmaPlugin plugin, EnigmaProject project) {
		this.plugin = plugin;
		this.project = project;
		var nameProposerServices = project.getEnigma().getServices()
				.get(NameProposalService.TYPE)
				.toArray(NameProposalService[]::new);
		this.baseTranslator = new ProposingTranslator(project.getMapper(), nameProposerServices);
	}

	private Optional<EntryMapping> resolveMappingFor(Entry<?> entry) {
		EntryMapping mapping = null;
		var translated = this.baseTranslator.extendedTranslate(entry);

		if (translated != null && translated.isProposed()) {
			mapping = new EntryMapping(translated.getValue().getName());
		}

		return Optional.ofNullable(mapping);
	}

	public void fillMappings(EntryTree<EntryMapping> result) {
		for (var node : this.plugin.getIndexer().localCandidates) {
			this.resolveMappingFor(node).ifPresent(mapping -> result.insert(node, mapping));
		}
	}
}

package yalmm.enigma;

import cuchaz.enigma.EnigmaProject;
import cuchaz.enigma.api.EnigmaPlugin;
import cuchaz.enigma.api.EnigmaPluginContext;
import cuchaz.enigma.api.service.JarIndexerService;
import cuchaz.enigma.api.service.NameProposalService;

public class YalmmEnigmaPlugin implements EnigmaPlugin {
	private final JarIndexer indexer = new JarIndexer();

	@Override
	public void init(EnigmaPluginContext context) {
		context.registerService(
				"yalmm:jar_indexer", JarIndexerService.TYPE,
				this.indexer::withContext
		);
		context.registerService(
				"yalmm:name_proposal", NameProposalService.TYPE,
				ctx -> new NameProposerService(this.indexer, ctx)
		);
	}

	public JarIndexer getIndexer() {
		return this.indexer;
	}

	public CustomMappingEmitter newMappingEmitter(EnigmaProject project) {
		return new CustomMappingEmitter(this, project);
	}
}

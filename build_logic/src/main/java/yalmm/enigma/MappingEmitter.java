package yalmm.enigma;

import cuchaz.enigma.Enigma;
import cuchaz.enigma.EnigmaProject;
import cuchaz.enigma.ProgressListener;
import cuchaz.enigma.analysis.index.BridgeMethodIndex;
import cuchaz.enigma.api.service.NameProposalService;
import cuchaz.enigma.classprovider.CombiningClassProvider;
import cuchaz.enigma.command.MappingCommandsUtil;
import cuchaz.enigma.translation.ProposingTranslator;
import cuchaz.enigma.translation.Translator;
import cuchaz.enigma.translation.mapping.EntryMapping;
import cuchaz.enigma.translation.mapping.serde.MappingFileNameFormat;
import cuchaz.enigma.translation.mapping.serde.MappingParseException;
import cuchaz.enigma.translation.mapping.serde.MappingSaveParameters;
import cuchaz.enigma.translation.mapping.tree.EntryTree;
import cuchaz.enigma.translation.mapping.tree.HashEntryTree;
import cuchaz.enigma.translation.representation.entry.Entry;
import cuchaz.enigma.translation.representation.entry.MethodEntry;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

public class MappingEmitter {
	private final EnigmaProject project;
	private final YalmmEnigmaPlugin plugin;
	private final EntryTree<EntryMapping> result = new HashEntryTree<>();
	private final Translator baseTranslator;

	public MappingEmitter(EnigmaProject project, YalmmEnigmaPlugin plugin) {
		this.project = project;
		this.plugin = plugin;

		var nameProposerServices = project.getEnigma().getServices().get(NameProposalService.TYPE).toArray(NameProposalService[]::new);
		this.baseTranslator = new ProposingTranslator(project.getMapper(), nameProposerServices);
	}

	public static EnigmaProject openProject(Enigma enigma, Path jarPath, Path mappingsPath, ProgressListener progress)
			throws IOException, MappingParseException {
		var project = enigma.openJar(jarPath, new CombiningClassProvider(), progress);
		var saveParameters = new MappingSaveParameters(MappingFileNameFormat.BY_DEOBF);
		var rawSource = MappingCommandsUtil.read("enigma", mappingsPath, saveParameters);
		project.setMappings(rawSource);
		return project;
	}

	private Optional<EntryMapping> resolveMappingFor(Entry<?> entry) {
		EntryMapping mapping = null;
		var translated = this.baseTranslator.extendedTranslate(entry);

		if (translated != null && translated.isProposed()) {
			mapping = new EntryMapping(translated.getValue().getName());
		}

		return Optional.ofNullable(mapping);
	}

	private void fillProposedNames(Collection<? extends Entry<?>> entries) {
		for (var node : entries) {
			this.resolveMappingFor(node).ifPresent(mapping -> this.result.insert(node, mapping));
		}
	}

	private void fillProposedMethodNames(Collection<MethodEntry> entries) {
		for (var node : this.project.getJarIndex().getEntryIndex().getMethods()) {
			if (!this.getBridgeMethodIndex().isSpecializedMethod(node)) {
				this.resolveMappingFor(node).ifPresent(mapping -> this.result.insert(node, mapping));
			}
		}

		for (var node : this.plugin.getIndexer().constructorParametersIndex.allParameters()) {
			this.resolveMappingFor(node).ifPresent(mapping -> this.result.insert(node, mapping));
		}
	}

	public void fillMappings() {
		for (var node : this.project.getMapper().getObfToDeobf()) {
			// Copy all non-specialized methods and other entries
			if (!(node.getEntry() instanceof MethodEntry method) || !this.getBridgeMethodIndex().isSpecializedMethod(method)) {
				result.insert(node.getEntry(), node.getValue());
			}
		}

		this.fillProposedNames(this.project.getJarIndex().getEntryIndex().getClasses());
		this.fillProposedNames(this.project.getJarIndex().getEntryIndex().getFields());
		this.fillProposedMethodNames(this.project.getJarIndex().getEntryIndex().getMethods());
	}

	public EntryTree<EntryMapping> result() {
		return this.result;
	}

	private BridgeMethodIndex getBridgeMethodIndex() {
		return this.project.getJarIndex().getBridgeMethodIndex();
	}
}

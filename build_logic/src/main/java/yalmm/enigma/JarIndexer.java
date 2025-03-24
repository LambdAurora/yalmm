package yalmm.enigma;

import cuchaz.enigma.analysis.index.JarIndex;
import cuchaz.enigma.api.service.EnigmaServiceContext;
import cuchaz.enigma.api.service.JarIndexerService;
import cuchaz.enigma.classprovider.ClassProvider;
import cuchaz.enigma.translation.representation.MethodDescriptor;
import cuchaz.enigma.translation.representation.entry.ClassEntry;
import cuchaz.enigma.translation.representation.entry.LocalVariableEntry;
import cuchaz.enigma.translation.representation.entry.MethodEntry;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import yalmm.enigma.index.ConstructorParametersIndex;
import yalmm.enigma.index.GetterSetterIndex;
import yalmm.enigma.index.SimpleTypeSingleIndex;
import yalmm.enigma.index.YalmmIndexer;
import yalmm.util.Descriptors;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class JarIndexer implements JarIndexerService {
	public static final String SIMPLE_TYPE_FIELD_NAMES_PATH = "simple_type_field_names_path";

	public final ConstructorParametersIndex constructorParametersIndex = new ConstructorParametersIndex();
	public final GetterSetterIndex getterSetterIndex = new GetterSetterIndex();
	public final SimpleTypeSingleIndex simpleTypeSingleIndex = new SimpleTypeSingleIndex();
	public final Set<LocalVariableEntry> localCandidates = new HashSet<>();

	private final List<YalmmIndexer> indexers = List.of(
			this.constructorParametersIndex,
			this.getterSetterIndex,
			this.simpleTypeSingleIndex
	);

	public JarIndexer withContext(EnigmaServiceContext<JarIndexerService> context) {
		this.simpleTypeSingleIndex.loadRegistry(context.getArgument(SIMPLE_TYPE_FIELD_NAMES_PATH)
				.map(Path::of).orElse(null));
		return this;
	}

	@Override
	public void acceptJar(Set<String> scope, ClassProvider classProvider, JarIndex jarIndex) {
		for (String className : scope) {
			ClassNode node = classProvider.get(className);

			if (node != null) {
				for (var index : this.indexers) {
					index.visitClassNode(classProvider, node, jarIndex);
				}
			}
		}

		record RichMethodEntry(MethodNode methodNode, MethodEntry mappingEntry) {}

		var locals = scope.parallelStream()
				.map(classProvider::get)
				.filter(Objects::nonNull)
				.flatMap(classNode -> {
					var classEntry = new ClassEntry(classNode.name);
					return classNode.methods.stream()
							.map(methodNode -> new RichMethodEntry(
									methodNode,
									new MethodEntry(classEntry, methodNode.name, new MethodDescriptor(methodNode.desc))
							));
				}).filter(method ->
						!jarIndex.getBridgeMethodIndex().isSpecializedMethod(method.mappingEntry)
				).flatMap(method -> Descriptors.getParameters(method.methodNode)
						.stream()
						.map(param -> new LocalVariableEntry(
								method.mappingEntry, param.lvtIndex(), "", true, null
						))
				).toList();
		this.localCandidates.addAll(locals);
	}
}

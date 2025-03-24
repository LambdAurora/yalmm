package yalmm.enigma.index;


import cuchaz.enigma.analysis.index.JarIndex;
import cuchaz.enigma.classprovider.ClassProvider;
import cuchaz.enigma.translation.representation.MethodDescriptor;
import cuchaz.enigma.translation.representation.entry.ClassEntry;
import cuchaz.enigma.translation.representation.entry.LocalVariableEntry;
import cuchaz.enigma.translation.representation.entry.MethodEntry;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;
import yalmm.enigma.index.SimpleTypeParamNamesRegistry.Name;
import yalmm.util.AsmUtils;
import yalmm.util.Descriptors;

import java.nio.file.Path;
import java.util.*;

/**
 * Index of fields/local variables that are of a rather simple type (as-in easy to guess the variable name) and which
 * they are entirely unique within their context (no other fields/local vars in the same scope have the same type).
 *
 * @author LambdAurora
 */
public class SimpleTypeSingleIndex implements YalmmIndexer, Opcodes {
	private final Map<ParameterEntry, String> parameters = new HashMap<>();
	private SimpleTypeParamNamesRegistry registry;

	public void loadRegistry(Path path) {
		if (path == null) {
			this.registry = null;
			return;
		}

		this.registry = new SimpleTypeParamNamesRegistry(path);
		this.registry.read();
	}

	public boolean isEnabled() {
		return this.registry != null;
	}

	public @Nullable String getParam(ParameterEntry paramEntry) {
		return this.parameters.get(paramEntry);
	}

	@TestOnly
	public List<ParameterEntry> getParamsOf(MethodEntry methodEntry) {
		var params = new ArrayList<ParameterEntry>();

		this.parameters.forEach((param, name) -> {
			if (param.parent.equals(methodEntry)) {
				params.add(param);
			}
		});

		return params;
	}

	@Override
	public void visitClassNode(ClassProvider provider, ClassNode classNode, JarIndex jarIndex) {
		if (!this.isEnabled()) return;

		var parentEntry = new ClassEntry(classNode.name);

		for (var method : classNode.methods) {
			if (method.parameters == null) continue;

			var methodDescriptor = new MethodDescriptor(method.desc);
			var methodEntry = new MethodEntry(parentEntry, method.name, methodDescriptor);
			var parameters = Descriptors.getParameters(method);

			var types = new HashMap<Type, Integer>();

			for (var param : parameters) {
				types.compute(param.type(), (t, old) -> {
					if (old == null) return 0;
					else return old + 1;
				});
			}

			var bannedTypes = new HashSet<Type>();
			types.forEach((type, amount) -> {
				if (amount > 1) bannedTypes.add(type);
			});

			this.collectMatchingParameters(method, bannedTypes, parameters).forEach((name, param) -> {
				if (!param.isNull()) {
					boolean isStatic = AsmUtils.maskMatch(method.access, ACC_STATIC);
					var paramEntry = new ParameterEntry(methodEntry, param.index() + (isStatic ? 0 : 1));
					this.parameters.put(paramEntry, name);
				}
			});
		}
	}

	private Map<String, ParameterBuildingEntry> collectMatchingParameters(
			MethodNode method, Set<Type> bannedTypes,
			List<Descriptors.ParameterEntry> parameters
	) {
		var knownParameters = new HashMap<String, ParameterBuildingEntry>();

		for (int index = 0, lvtIndex = 0; index < method.parameters.size(); index++) {
			if (index > 0) lvtIndex += parameters.get(index - 1).getSize();

			if (bannedTypes.contains(parameters.get(index).type())) continue;

			ParameterNode node = method.parameters.get(index);
			String desc = parameters.get(index).getDescriptor();
			if (desc.charAt(0) != 'L') continue;
			String type = desc.substring(1, desc.length() - 1);

			var entry = this.registry.getEntry(type);
			if (entry != null) {
				ParameterBuildingEntry existingEntry = knownParameters.get(entry.name().local());

				if (existingEntry != null) {
					if (existingEntry.entry() == entry) {
						knownParameters.put(entry.name().local(), ParameterBuildingEntry.createNull(entry));
						continue;
					}

					Name foundFallback = entry.findFallback(fallback -> !knownParameters.containsKey(fallback.local()));

					if (foundFallback != null) {
						knownParameters.put(foundFallback.local(), new ParameterBuildingEntry(node, lvtIndex, entry));

						if (!existingEntry.isNull() && existingEntry.entry().exclusive()) {
							Name replacement = existingEntry.entry().findFallback(
									fallback -> !knownParameters.containsKey(fallback.local())
							);

							knownParameters.put(entry.name().local(), ParameterBuildingEntry.createNull(entry));

							if (replacement != null) {
								knownParameters.put(replacement.local(), new ParameterBuildingEntry(
										existingEntry.node(), existingEntry.index(), existingEntry.entry()
								));
							}
						}
					} else {
						knownParameters.put(entry.name().local(), ParameterBuildingEntry.createNull(entry));
					}
				} else {
					knownParameters.put(entry.name().local(), new ParameterBuildingEntry(node, lvtIndex, entry));
				}
			}
		}

		return knownParameters;
	}

	private record FieldBuildingEntry(FieldNode node, Name name, SimpleTypeParamNamesRegistry.Entry entry) {
		public static final FieldBuildingEntry NULL = new FieldBuildingEntry(null, null, null);
	}

	private record ParameterBuildingEntry(ParameterNode node, int index, SimpleTypeParamNamesRegistry.Entry entry) {
		public static ParameterBuildingEntry createNull(SimpleTypeParamNamesRegistry.Entry entry) {
			return new ParameterBuildingEntry(null, -1, entry);
		}

		public boolean isNull() {
			return this.node == null;
		}
	}

	public record ParameterEntry(MethodEntry parent, int index) {
		public static ParameterEntry fromLocalVariableEntry(LocalVariableEntry entry) {
			return new ParameterEntry(entry.getParent(), entry.getIndex());
		}
	}
}
package yalmm.enigma.index;

import cuchaz.enigma.analysis.index.JarIndex;
import cuchaz.enigma.classprovider.ClassProvider;
import cuchaz.enigma.translation.mapping.EntryRemapper;
import cuchaz.enigma.translation.representation.MethodDescriptor;
import cuchaz.enigma.translation.representation.TypeDescriptor;
import cuchaz.enigma.translation.representation.entry.ClassEntry;
import cuchaz.enigma.translation.representation.entry.FieldEntry;
import cuchaz.enigma.translation.representation.entry.LocalVariableEntry;
import cuchaz.enigma.translation.representation.entry.MethodEntry;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import yalmm.util.Descriptors;

import java.util.*;

/**
 * Represents an index of constructor parameters which are linked to a field.
 *
 * @author LambdAurora
 */
public class ConstructorParametersIndex implements YalmmIndexer {
	private final Map<LocalVariableEntry, NameResolver> entries = new HashMap<>();
	private final Map<MethodEntry, Map<LocalVariableEntry, NameResolver>> byMethod = new HashMap<>();
	private final Map<MethodEntry, List<SuperCallResolver>> superCallsPendingResolve = new HashMap<>();

	@Override
	public void visitClassNode(ClassProvider classProvider, ClassNode classNode, JarIndex jarIndex) {
		for (var method : classNode.methods) {
			if (method.name.equals("<init>")) {
				this.visitConstructor(classProvider, classNode, method);
			}
		}
	}

	private void put(LocalVariableEntry param, NameResolver field) {
		this.entries.put(param, field);
		this.byMethod.compute(
				param.getParent(),
				(key, existing) -> existing == null ? new HashMap<>() : existing
		).put(param, field);
	}

	private void visitConstructor(
			ClassProvider classProvider, ClassNode classNode, MethodNode constructorNode
	) {
		var classEntry = new ClassEntry(classNode.name);
		var methodEntry = new MethodEntry(classEntry, constructorNode.name, new MethodDescriptor(constructorNode.desc));

		var parameters = Descriptors.getParameters(constructorNode);
		if (parameters.isEmpty()) return;
		/*if (this.callToCanonical(classNode, constructorNode)) {
			// @TODO Handle non-canonical constructors one day, as not every field will be present.
		}*/

		boolean isFirstLabel = true;
		boolean isFirstInvokeSpecial = true;

		for (var inst : constructorNode.instructions) {
			// Search for every field assignation.
			if (inst instanceof FieldInsnNode fieldInst && inst.getOpcode() == Opcodes.PUTFIELD) {
				if (!fieldInst.owner.equals(classNode.name)) continue; // The owner isn't this class.

				var previousInst = fieldInst.getPrevious();
				if (previousInst.getOpcode() >= Opcodes.ILOAD && previousInst.getOpcode() <= Opcodes.ALOAD) {
					var loadInst = (VarInsnNode) previousInst;

					if (parameters.getLast().lvtIndex() < loadInst.var)
						continue; // This load opcode does not correspond to a parameter.

					var param = new LocalVariableEntry(methodEntry, loadInst.var, "", true, null);
					var field = new FieldEntry(classEntry, fieldInst.name, new TypeDescriptor(fieldInst.desc));
					this.put(param, new ByFieldResolver(param, field));
				}
			} else if (
					inst instanceof MethodInsnNode methodInst
							&& methodInst.getOpcode() == Opcodes.INVOKESPECIAL
							&& methodInst.name.equals("<init>")
							&& isFirstInvokeSpecial
			) {
				this.visitPotentialSuperCall(classProvider, methodEntry, methodInst);
				isFirstInvokeSpecial = false;
			} else if (inst instanceof LabelNode) {
				if (isFirstLabel) {
					isFirstLabel = false;
				} else {
					isFirstInvokeSpecial = false;
				}
			}
		}

		var pendingForCurrent = this.superCallsPendingResolve.get(methodEntry);
		if (pendingForCurrent != null) {
			var resolved = this.byMethod.get(methodEntry);

			if (resolved != null) {
				for (var pending : pendingForCurrent) {
					pending.resolve(this, resolved);
				}
			}

			// Cleanup.
			this.superCallsPendingResolve.remove(methodEntry);
		}
	}

	private void visitPotentialSuperCall(
			ClassProvider classProvider, MethodEntry constructorEntry, MethodInsnNode methodInst
	) {
		ClassNode owner = classProvider.get(methodInst.owner);
		if (owner == null) return;

		var calledMethod = owner.methods.stream().filter(method -> method.name.equals("<init>")
				&& method.desc.equals(methodInst.desc)).findFirst();
		if (calledMethod.isEmpty()) return;

		var superClassEntry = new ClassEntry(methodInst.owner);
		var superMethodEntry = new MethodEntry(superClassEntry, methodInst.name, new MethodDescriptor(methodInst.desc));

		var superParameters = Descriptors.getParameters(calledMethod.get());
		if (superParameters.isEmpty()) return;

		var paramMapping = new HashMap<LocalVariableEntry, LocalVariableEntry>();
		int currentIndex = 0;

		AbstractInsnNode previousInst;
		while ((previousInst = methodInst.getPrevious()) != null) {
			if (previousInst.getOpcode() >= Opcodes.ILOAD && previousInst.getOpcode() <= Opcodes.ALOAD) {
				if (currentIndex >= superParameters.size()) break;

				var superParam = superParameters.get(currentIndex++);
				var param = new LocalVariableEntry(
						constructorEntry, ((VarInsnNode) previousInst).var, "", true, null
				);
				paramMapping.put(param, new LocalVariableEntry(
						superMethodEntry, superParam.lvtIndex(), "", true, null
				));
			} else {
				break;
			}
		}

		// We now can attempt to resolve.
		var resolver = new SuperCallResolver(constructorEntry, paramMapping);
		var existing = this.byMethod.get(superMethodEntry);
		if (existing != null) {
			// Resolve now.
			resolver.resolve(this, existing);
		} else {
			this.superCallsPendingResolve
					.computeIfAbsent(superMethodEntry, key -> new ArrayList<>())
					.add(resolver);
		}
	}

	/**
	 * Gets the linked field of the given parameter.
	 *
	 * @param entry the parameter
	 * @return the field
	 */
	public NameResolver getLinkedField(LocalVariableEntry entry) {
		return this.entries.get(entry);
	}

	/**
	 * {@return all the indexed parameters}
	 */
	public Collection<LocalVariableEntry> allParameters() {
		return this.entries.keySet();
	}

	private boolean callToCanonical(ClassNode classNode, MethodNode constructorNode) {
		for (var inst : constructorNode.instructions) {
			if (inst.getOpcode() == Opcodes.INVOKESPECIAL && inst instanceof MethodInsnNode instNode) {
				if (instNode.owner.equals(classNode.name) && instNode.name.equals("<init>")) {
					return true;
				}
			}
		}
		return false;
	}

	public interface NameResolver {
		LocalVariableEntry param();

		Optional<String> resolve(EntryRemapper remapper);
	}

	public record ByFieldResolver(LocalVariableEntry param, FieldEntry field) implements NameResolver {
		@Override
		public Optional<String> resolve(EntryRemapper remapper) {
			var deobfField = remapper.extendedDeobfuscate(this.field);

			if (deobfField != null && deobfField.isDeobfuscated()) {
				return Optional.of(deobfField.getValue().getName());
			} else {
				return Optional.of(this.field.getName());
			}
		}
	}

	public record BySuperResolver(NameResolver parent) implements NameResolver {
		@Override
		public LocalVariableEntry param() {
			return this.parent.param();
		}

		@Override
		public Optional<String> resolve(EntryRemapper remapper) {
			return this.resolveFromParentParam(remapper).or(() -> this.parent.resolve(remapper));
		}

		private Optional<String> resolveFromParentParam(EntryRemapper remapper) {
			var parent = remapper.extendedDeobfuscate(this.parent.param());

			if (parent.isDeobfuscated()) {
				return Optional.of(parent.getValue().getName());
			} else {
				return Optional.empty();
			}
		}
	}

	private record SuperCallResolver(
			MethodEntry constructorEntry,
			Map<LocalVariableEntry, LocalVariableEntry> paramMapping
	) {
		public void resolve(
				ConstructorParametersIndex index, Map<LocalVariableEntry, NameResolver> existing
		) {
			this.paramMapping.forEach((param, superParam) -> {
				var found = existing.get(superParam);
				if (found != null) {
					index.put(param, new BySuperResolver(found));
				}
			});
		}
	}
}

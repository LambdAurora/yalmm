package yalmm.enigma.index;

import cuchaz.enigma.analysis.index.JarIndex;
import cuchaz.enigma.classprovider.ClassProvider;
import cuchaz.enigma.translation.representation.MethodDescriptor;
import cuchaz.enigma.translation.representation.TypeDescriptor;
import cuchaz.enigma.translation.representation.entry.ClassEntry;
import cuchaz.enigma.translation.representation.entry.FieldEntry;
import cuchaz.enigma.translation.representation.entry.LocalVariableEntry;
import cuchaz.enigma.translation.representation.entry.MethodEntry;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import yalmm.util.AsmUtils;
import yalmm.util.Descriptors;

import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.ACC_NATIVE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

public class GetterSetterIndex implements YalmmIndexer {
	private final Map<MethodEntry, FieldEntry> linked = new HashMap<>();
	private final Map<LocalVariableEntry, FieldEntry> linkedSetterParams = new HashMap<>();

	@Override
	public void visitClassNode(ClassProvider classProvider, ClassNode classNode, JarIndex jarIndex) {
		var classEntry = new ClassEntry(classNode.name);

		for (var method : classNode.methods) {
			var descriptor = new MethodDescriptor(method.desc);
			var methodEntry = new MethodEntry(classEntry, method.name, descriptor);

			if (jarIndex.getBridgeMethodIndex().isSpecializedMethod(methodEntry)) {
				return;
			}

			if (!AsmUtils.matchAccess(method, ACC_STATIC) && !AsmUtils.matchAccess(method, ACC_NATIVE)) {
				if (descriptor.getReturnDesc().equals(Descriptors.VOID_TYPE)
						&& descriptor.getArgumentDescs().size() == 1) { // Potential setter.
					if (descriptor.getArgumentDescs().get(0).equals(Descriptors.BOOLEAN_TYPE)) {
						continue; // Ignore booleans for now.
					}

					AsmUtils.getFieldFromSetter(classNode, method)
							.ifPresent(field -> {
								this.linkField(classNode, method, descriptor, field);
							});
				} else { // Potential getter.
					if (descriptor.getReturnDesc().equals(Descriptors.BOOLEAN_TYPE)) {
						continue; // Ignore booleans for now.
					}

					AsmUtils.getFieldFromGetter(classNode, method)
							.ifPresent(field -> {
								this.linkField(classNode, method, descriptor, field);
							});
				}
			}
		}
	}

	private void linkField(ClassNode classNode, MethodNode methodNode, MethodDescriptor descriptor, FieldNode fieldNode) {
		var classEntry = new ClassEntry(classNode.name);
		var methodEntry = new MethodEntry(classEntry, methodNode.name, descriptor);
		var fieldEntry = new FieldEntry(classEntry, fieldNode.name, new TypeDescriptor(fieldNode.desc));

		this.linked.put(methodEntry, fieldEntry);

		if (descriptor.getArgumentDescs().size() == 1) {
			var paramEntry = new LocalVariableEntry(methodEntry, 1, "", true, null);
			this.linkedSetterParams.put(paramEntry, fieldEntry);
		}
	}

	public FieldEntry getLinkedField(MethodEntry method) {
		return this.linked.get(method);
	}

	public FieldEntry getLinkedField(LocalVariableEntry param) {
		return this.linkedSetterParams.get(param);
	}
}

package yalmm.enigma.index;

import cuchaz.enigma.translation.representation.MethodDescriptor;
import cuchaz.enigma.translation.representation.TypeDescriptor;
import cuchaz.enigma.translation.representation.entry.ClassEntry;
import cuchaz.enigma.translation.representation.entry.FieldEntry;
import cuchaz.enigma.translation.representation.entry.LocalVariableEntry;
import cuchaz.enigma.translation.representation.entry.MethodEntry;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import yalmm.util.Descriptors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an index of constructor parameters which are linked to a field.
 *
 * @author LambdAurora
 */
public class ConstructorParametersIndex {
	private final Map<LocalVariableEntry, FieldEntry> entries = new HashMap<>();

	public void visitClassNode(ClassNode classNode) {
		for (var method : classNode.methods) {
			if (method.name.equals("<init>")) {
				this.visitConstructor(classNode, method);
			}
		}
	}

	private void visitConstructor(ClassNode classNode, MethodNode constructorNode) {
		var classEntry = new ClassEntry(classNode.name);
		var methodEntry = new MethodEntry(classEntry, constructorNode.name, new MethodDescriptor(constructorNode.desc));

		var parameters = Descriptors.getParameters(constructorNode);
		if (parameters.isEmpty()) return;
		/*if (this.callToCanonical(classNode, constructorNode)) {
			// @TODO Handle non-canonical constructors one day, as not every field will be present.
		}*/

		for (var inst : constructorNode.instructions) {
			// Search for every field assignation.
			if (inst.getOpcode() == Opcodes.PUTFIELD) {
				var fieldInst = (FieldInsnNode) inst;

				if (!fieldInst.owner.equals(classNode.name)) continue; // The owner isn't this class.

				var previousInst = fieldInst.getPrevious();
				if (previousInst.getOpcode() >= Opcodes.ILOAD && previousInst.getOpcode() <= Opcodes.ALOAD) {
					var loadInst = (VarInsnNode) previousInst;

					if (parameters.getLast().lvtIndex() < loadInst.var)
						continue; // This load opcode does not correspond to a parameter.

					var param = new LocalVariableEntry(methodEntry, loadInst.var, "", true, null);
					var field = new FieldEntry(classEntry, fieldInst.name, new TypeDescriptor(fieldInst.desc));
					this.entries.put(param, field);
				}
			}
		}
	}

	/**
	 * Gets the linked field of the given parameter.
	 *
	 * @param entry the parameter
	 * @return the field
	 */
	public FieldEntry getLinkedField(LocalVariableEntry entry) {
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
}
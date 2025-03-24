package yalmm.util;

import cuchaz.enigma.translation.representation.MethodDescriptor;
import cuchaz.enigma.translation.representation.TypeDescriptor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

public class Descriptors {
	public static final TypeDescriptor VOID_TYPE = new TypeDescriptor("V");
	public static final TypeDescriptor BOOLEAN_TYPE = new TypeDescriptor("Z");

	public static MethodDescriptor getDescriptor(MethodNode node) {
		return new MethodDescriptor(node.desc);
	}

	public static List<Type> getParameterTypes(MethodNode node) {
		return List.of(Type.getArgumentTypes(node.desc));
	}

	public static List<Type> getParameterTypes(MethodInsnNode node) {
		return List.of(Type.getArgumentTypes(node.desc));
	}

	public static List<ParameterEntry> getParameters(MethodNode node) {
		int lvtIndex = AsmUtils.matchAccess(node, Opcodes.ACC_STATIC) ? 0 : 1;
		var parameters = new ArrayList<ParameterEntry>();
		for (var type : getParameterTypes(node)) {
			parameters.add(new ParameterEntry(node, lvtIndex, type));
			lvtIndex += type.getSize();
		}
		return parameters;
	}

	public record ParameterEntry(MethodNode method, int lvtIndex, Type type) {
		public int getSize() {
			return this.type.getSize();
		}

		public String getDescriptor() {
			return this.type.getDescriptor();
		}
	}
}

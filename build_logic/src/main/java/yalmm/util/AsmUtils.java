package yalmm.util;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Optional;
import java.util.stream.Stream;

public final class AsmUtils {
	public static boolean maskMatch(int value, int... masks) {
		boolean matched = true;

		for (int mask : masks) {
			matched &= (value & mask) != 0;
		}

		return matched;
	}

	public static boolean matchAccess(FieldNode node, int... masks) {
		return maskMatch(node.access, masks);
	}

	public static boolean matchAccess(MethodNode node, int... masks) {
		return maskMatch(node.access, masks);
	}

	public static Optional<FieldNode> getField(ClassNode node, String name, String desc) {
		for (var field : node.fields) {
			if (field.name.equals(name) && field.desc.equals(desc)) {
				return Optional.of(field);
			}
		}

		return Optional.empty();
	}

	public static Optional<MethodNode> getMethod(ClassNode node, String name, String desc) {
		for (var method : node.methods) {
			if (method.name.equals(name) && method.desc.equals(desc)) {
				return Optional.of(method);
			}
		}

		return Optional.empty();
	}

	public static Optional<FieldNode> getFieldFromGetter(ClassNode classNode, MethodNode node) {
		if (!Descriptors.getDescriptor(node).getArgumentDescs().isEmpty()) return Optional.empty();

		var instructions = Stream.of(node.instructions.toArray())
				.filter(opcode -> !(opcode instanceof LineNumberNode || opcode instanceof LabelNode))
				.toList();

		if (instructions.size() != 3) return Optional.empty();
		if (instructions.get(0).getOpcode() != Opcodes.ALOAD) return Optional.empty();
		var getFieldNode = instructions.get(1);
		if (getFieldNode.getOpcode() != Opcodes.GETFIELD) return Optional.empty();

		var fieldInsnNode = (FieldInsnNode) getFieldNode;

		int expectedReturnOpcode = switch (fieldInsnNode.desc) {
			case "I" -> Opcodes.IRETURN;
			case "L" -> Opcodes.LRETURN;
			case "F" -> Opcodes.FRETURN;
			case "D" -> Opcodes.DRETURN;
			default -> Opcodes.ARETURN;
		};

		if (instructions.get(2).getOpcode() != expectedReturnOpcode) return Optional.empty();

		if (fieldInsnNode.owner.equals(classNode.name)) {
			return getField(classNode, fieldInsnNode.name, fieldInsnNode.desc);
		} else {
			return Optional.empty();
		}
	}

	public static Optional<FieldNode> getFieldFromSetter(ClassNode classNode, MethodNode node) {
		if (Descriptors.getDescriptor(node).getArgumentDescs().size() != 1) return Optional.empty();

		var instructions = Stream.of(node.instructions.toArray())
				.filter(opcode -> !(opcode instanceof LineNumberNode || opcode instanceof LabelNode))
				.toList();

		if (instructions.size() != 4) return Optional.empty();
		if (instructions.get(0).getOpcode() != Opcodes.ALOAD) return Optional.empty();
		if (instructions.get(3).getOpcode() != Opcodes.RETURN) return Optional.empty();
		var putFieldNode = instructions.get(2);
		if (putFieldNode.getOpcode() != Opcodes.PUTFIELD) return Optional.empty();

		var fieldInsnNode = (FieldInsnNode) putFieldNode;

		int expectedLoadOpcode = switch (fieldInsnNode.desc) {
			case "I" -> Opcodes.ILOAD;
			case "L" -> Opcodes.LLOAD;
			case "F" -> Opcodes.FLOAD;
			case "D" -> Opcodes.DLOAD;
			default -> Opcodes.ALOAD;
		};

		if (instructions.get(1).getOpcode() != expectedLoadOpcode) return Optional.empty();

		if (fieldInsnNode.owner.equals(classNode.name)) {
			return getField(classNode, fieldInsnNode.name, fieldInsnNode.desc);
		} else {
			return Optional.empty();
		}
	}
}

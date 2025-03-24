package yalmm.enigma.index;

import cuchaz.enigma.analysis.index.JarIndex;
import cuchaz.enigma.classprovider.ClassProvider;
import org.objectweb.asm.tree.ClassNode;

public interface YalmmIndexer {
	void visitClassNode(
			ClassProvider classProvider, ClassNode node, JarIndex jarIndex
	);
}

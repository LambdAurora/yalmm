package yalmm.enigma;

import cuchaz.enigma.analysis.index.JarIndex;
import cuchaz.enigma.api.service.JarIndexerService;
import cuchaz.enigma.classprovider.ClassProvider;
import org.objectweb.asm.tree.ClassNode;
import yalmm.enigma.index.ConstructorParametersIndex;

import java.util.Set;

public class JarIndexer implements JarIndexerService {
	public final ConstructorParametersIndex constructorParametersIndex = new ConstructorParametersIndex();

	@Override
	public void acceptJar(Set<String> scope, ClassProvider classProvider, JarIndex jarIndex) {
		for (String className : scope) {
			ClassNode node = classProvider.get(className);

			if (node != null) {
				constructorParametersIndex.visitClassNode(node);
			}
		}
	}
}

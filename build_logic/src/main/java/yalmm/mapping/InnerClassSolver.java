package yalmm.mapping;

import cuchaz.enigma.translation.mapping.EntryMapping;
import cuchaz.enigma.translation.mapping.tree.EntryTree;
import cuchaz.enigma.translation.representation.entry.ClassEntry;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class InnerClassSolver {
	private final InnerClassSolver parent;
	private final ClassEntry node;
	private String knownName;
	private final Map<ClassEntry, InnerClassSolver> innerClasses = new HashMap<>();

	public InnerClassSolver(InnerClassSolver parent, ClassEntry node) {
		this.parent = parent;
		this.node = node;
	}

	public ClassEntry node() {
		return this.node;
	}

	public void propagateKnownName(String knownName) {
		if (this.parent == null) {
			this.knownName = knownName;
		} else {
			int lastPartSeparator = knownName.lastIndexOf('$');
			this.knownName = knownName.substring(lastPartSeparator + 1);

			this.parent.propagateKnownName(knownName.substring(0, lastPartSeparator));
		}
	}

	public void setKnownName(String knownName) {
		this.knownName = knownName;
	}

	public Map<ClassEntry, InnerClassSolver> getInnerClasses() {
		return this.innerClasses;
	}

	public String resolve() {
		var name = this.knownName == null ? this.node.getName() : this.knownName;

		return this.parent == null ? name : this.parent.resolve() + "$" + name;
	}

	public boolean shouldResolve() {
		return this.knownName != null || this.innerClasses.values().stream().anyMatch(InnerClassSolver::shouldResolve);
	}

	public void apply(EntryTree<EntryMapping> target) {
		if (target.get(this.node) == null) {
			target.insert(this.node, new EntryMapping(null));
		}

		this.innerClasses.values().forEach(innerClass -> innerClass.apply(target));
	}
}

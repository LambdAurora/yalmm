package yalmm.util;

import org.jetbrains.annotations.Nullable;
import yalmm.model.MappingClass;
import yalmm.model.MappingEntry;
import yalmm.model.MappingField;
import yalmm.model.MappingMethod;

public class TinyWriter {
	private final StringBuilder builder;
	private final int level;

	public TinyWriter(StringBuilder builder, int level) {
		this.builder = builder;
		this.level = level;
	}

	private void appendIndentation() {
		if (this.level > 0) {
			this.builder.append("\t".repeat(this.level));
		}
	}

	private void endLine() {
		this.builder.append("\n");
	}

	private void appendTarget(String source, @Nullable String entry) {
		if (entry != null) {
			this.builder.append("\t").append(entry);
		} else {
			this.builder.append("\t").append(source);
		}
	}

	private void appendTarget(MappingEntry entry) {
		this.appendTarget(entry.getSource(), entry.getTarget());
	}

	public TinyWriter appendClass(MappingClass clazz) {
		this.appendClass(clazz.getSource(), clazz.getTarget(), clazz);

		for (var subClass : clazz.getSubClasses()) {
			this.appendSubClass(clazz.getSource(), clazz.getTarget(), subClass);
		}

		return this;
	}

	private void appendSubClass(String parentSource, String parentTarget, MappingClass clazz) {
		String source = parentSource + "$" + clazz.getSource();
		String target = clazz.getTarget() != null ? (parentTarget + "$" + clazz.getTarget()) : null;

		this.appendClass(source, target, clazz);

		for (var subClass : clazz.getSubClasses()) {
			this.appendSubClass(source, target, subClass);
		}
	}

	private void appendClass(String source, @Nullable String target, MappingClass clazz) {
		this.appendIndentation();
		this.builder.append("c\t").append(source);

		this.appendTarget(source, target);
		this.endLine();

		if (clazz.hasComments()) {
			this.appendIndentation();
			this.builder.append("\tc\t").append(String.join("\\n", clazz.getComment()));
			this.endLine();
		}

		var subWriter = new TinyWriter(this.builder, this.level + 1);

		for (var field : clazz.getFields()) {
			subWriter.appendField(field);
		}

		for (var method : clazz.getMethods()) {
			subWriter.appendMethod(method);
		}
	}

	public void appendField(MappingField entry) {
		this.appendIndentation();
		this.builder.append("f\t").append(entry.getDescriptor()).append("\t").append(entry.getSource());

		this.appendTarget(entry);
		this.endLine();

		if (entry.hasComments()) {
			this.appendIndentation();
			this.builder.append("\tc\t").append(String.join("\\n", entry.getComment()));
			this.endLine();
		}
	}

	public void appendMethod(MappingMethod entry) {
		this.appendIndentation();
		this.builder.append("m\t").append(entry.getDescriptor()).append("\t").append(entry.getSource());

		this.appendTarget(entry);
		this.endLine();

		if (entry.hasComments()) {
			this.appendIndentation();
			this.builder.append("\tc\t").append(String.join("\\n", entry.getComment()));
			this.endLine();
		}

		// Parameters
		for (var param : entry.getParams()) {
			this.appendIndentation();
			this.builder.append("\tp\t").append(param.index());

			if (param.name() != null) {
				this.builder.append("\t\t").append(param.name());
			}

			this.endLine();

			if (param.hasComments()) {
				this.appendIndentation();
				this.builder.append("\t\tc\t").append(String.join("\\n", param.comments()));
				this.endLine();
			}
		}
	}
}

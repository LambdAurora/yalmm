package yalmm.model;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MappingEntry {
	private final String source;
	private String target;
	private List<String> comment;

	public MappingEntry(String source) {
		this.source = source;
	}

	public String getSource() {
		return this.source;
	}

	public @Nullable String getTarget() {
		return this.target;
	}

	public void setTarget(@Nullable String target) {
		this.target = target;
	}

	public boolean hasComments() {
		return this.comment != null && !this.comment.isEmpty();
	}

	public List<String> getComment() {
		return this.comment;
	}

	public void setComment(List<String> comment) {
		this.comment = comment;
	}
}

package yalmm.model;

public abstract class MappingDescribableEntry extends MappingEntry {
	private final String descriptor;

	public MappingDescribableEntry(String source, String descriptor) {
		super(source);
		this.descriptor = descriptor;
	}

	public String getDescriptor() {
		return this.descriptor;
	}
}

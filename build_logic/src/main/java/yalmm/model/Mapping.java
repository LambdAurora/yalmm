package yalmm.model;

import yalmm.util.TinyWriter;

import java.util.ArrayList;
import java.util.List;

public class Mapping {
	private final List<MappingClass> classes = new ArrayList<>();

	public List<MappingClass> getClasses() {
		return this.classes;
	}

	public String toTinyV2() {
		var builder = new StringBuilder();

		builder.append("tiny\t2\t0\tintermediary\tnamed\n");

		var writer = new TinyWriter(builder, 0);

		for (var clazz : this.classes) {
			writer.appendClass(clazz);
		}

		return builder.toString();
	}
}

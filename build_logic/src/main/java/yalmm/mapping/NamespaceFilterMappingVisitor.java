package yalmm.mapping;

import net.fabricmc.mappingio.MappedElementKind;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.ForwardingMappingVisitor;

import java.io.IOException;
import java.util.List;

public class NamespaceFilterMappingVisitor extends ForwardingMappingVisitor {
	private int namespaceToKeep = -1;

	public NamespaceFilterMappingVisitor(MappingVisitor next) {
		super(next);
	}

	@Override
	public void visitNamespaces(String srcNamespace, List<String> dstNamespaces) throws IOException {
		this.namespaceToKeep = dstNamespaces.indexOf("named");
		this.next.visitNamespaces(srcNamespace, List.of("named"));
	}

	@Override
	public void visitDstName(MappedElementKind targetKind, int namespace, String name) throws IOException {
		if (this.namespaceToKeep == namespace) {
			super.visitDstName(targetKind, 0, name);
		}
	}

	@Override
	public void visitDstDesc(MappedElementKind targetKind, int namespace, String desc) throws IOException {
		if (this.namespaceToKeep == namespace) {
			super.visitDstDesc(targetKind, 0, desc);
		}
	}
}

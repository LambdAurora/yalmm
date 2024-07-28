package yalmm.mapping;

import net.fabricmc.mappingio.MappedElementKind;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.ForwardingMappingVisitor;
import net.fabricmc.mappingio.tree.MappingTreeView;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

public class YalmmMappingVisitor extends ForwardingMappingVisitor {
	private final MappingTreeView mojangToIntermediary;
	private final int intermediaryNamespace;

	private MappingTreeView.ClassMappingView currentClass;
	private MappingTreeView.MethodMappingView currentMethod;
	private boolean allowed = true;

	public YalmmMappingVisitor(MappingVisitor next, MappingTreeView mojangToIntermediary) {
		super(next);
		this.mojangToIntermediary = mojangToIntermediary;
		this.intermediaryNamespace = this.mojangToIntermediary.getNamespaceId("intermediary");
	}

	@Override
	public void visitNamespaces(String srcNamespace, List<String> dstNamespaces) throws IOException {
		this.next.visitNamespaces("intermediary", dstNamespaces);
	}

	@Override
	public boolean visitClass(String srcName) throws IOException {
		this.currentClass = this.mojangToIntermediary.getClass(srcName);
		this.allowed = true;

		String actualSrcName = this.currentClass.getDstName(intermediaryNamespace);
		if (actualSrcName == null) actualSrcName = srcName;

		return this.next.visitClass(actualSrcName);
	}

	@Override
	public boolean visitField(String srcName, @Nullable String srcDesc) throws IOException {
		var field = this.currentClass.getField(srcName, srcDesc);
		this.allowed = true;
		return this.next.visitField(
				field.getDstName(intermediaryNamespace),
				field.getDstDesc(intermediaryNamespace)
		);
	}

	@Override
	public boolean visitMethod(String srcName, @Nullable String srcDesc) throws IOException {
		String actualSrcName = srcName;

		this.currentMethod = this.currentClass.getMethod(srcName, srcDesc);

		if (this.currentMethod != null) {
			String newSrcName = this.currentMethod.getDstName(this.intermediaryNamespace);

			if (newSrcName != null) {
				actualSrcName = newSrcName;
			}
		}

		return this.allowed && this.next.visitMethod(
				actualSrcName,
				this.currentMethod != null ? this.currentMethod.getDstDesc(this.intermediaryNamespace) : null
		);
	}

	@Override
	public boolean visitMethodArg(int argPosition, int lvIndex, @Nullable String srcName) throws IOException {
		String actualSrcName = srcName;

		if (this.currentMethod != null) {
			var arg = this.currentMethod.getArg(argPosition, lvIndex, srcName);
			if (arg != null) {
				actualSrcName = arg.getDstName(this.intermediaryNamespace);
			}
		}

		return this.allowed && this.next.visitMethodArg(argPosition, lvIndex, actualSrcName);
	}

	@Override
	public boolean visitMethodVar(int lvtRowIndex, int lvIndex, int startOpIdx, int endOpIdx, @Nullable String srcName) throws IOException {
		String actualSrcName = srcName;

		if (this.currentMethod != null) {
			var arg = this.currentMethod.getVar(lvtRowIndex, lvIndex, startOpIdx, endOpIdx, srcName);
			if (arg != null) {
				actualSrcName = arg.getDstName(this.intermediaryNamespace);
			}
		}

		return this.allowed && this.next.visitMethodVar(lvtRowIndex, lvIndex, startOpIdx, endOpIdx, actualSrcName);
	}

	@Override
	public void visitDstName(MappedElementKind targetKind, int namespace, String name) throws IOException {
		if (this.allowed) {
			super.visitDstName(targetKind, namespace, name);
		}
	}

	@Override
	public void visitDstDesc(MappedElementKind targetKind, int namespace, String desc) throws IOException {
		if (this.allowed) {
			super.visitDstDesc(targetKind, namespace, desc);
		}
	}

	@Override
	public boolean visitElementContent(MappedElementKind targetKind) throws IOException {
		return this.allowed && super.visitElementContent(targetKind);
	}

	@Override
	public void visitComment(MappedElementKind targetKind, String comment) throws IOException {
		if (this.allowed) {
			super.visitComment(targetKind, comment);
		}
	}
}

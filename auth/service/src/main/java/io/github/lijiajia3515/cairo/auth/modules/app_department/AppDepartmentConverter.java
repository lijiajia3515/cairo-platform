package io.github.lijiajia3515.cairo.auth.modules.app_department;

import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.AppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.AppDepartmentExtension;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.AppDepartmentField;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.MetadataAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.TreeNodeAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppDepartmentMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserConverter;

import java.util.Map;

public class AppDepartmentConverter {

	public static AppDepartment convert(AppDepartmentMongodb m, AppDepartmentExtension extension) {
		final AppDepartment.AppDepartmentBuilder<?, ?> builder = AppDepartment.builder()
			.departmentId(m.getDepartmentId());

		if (extension.fields().contains(AppDepartmentField.NAME)) {
			builder.departmentName(m.getDepartmentName());
		}

		if (extension.fields().contains(AppDepartmentField.PARENT_ID)) {
			builder.parentId(m.getParentId())
				.root(m.isRoot());
		}

		if (extension.fields().contains(AppDepartmentField.REMARK)) {
			builder.remark(m.getRemark());
		}

		builder.remark(m.getRemark());
		return builder.build();
	}

	public static MetadataAppDepartment convert(AppDepartmentMongodb m, Map<String, AppUser> metadataUserMap, AppDepartmentExtension extension) {
		final MetadataAppDepartment.MetadataAppDepartmentBuilder<?, ?> builder = MetadataAppDepartment.builder()
			.departmentId(m.getDepartmentId());

		if (extension.fields().contains(AppDepartmentField.NAME)) {
			builder.departmentName(m.getDepartmentName());
		}

		if (extension.fields().contains(AppDepartmentField.PARENT_ID)) {
			builder.parentId(m.getParentId())
				.root(m.isRoot());
		}

		if (extension.fields().contains(AppDepartmentField.REMARK)) {
			builder.remark(m.getRemark());
		}

		if (extension.fields().contains(AppDepartmentField.METADATA)) {
			builder.metadata(CairoAppUserConverter.convertAppUser(m.getMetadata(), metadataUserMap));
		}

		builder.remark(m.getRemark());
		return builder.build();
	}


	public static TreeNodeAppDepartment treeConvert(AppDepartmentMongodb m) {
		return TreeNodeAppDepartment.builder()
			.parentId(m.getParentId())
			.root(m.isRoot())
			.departmentId(m.getDepartmentId())
			.departmentName(m.getDepartmentName())
			.depth(m.getDepth())
			.leftNo(m.getLeftNo())
			.rightNo(m.getRightNo())
			.build();
	}

}

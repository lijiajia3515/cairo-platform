package io.github.lijiajia3515.cairo.auth.modules.tenant_app_department;

import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.MetadataTenantAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.TenantAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.TenantAppDepartmentExtension;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.TenantAppDepartmentField;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department.TreeNodeTenantAppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppDepartmentMongodb;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.CairoTenantAppUserConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUser;

import java.util.Map;

public class TenantAppDepartmentConverter {

	public static TenantAppDepartment convert(TenantAppDepartmentMongodb m, TenantAppDepartmentExtension extension) {
		final TenantAppDepartment.TenantAppDepartmentBuilder<?, ?> builder = TenantAppDepartment.builder()
			.tenantId(m.getTenantId())
			.appId(m.getAppId())
			.departmentId(m.getDepartmentId());

		if (extension.fields().contains(TenantAppDepartmentField.NAME)) {
			builder.departmentName(m.getDepartmentName());
		}

		if (extension.fields().contains(TenantAppDepartmentField.PARENT_ID)) {
			builder.parentId(m.getParentId());
		}

		if (extension.fields().contains(TenantAppDepartmentField.REMARK)) {
			builder.remark(m.getRemark());
		}

		builder.remark(m.getRemark());
		return builder.build();
	}

	public static MetadataTenantAppDepartment convert(TenantAppDepartmentMongodb m, Map<String, TenantAppUser> metadataUserMap, TenantAppDepartmentExtension extension) {
		final MetadataTenantAppDepartment.MetadataTenantAppDepartmentBuilder<?, ?> builder = MetadataTenantAppDepartment.builder()
			.tenantId(m.getTenantId())
			.appId(m.getAppId())
			.departmentId(m.getDepartmentId());

		if (extension.fields().contains(TenantAppDepartmentField.NAME)) {
			builder.departmentName(m.getDepartmentName());
		}

		if (extension.fields().contains(TenantAppDepartmentField.PARENT_ID)) {
			builder.parentId(m.getParentId());
		}

		if (extension.fields().contains(TenantAppDepartmentField.REMARK)) {
			builder.remark(m.getRemark());
		}

		if (extension.fields().contains(TenantAppDepartmentField.METADATA)) {
			builder.metadata(CairoTenantAppUserConverter.convertTenantAppUser(m.getMetadata(), metadataUserMap));
		}

		builder.remark(m.getRemark());
		return builder.build();
	}


	public static TreeNodeTenantAppDepartment treeConvert(TenantAppDepartmentMongodb m) {
		return TreeNodeTenantAppDepartment.builder()
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

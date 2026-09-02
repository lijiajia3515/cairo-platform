package io.github.lijiajia3515.cairo.auth.modules.tenant_app_department_template;


import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department_template.MetadataTenantAppDepartmentTemplate;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department_template.TenantAppDepartmentTemplate;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department_template.TenantAppDepartmentTemplateExtension;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department_template.TenantAppDepartmentTemplateField;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_department_template.TreeNodeTenantAppDepartmentTemplate;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppDepartmentTemplateMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserConverter;

import java.util.Map;

public class TenantAppDepartmentTemplateConverter {

	public static TenantAppDepartmentTemplate convert(TenantAppDepartmentTemplateMongodb m, TenantAppDepartmentTemplateExtension extension) {
		final TenantAppDepartmentTemplate.TenantAppDepartmentTemplateBuilder<?, ?> builder = TenantAppDepartmentTemplate.builder()
			.tenantAppDepartmentTemplateId(m.getTenantAppDepartmentTemplateId());

		if (extension.fields().contains(TenantAppDepartmentTemplateField.NAME)) {
			builder.tenantAppDepartmentTemplateName(m.getTenantAppDepartmentTemplateName());
		}

		if (extension.fields().contains(TenantAppDepartmentTemplateField.PARENT_ID)) {
			builder.parentId(m.getParentId())
				.root(m.isRoot());
		}

		if (extension.fields().contains(TenantAppDepartmentTemplateField.REMARK)) {
			builder.remark(m.getRemark());
		}

		builder.remark(m.getRemark());
		return builder.build();
	}

	public static MetadataTenantAppDepartmentTemplate convert(TenantAppDepartmentTemplateMongodb m, Map<String, AppUser> metadataUserMap, TenantAppDepartmentTemplateExtension extension) {
		final MetadataTenantAppDepartmentTemplate.MetadataTenantAppDepartmentTemplateBuilder<?, ?> builder = MetadataTenantAppDepartmentTemplate.builder()
			.tenantAppDepartmentTemplateId(m.getTenantAppDepartmentTemplateId());

		if (extension.fields().contains(TenantAppDepartmentTemplateField.NAME)) {
			builder.tenantAppDepartmentTemplateName(m.getTenantAppDepartmentTemplateName());
		}

		if (extension.fields().contains(TenantAppDepartmentTemplateField.PARENT_ID)) {
			builder.parentId(m.getParentId())
				.root(m.isRoot());
		}

		if (extension.fields().contains(TenantAppDepartmentTemplateField.REMARK)) {
			builder.remark(m.getRemark());
		}

		if (extension.fields().contains(TenantAppDepartmentTemplateField.METADATA)) {
			builder.metadata(CairoAppUserConverter.convertAppUser(m.getMetadata(), metadataUserMap));
		}

		builder.remark(m.getRemark());
		return builder.build();
	}


	public static TreeNodeTenantAppDepartmentTemplate treeConvert(TenantAppDepartmentTemplateMongodb m) {
		return TreeNodeTenantAppDepartmentTemplate.builder()
			.parentId(m.getParentId())
			.root(m.isRoot())
			.tenantAppDepartmentTemplateId(m.getTenantAppDepartmentTemplateId())
			.tenantAppDepartmentTemplateName(m.getTenantAppDepartmentTemplateName())
			.depth(m.getDepth())
			.leftNo(m.getLeftNo())
			.rightNo(m.getRightNo())
			.build();
	}

}

package io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_doc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetOnlineTenantAppDocArgs implements Serializable {

	/**
	 * 租户id
	 */
	private String tenantId;

	/**
	 * 当前用户id
	 */
	private String userId;

	/**
	 * 文件地址
	 */
	private String filepath;


}

package io.github.lijiajia3515.cairo.auth.domain.api.endpoint.app_user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 管理员修改应用用户信息
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyAppUserInfoArgs implements Serializable {

	/**
	 * 用户id
	 */
	@NotNull
	private String userId;

	/**
	 * 昵称
	 */
	private String nickname;

	/**
	 * 联系方式
	 */
	private String phoneNumber;

	/**
	 * 职位
	 */
	private String position;

	/**
	 * 主部门id
	 */
	private String mainDepartmentId;



	/**
	 * 用户标签
	 */
	private List<String> tagIds;

	/**
	 * 应用角色
	 */
	private List<String> roleIds;

	/**
	 * 应用部门
	 */
	private List<String> departmentIds;

}

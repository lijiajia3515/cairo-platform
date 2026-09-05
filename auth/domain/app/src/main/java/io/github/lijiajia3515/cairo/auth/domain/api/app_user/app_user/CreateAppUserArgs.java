package io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 管理员创建应用级用户参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CreateAppUserArgs implements Serializable {

	/**
	 * 账号id
	 */
	@NotNull
	private String accountId;

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
	 * 角色
	 */
	private List<String> roleIds;

	/**
	 * 部门
	 */
	private List<String> departmentIds;

	/**
	 * 标签
	 */
	private List<String> tagIds;


}

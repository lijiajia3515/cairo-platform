package io.github.lijiajia3515.cairo.auth.domain.api.app_user.app_user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 创建账号和应用级用户参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CreateAccountAndAppUserArgs implements Serializable {

	/**
	 * 手机号
	 */
	private String phoneNumber;

	/**
	 * 用户名
	 */
	private String username;

	/**
	 * 邮箱
	 */
	@Email
	private String email;

	/**
	 * 头像
	 */
	private String avatarUrl;

	/**
	 * 密码
	 */
	// @Size(min = 6, max = 40)
	private String password;

	/**
	 * 昵称
	 */
	@NotNull
	private String nickname;

	/**
	 * 角色id
	 */
	private List<String> roleIds;

	/**
	 * 部门集合
	 */
	private List<String> departmentIds;

	/**
	 * 职位
	 */
	private String position;

	/**
	 * 主部门id
	 */
	private String mainDepartmentId;

	/**
	 * 标签
	 */
	private List<String> tagIds;
}

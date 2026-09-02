package io.github.lijiajia3515.cairo.auth.domain.dto.app_user;

import io.github.lijiajia3515.cairo.auth.domain.dto.app_department.AppDepartment;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_role.MetadataAppRole;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user_tag.AppUserTag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;


/**
 * 应用用户
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AppUser implements Serializable {
	/**
	 * Id
	 */
	private String userId;


	/**
	 * 昵称
	 */
	private String nickname;


	/**
	 * 手机号
	 */
	private String phoneNumber;

	/**
	 * 是否应用管理员
	 */
	private Boolean appAdmin;

	/**
	 * 角色
	 */
	private List<MetadataAppRole> roles;

	/**
	 * 部门
	 */
	private List<AppDepartment> departments;

	/**
	 * 主部门
	 */
	private String mainDepartmentId;

	/**
	 * 职位
	 */
	private String position;

	/**
	 * 标签
	 */
	private List<AppUserTag> tags;

	/**
	 * 启用状态
	 */
	private Boolean enabled;

	/**
	 * 加入时间
	 */
	private LocalDateTime joinTime;

	/**
	 * 登录时间
	 */
	private LocalDateTime loginTime;

	/**
	 * 注销状态
	 */
	private String logoffStatus;

	/**
	 * 注销时间
	 */
	private LocalDateTime logoffPendingTime;

	/**
	 * 注销成功时间
	 */
	private LocalDateTime logoffSuccessTime;

	/**
	 * accountId
	 */
	private String accountId;

	/**
	 * accountNickname
	 */
	private String accountNickname;

	/**
	 * 头像
	 */
	private String accountAvatarUrl;

	/**
	 * 账号登录手机号
	 */
	private String accountPhoneNumber;

	/**
	 * 用户名
	 */
	private String accountUsername;

	/**
	 * 邮箱
	 */
	private String accountEmail;
}

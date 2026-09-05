package io.github.lijiajia3515.cairo.auth.domain.mongodb;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractAppUserMetadataMongodbField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
	 * 企业应用级用户
	 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantAppUserMongodb implements Serializable {
	/**
	 * 标识
	 */
	@MongoId
	private ObjectId _id;

	/**
	 * 企业ID
	 * 所属企业的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String tenantId;

	/**
	 * 应用ID
	 * 所属应用的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String appId;

	/**
	 * 用户ID
	 * 所属用户的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String userId;

	/**
	 * 昵称
	 */
	@Field(write = Field.Write.ALWAYS)
	private String nickname;

	/**
	 * 手机号
	 * 手机号
	 */
	@Field(write = Field.Write.ALWAYS)
	private String phoneNumber;

	/**
	 * 是否管理员
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean admin;

	/**
	 * 角色标识
	 * 关联角色的唯一标识数组
	 */
	@Field(write = Field.Write.ALWAYS)
	private List<String> roleIds;

	/**
	 * 职位
	 */
	@Field(write = Field.Write.ALWAYS)
	private String position;

	/**
	 * 主部门标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String mainDepartmentId;

	/**
	 * 部门标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private List<String> departmentIds;

	/**
	 * 标签标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private List<String> tagIds;

	/**
	 * 启用状态
	 * 启用为 true，禁用为 false
	 */
	@Field(write = Field.Write.ALWAYS)
	private Boolean enabled;


	/**
	 * 加入时间
	 */
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime joinTime;

	/**
	 * 登录时间
	 * 最后登录时间
	 */
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime loginTime;

	/**
	 * 注销状态
	 */
	@Field(write = Field.Write.ALWAYS)
	private String logoffStatus;

	/**
	 * 注销时间
	 * 注销等待开始时间
	 */
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime logoffPendingTime;

	/**
	 * 注销成功时间
	 * 注销完成时间
	 */
	@Field(write = Field.Write.ALWAYS)
	private LocalDateTime logoffSuccessTime;

	/**
	 * 账号ID
	 * 所属账号的唯一标识
	 */
	@Field(write = Field.Write.ALWAYS)
	private String accountId;

	/**
	 * 元信息
	 * 元信息，包含创建与更新的用户及时间
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private TenantAppUserMetadataMongodb metadata = new TenantAppUserMetadataMongodb();

	public static final MongodbField FIELD = new MongodbField();

	public static class MongodbField extends AbstractAppUserMetadataMongodbField {
		public final String TENANT_ID = field("tenantId");

		public final String APP_ID = field("appId");

		public final String USER_ID = field("userId");

		public final String NICKNAME = field("nickname");

		public final String PHONE_NUMBER = field("phoneNumber");

		public final String POSITION = field("position");

		public final String ROLE_IDS = field("roleIds");
		public final String DEPARTMENT_IDS = field("departmentIds");

		public final String MAIN_DEPARTMENT_ID = field("mainDepartmentId");
		public final String TAG_IDS = field("tagIds");

		public final String ADMIN = field("admin");

		public final String ENABLED = field("enabled");

		public final String JOIN_TIME = field("joinTime");

		public final String LOGIN_TIME = field("loginTime");

		public final String LOGOFF_STATUS = field("logoffStatus");

		public final String LOGOFF_PENDING_TIME = field("logoffPendingTime");

		public final String LOGOFF_SUCCESS_TIME = field("logoffSuccessTime");
		public final String TRANSFER_ACCOUNT_TIME = field("transferAccountTime");

		public final String ACCOUNT_ID = field("accountId");


	}
}

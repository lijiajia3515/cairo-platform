package io.github.lijiajia3515.cairo.auth.domain.mongodb;

import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractAppUserMetadataMongodbField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.io.Serializable;
import java.util.List;


/**
	 * 企业应用用户组
	 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TenantAppUserGroupMongodb implements Serializable {

	/**
	 * 标识
	 */
	@MongoId
	private ObjectId _id;

	/**
	 * 租户ID
	 * 所属租户的唯一标识
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
     * 分组ID
     */
	@Field(write = Field.Write.ALWAYS)
    private String groupId;

    /**
     * 名称
     */
	@Field(write = Field.Write.ALWAYS)
    private String groupName;


    /**
     * 继承角色ID集合
     * 关联角色的唯一标识数组
     */
	@Field(write = Field.Write.ALWAYS)
    private List<String> roleIds;

	/**
	 * 备注
	 * 备注信息
	 */
	@Field(write = Field.Write.ALWAYS)
	private String remark;

	/**
	 * 元信息
	 * 元信息，包含创建与更新的用户及时间
	 */
	@Builder.Default
	@Field(write = Field.Write.ALWAYS)
	private AppUserMetadataMongodb metadata = new AppUserMetadataMongodb();

    public static final MongodbField FIELD = new MongodbField();

    public static class MongodbField extends AbstractAppUserMetadataMongodbField {
        private MongodbField() {

        }

		public final String TENANT_ID = field("tenantId");

		public final String APP_ID = field("appId");

        public final String GROUP_ID = field("groupId");
        public final String GROUP_NAME = field("groupName");
        public final String ROLE_IDS = field("roleIds");
        public final String REMARK = field("remark");
    }
}

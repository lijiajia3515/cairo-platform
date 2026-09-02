package io.github.lijiajia3515.cairo.auth.domain.dto.permission;

import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.CairoAppUserMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 功能权限
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AppUserMetadataPermission implements Serializable {
    /**
     * 上级id
     */
    private String menuId;

    /**
     * 菜单图标
     */
    private String menuIcon;

    /**
     * 菜单名称列表
     */
    private List<String> menuNames;

    /**
     * 功能权限ID
     */
    private String permissionId;


    /**
     * 功能权限名称
     */
    private String permissionName;

    /**
     * icon
     */
    private String icon;

    /**
     * 接口所需要的权限标识
     */
    private Set<String> authorities;

	/**
	 * 类型
	 */
	private String type;

    /**
     * 是否默认拥有
     */
    private Boolean defaultPermission;

    /**
     * 是否隐藏
     */
    private Boolean hiddenPermission;

    /**
     * 排序值
     */
    private Long sort;

    /**
     * metadata
     */
    private CairoAppUserMetadata metadata;
}

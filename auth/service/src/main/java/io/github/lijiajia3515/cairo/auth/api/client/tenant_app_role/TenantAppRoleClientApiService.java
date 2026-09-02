package io.github.lijiajia3515.cairo.auth.api.client.tenant_app_role;

import io.github.lijiajia3515.cairo.auth.CairoAuthExtensionConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppRoleMongodb;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role.MetadataTenantAppRole;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role.TenantAppRole;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_role.TenantAppRoleConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_role.TenantAppRoleExtension;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app_role.GetTenantAppRoleArgs;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.CairoTenantAppUserTool;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app_user.TenantAppUser;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user.TenantAppUserCommonService;
import io.github.lijiajia3515.cairo.core.page.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;

import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * [client/api] tenant role service
 */
@Slf4j
@Validated
@Component
public class TenantAppRoleClientApiService {
    private final MongoTemplate mongoTemplate;
    private final MongoTemplate readMongoTemplate;
    private final TenantAppUserCommonService tenantAppUserCommonService;

    public TenantAppRoleClientApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
										 @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
										 TenantAppUserCommonService tenantAppUserCommonService) {
        this.mongoTemplate = mongoTemplate;
        this.readMongoTemplate = readMongoTemplate;
        this.tenantAppUserCommonService = tenantAppUserCommonService;
    }

    /**
     * find
     *
     * @param appId appId
     * @param args  args
     * @return role page
     */
    @NewSpan
    @BizLog(
            bizId = "role:get_role_list",
            scope = "read",
            params = {
                    @BizLog.Param(key = "appId", value = "#appId"),
                    @BizLog.Param(key = "args", value = "#args"),
            }
    )
    public List<MetadataTenantAppRole> getTenantAppRoleList(@Valid @NotNull String appId, @Validated GetTenantAppRoleArgs args) {
        Criteria criteria = buildCriteria(appId, args);
        Query query = Query.query(criteria);
        query.with(Sort.by(Sort.Order.desc(TenantAppRoleMongodb.FIELD.METADATA.UPDATE_TIME)));

        final List<TenantAppRoleMongodb> rms = readMongoTemplate.find(query, TenantAppRoleMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE);
        return getTenantAppRoleList(args.getTenantId(), appId, rms, args.getExtension());
    }


    /**
     * find page
     *
     * @param appId 应用id
     * @param args  args
     * @return role page
     */
    @NewSpan
    @BizLog(
            bizId = "role:get_role_page_list",
            scope = "read",
            params = {
                    @BizLog.Param(key = "appId", value = "#appId"),
                    @BizLog.Param(key = "args", value = "#args"),
            }
    )
    public Page<MetadataTenantAppRole> getTenantAppRolePageList(@Valid @NotNull String appId, @Validated GetTenantAppRoleArgs args) {
        Criteria criteria = buildCriteria(appId, args);
        Query query = Query.query(criteria);

        long total = readMongoTemplate.count(query, TenantAppRoleMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE);

        query.with(args.pageable());
        List<TenantAppRoleMongodb> rms = readMongoTemplate.find(query, TenantAppRoleMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE);

        final List<MetadataTenantAppRole> rs = getTenantAppRoleList(args.getTenantId(), appId, rms, args.getExtension());

        return new Page<>(args, rs, total);
    }

    @NewSpan
    private List<MetadataTenantAppRole> getTenantAppRoleList(String tenantId, String appId, List<TenantAppRoleMongodb> ms, Map<String, String> extensionMap) {
        TenantAppRoleExtension extension = Optional.ofNullable(extensionMap.get(CairoAuthExtensionConstants.ROLE)).map(TenantAppRoleExtension::valueOf).orElse(TenantAppRoleExtension.ALL);

        Set<String> metadataUserIds = CairoTenantAppUserTool.getTenantAppUserMetadataUserIds(ms.stream().map(TenantAppRoleMongodb::getMetadata).collect(Collectors.toList()));
        Map<String, TenantAppUser> metadataUserMap = Optional.of(metadataUserIds)
                .filter(userIds -> !userIds.isEmpty())
                .map(userIds -> tenantAppUserCommonService.getUserMapByUserIds(tenantId, appId, userIds))
                .orElse(Collections.emptyMap());

        return ms.stream()
                .map(x -> TenantAppRoleConverter.convert(x, Collections.emptyMap(), metadataUserMap, extension))
                .collect(Collectors.toList());
    }

    /**
     * 根据角色id获取角色id
     *
     * @param tenantId tenantId
     * @param appId    appId
     * @param roleId   roleId
     * @return 角色信息
     */
    @NewSpan
    @BizLog(
            bizId = "role:get_role_by_id",
            scope = "read",
            params = {
                    @BizLog.Param(key = "tenantId", value = "#tenantId"),
                    @BizLog.Param(key = "appId", value = "#appId"),
                    @BizLog.Param(key = "roleId", value = "#roleId"),
            }
    )
    Optional<TenantAppRole> getRoleById(@Valid @NotNull String tenantId, @Valid @NotNull String appId, @Valid @NotNull String roleId) {
        Query query = Query.query(Criteria
                .where(TenantAppRoleMongodb.FIELD.TENANT_ID).is(tenantId)
                .and(TenantAppRoleMongodb.FIELD.APP_ID).is(appId)
                .and(TenantAppRoleMongodb.FIELD.ROLE_ID).is(roleId)
        );
        TenantAppRoleMongodb role = readMongoTemplate.findOne(query, TenantAppRoleMongodb.class, MongodbConstants.Collection.TENANT_APP_ROLE);
        return Optional.ofNullable(role).map(TenantAppRoleConverter::convert);
    }

    private Criteria buildCriteria(String appId, GetTenantAppRoleArgs param) {
        Criteria criteria = Criteria
                .where(TenantAppRoleMongodb.FIELD.TENANT_ID).is(param.getTenantId())
                .and(TenantAppRoleMongodb.FIELD.APP_ID).is(appId);
        Optional.ofNullable(param.getKeyword()).filter(kw -> !kw.isEmpty()).ifPresent(kw -> criteria.and(TenantAppRoleMongodb.FIELD.ROLE_NAME).regex(kw));
        return criteria;
    }


}

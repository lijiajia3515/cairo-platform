package io.github.lijiajia3515.cairo.auth.api.client.app_role;

import io.github.lijiajia3515.cairo.auth.CairoAuthExtensionConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppRoleMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_role.AppRole;
import io.github.lijiajia3515.cairo.auth.modules.app_role.AppRoleConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_role.AppRoleExtension;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_role.MetadataAppRole;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_role.GetAppRoleArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserTool;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
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
 * [client/api] app_app_role service
 */
@Slf4j
@Validated
@Component
public class AppRoleClientApiService {
    private final MongoTemplate readMongoTemplate;
    private final AppUserCommonService userCommonService;

    public AppRoleClientApiService(@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
								   AppUserCommonService userCommonService) {
        this.readMongoTemplate = readMongoTemplate;
        this.userCommonService = userCommonService;
    }

    /**
     * find
     *
     * @param appId appId
     * @param args  args
     * @return app_role page
     */
    @NewSpan
    @BizLog(
            bizId = "app_role:get_app_role_list",
            scope = "read",
            params = {
                    @BizLog.Param(key = "appId", value = "#appId"),
                    @BizLog.Param(key = "args", value = "#args"),
            }
    )
    public List<MetadataAppRole> getRoleList(@Valid @NotNull String appId, @Validated GetAppRoleArgs args) {
        Criteria criteria = buildCriteria(appId, args);
        Query query = Query.query(criteria);
        query.with(Sort.by(Sort.Order.desc(AppRoleMongodb.FIELD.METADATA.UPDATE_TIME)));

        final List<AppRoleMongodb> rms = readMongoTemplate.find(query, AppRoleMongodb.class, MongodbConstants.Collection.APP_ROLE);
        return getRoleList(appId, rms, args.getExtension());
    }


    /**
     * find page
     *
     * @param appId 应用id
     * @param args  args
     * @return app_role page
     */
    @NewSpan
    @BizLog(
            bizId = "app_role:get_app_role_page_list",
            scope = "read",
            params = {
                    @BizLog.Param(key = "appId", value = "#appId"),
                    @BizLog.Param(key = "args", value = "#args"),
            }
    )
    public Page<MetadataAppRole> getAppRolePageList(@Valid @NotNull String appId, @Validated GetAppRoleArgs args) {
        Criteria criteria = buildCriteria(appId, args);
        Query query = Query.query(criteria);

        long total = readMongoTemplate.count(query, AppRoleMongodb.class, MongodbConstants.Collection.APP_ROLE);

        query.with(args.pageable());
        List<AppRoleMongodb> rms = readMongoTemplate.find(query, AppRoleMongodb.class, MongodbConstants.Collection.APP_ROLE);

        final List<MetadataAppRole> rs = getRoleList(appId, rms, args.getExtension());

        return new Page<>(args, rs, total);
    }

    @NewSpan
    private List<MetadataAppRole> getRoleList(String appId, List<AppRoleMongodb> ms, Map<String, String> extensionMap) {
        AppRoleExtension extension = Optional.ofNullable(extensionMap.get(CairoAuthExtensionConstants.ROLE)).map(AppRoleExtension::valueOf).orElse(AppRoleExtension.ALL);

        Set<String> metadataUserIds = CairoAppUserTool.getAppUserMetadataUserIds(ms.stream().map(AppRoleMongodb::getMetadata).collect(Collectors.toList()));
        Map<String, AppUser> metadataUserMap = Optional.of(metadataUserIds)
                .filter(userIds -> !userIds.isEmpty())
                .map(userIds -> userCommonService.getAppUserMapByAppUserIds(appId, userIds))
                .orElse(Collections.emptyMap());

        return ms.stream()
                .map(x -> AppRoleConverter.convert(x, Collections.emptyMap(), metadataUserMap, extension))
                .collect(Collectors.toList());
    }

    /**
     * 根据角色id获取角色id
     *
     * @param appId    appId
     * @param appRoleId   appRoleId
     * @return 角色信息
     */
    @NewSpan
    @BizLog(
            bizId = "app_role:get_app_role_by_id",
            scope = "read",
            params = {
                    @BizLog.Param(key = "tenantId", value = "#tenantId"),
                    @BizLog.Param(key = "appId", value = "#appId"),
                    @BizLog.Param(key = "app_roleId", value = "#app_roleId"),
            }
    )
    Optional<AppRole> getRoleById(@Valid @NotNull String appId, @Valid @NotNull String appRoleId) {
        Query query = Query.query(Criteria
                .where(AppRoleMongodb.FIELD.APP_ID).is(appId)
                .and(AppRoleMongodb.FIELD.ROLE_ID).is(appRoleId)
        );
        AppRoleMongodb appRole = readMongoTemplate.findOne(query, AppRoleMongodb.class, MongodbConstants.Collection.APP_ROLE);
        return Optional.ofNullable(appRole).map(AppRoleConverter::convert);
    }

    private Criteria buildCriteria(String appId, GetAppRoleArgs param) {
        Criteria criteria = Criteria
                .where(AppRoleMongodb.FIELD.APP_ID).is(appId);
        Optional.ofNullable(param.getKeyword()).filter(kw -> !kw.isEmpty()).ifPresent(kw -> criteria.and(AppRoleMongodb.FIELD.ROLE_NAME).regex(kw));
        return criteria;
    }


}

package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.app_release;


import com.baomidou.lock.annotation.Lock4j;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app.GetAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.endpoint.GetEndpointByAppClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user.GetAppUserClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.domain.dto.endpoint.Endpoint;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.api.client.app.AppClientApiService;
import io.github.lijiajia3515.cairo.auth.api.client.endpoint.EndpointClientApiService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.exception.ParamsErrorBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.FileKeyPrefixConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app_release.CreateAppReleaseArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app_release.DeleteAppReleaseArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app_release.GetAppReleaseArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app_release.ModifyAppReleaseInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.app_release.SetAppReleaseLatestVersionArgs;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppReleaseMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.modules.app_release.AppReleaseConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_release.MetadataAppRelease;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_release.AppReleaseType;
import io.github.lijiajia3515.cairo.auth.modules.file.public_file.PublicFileCommonService;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;

@Slf4j
@Validated
@Component
public class AppReleaseCairoWebManageApiService {
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final MongoTemplate readMongoTemplate;
	private final AppUserCommonService appUserCommonService;
	private final CairoSecurityProperties cairoSecurityProperties;
	private final AppClientApiService appClientApiService;
	private final EndpointClientApiService endpointClientApiService;
	private final PublicFileCommonService publicFileCommonService;

	AppReleaseCairoWebManageApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
										   TransactionTemplate transactionTemplate,
										   @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
										   AppUserCommonService appUserCommonService,
										   CairoSecurityProperties cairoSecurityProperties,
										   AppClientApiService appClientApiService,
										   EndpointClientApiService endpointClientApiService,
										   PublicFileCommonService publicFileCommonService) {
		this.appUserCommonService = appUserCommonService;
		this.cairoSecurityProperties = cairoSecurityProperties;
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.appClientApiService = appClientApiService;
		this.endpointClientApiService = endpointClientApiService;
		this.publicFileCommonService = publicFileCommonService;
	}

	/**
	 * 创建应用发行
	 *
	 * @param args 参数
	 */
	@NewSpan
	@BizLog(
		bizId = "app_release:create_app_release",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void createAppRelease(@Validated CreateAppReleaseArgs args) {
		AppReleaseType type = AppReleaseType.typeValueOf(args.getType()).orElseThrow(() -> new ParamsErrorBusinessException(String.format("参数: 类型：%s 错误", args.getType())));
		AppReleaseMongodb appReleaseMongodb = AppReleaseMongodb.builder()
			.appId(args.getAppId())
			.endpointId(args.getEndpointId())
			.type(type.getTypeValue())
			.appVersion(args.getAppVersion())
			.releaseVersion(args.getReleaseVersion())
			.latestVersion(args.getLatestVersion())
			.remark(args.getRemark())
			.force(args.getForce())
			.webUrl(args.getWebUrl())
			.androidApkUrl(args.getAndroidApkUrl())
			.iosAppStoreUrl(args.getIosAppStoreUrl())
			.title(args.getTitle())
			.metadata(AppUserMetadataMongodb.builder()
				.createUserId(CairoSecurityContextHolder.getAppUserId())
				.updateUserId(CairoSecurityContextHolder.getAppUserId())
				.build())
			.build();

		AppReleaseMongodb mongodb = transactionTemplate.execute(status -> {
			try {
				if (args.getLatestVersion().equals(true)) {
					Query query = Query.query(Criteria
						.where(AppReleaseMongodb.FIELD.APP_ID).is(args.getAppId())
						.and(AppReleaseMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId())
						.and(AppReleaseMongodb.FIELD.TYPE).is(args.getType())
						.and(AppReleaseMongodb.FIELD.RELEASE_VERSION).is(args.getReleaseVersion())
					);
					Update update = Update.update(AppReleaseMongodb.FIELD.LATEST_VERSION, false);
					UpdateResult updateResult = mongoTemplate.updateMulti(query, update, AppReleaseMongodb.class, MongodbConstants.Collection.APP_RELEASE);
					log.info("appReleaseLatestVersion:{}", updateResult);
				}
				return mongoTemplate.insert(appReleaseMongodb, MongodbConstants.Collection.APP_RELEASE);
			} catch (Exception e) {
				log.debug("createAppRelease", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("创建应用发行失败");
			}
		});
	}

	/**
	 * 修改应用发行信息
	 * @param args 参数
	 */
	@NewSpan
	@Lock4j(name = "modify_app_release_info", keys = {"#args.appId","#args.endpointId","#args.type","#args.appVersion"})
	@BizLog(
		bizId = "app_release:modify_app_release_info",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifyAppReleaseInfo(ModifyAppReleaseInfoArgs args) {
		AppReleaseType type = AppReleaseType.typeValueOf(args.getType()).orElseThrow(() -> new ParamsErrorBusinessException(String.format("参数: 类型：%s 错误", args.getType())));
		AppReleaseMongodb mongodb = transactionTemplate.execute(status -> {
			try {
				Query query = Query.query(Criteria
					.where(AppReleaseMongodb.FIELD.APP_ID).is(args.getAppId())
					.and(AppReleaseMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId())
					.and(AppReleaseMongodb.FIELD.TYPE).is(type.getTypeValue())
					.and(AppReleaseMongodb.FIELD.APP_VERSION).is(args.getAppVersion())
				);

				Update update = new Update();
				Optional.ofNullable(args.getTitle()).filter(x -> !x.isBlank()).ifPresent(x -> update.set(AppReleaseMongodb.FIELD.TITLE, x));
				Optional.ofNullable(args.getRemark()).filter(x -> !x.isBlank()).ifPresent(x -> update.set(AppReleaseMongodb.FIELD.REMARK, x));
				Optional.ofNullable(args.getReleaseVersion()).ifPresent(x -> update.set(AppReleaseMongodb.FIELD.RELEASE_VERSION, x));
				Optional.ofNullable(args.getForce()).ifPresent(x -> update.set(AppReleaseMongodb.FIELD.FORCE, x));
				Optional.ofNullable(args.getWebUrl()).ifPresent(x -> update.set(AppReleaseMongodb.FIELD.WEB_URL, x));
				Optional.ofNullable(args.getAndroidApkUrl()).filter(x -> !x.isBlank()).ifPresent(x -> update.set(AppReleaseMongodb.FIELD.ANDROID_APK_URL, x));
				Optional.ofNullable(args.getIosAppStoreUrl()).filter(x -> !x.isBlank()).ifPresent(x -> update.set(AppReleaseMongodb.FIELD.IOS_APP_STORE_URL, x));

				update.set(AppReleaseMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
				update.currentDate(AppReleaseMongodb.FIELD.METADATA.UPDATE_TIME);

				final FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
				return mongoTemplate.findAndModify(query, update, options, AppReleaseMongodb.class, MongodbConstants.Collection.APP_RELEASE);
			} catch (Exception e) {
				log.debug("modifyAppReleaseInfo", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("更新应用发行信息失败");
			}
		});

		if (mongodb == null) {
			throw new ConflictBusinessException("更新应用发行信息失败");
		}
	}

	/**
	 * 设置为最新版本
	 *
	 * @param args 参数
	 */
	@NewSpan
	@Lock4j(name = "set_app_release_version_latest_version", keys = {"#args.appId","#args.endpointId","#args.type","#args.appVersion"})
	@BizLog(
		bizId = "app_release:set_app_release_version_latest_version",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void setAppReleaseVersionLatestVersion(SetAppReleaseLatestVersionArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Query query = Query.query(Criteria
					.where(AppReleaseMongodb.FIELD.APP_ID).is(args.getAppId())
					.and(AppReleaseMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId())
					.and(AppReleaseMongodb.FIELD.TYPE).is(args.getType())
					.and(AppReleaseMongodb.FIELD.APP_VERSION).is(args.getAppVersion()));

				AppReleaseMongodb versionMongodb = mongoTemplate.findOne(query, AppReleaseMongodb.class, MongodbConstants.Collection.APP_RELEASE);
				if (null == versionMongodb) {
					throw new ConflictBusinessException("应用发行不存在");
				}

				Query otherQuery = Query.query(Criteria
					.where(AppReleaseMongodb.FIELD.APP_ID).is(args.getAppId())
					.and(AppReleaseMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId())
					.and(AppReleaseMongodb.FIELD.TYPE).is(args.getType())
					.and(AppReleaseMongodb.FIELD.APP_VERSION).ne(args.getAppVersion())
					.and(AppReleaseMongodb.FIELD.RELEASE_VERSION).is(versionMongodb.getReleaseVersion()));
				Update otherUpdate = Update.update(AppReleaseMongodb.FIELD.LATEST_VERSION, false);
				UpdateResult otherUpdateResult = mongoTemplate.updateMulti(otherQuery, otherUpdate, AppReleaseMongodb.class, MongodbConstants.Collection.APP_RELEASE);
				log.info("setLatestVersion false :{}", otherUpdateResult.getModifiedCount());

				Update update = new Update();
				update.set(AppReleaseMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
				update.currentDate(AppReleaseMongodb.FIELD.METADATA.UPDATE_TIME);
				update.set(AppReleaseMongodb.FIELD.LATEST_VERSION, true);
				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, AppReleaseMongodb.class, MongodbConstants.Collection.APP_RELEASE);
				if (updateResult.getModifiedCount() != 1) {
					throw new ConflictBusinessException("应用版本最新版本更新失败");
				}
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("setAppReleaseLatestVersion", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("设置应用发行最新版本失败");
			}
		});

	}

	/**
	 * 删除应用发行
	 *
	 * @param args 参数
	 */
	@NewSpan
	@Lock4j(name = "delete_app_release", keys = {"#args.appId","#args.endpointId","#args.type","#args.appVersion"})
	@BizLog(
		bizId = "app_release:delete_app_release",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void deleteAppRelease(DeleteAppReleaseArgs args) {
		AppReleaseMongodb appReleaseMongodb = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(AppReleaseMongodb.FIELD.APP_ID).is(args.getAppId())
					.and(AppReleaseMongodb.FIELD.ENDPOINT_ID).is(args.getEndpointId())
					.and(AppReleaseMongodb.FIELD.TYPE).is(args.getType())
					.and(AppReleaseMongodb.FIELD.APP_VERSION).is(args.getAppVersion());

				Query query = Query.query(criteria);
				Update update = new Update();
				update.set(AppReleaseMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
				update.currentDate(AppReleaseMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, AppReleaseMongodb.class, MongodbConstants.Collection.APP_RELEASE);
				AppReleaseMongodb deleteApp = mongoTemplate.findAndRemove(query, AppReleaseMongodb.class, MongodbConstants.Collection.APP_RELEASE);
				if (deleteApp == null) {
					throw new ConflictBusinessException("删除应用发行失败，应用版本不存在");
				}
				return mongoTemplate.insert(deleteApp, MongodbConstants.DeletedCollection.APP_RELEASE);
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除应用发行失败");
			} catch (Exception e) {
				log.debug("deleteAppRelease", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除应用发行失败");
			}
		});

		if (appReleaseMongodb != null && appReleaseMongodb.getAndroidApkUrl() != null) {
			publicFileCommonService.deleteFile(args.getAppId().concat("/").concat(FileKeyPrefixConstants.Collection.APP_RELEASE), List.of(appReleaseMongodb.getAndroidApkUrl()));
		}
	}


	/**
	 * 获取应用发行列表
	 *
	 * @param args 参数
	 */
	@NewSpan
	@BizLog(
		bizId = "app_release:get_app_release_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<MetadataAppRelease> getAppReleaseList(String appId, String endpointId, GetAppReleaseArgs args) {
		Criteria criteria = buildCriteria(appId, endpointId, args);
		Query query = Query.query(criteria);

		// 排序
		query.with(Sort.by(Sort.Order.desc(AppReleaseMongodb.FIELD.METADATA.UPDATE_TIME))).with(args.pageable());

		List<AppReleaseMongodb> appReleaseMongodbList = readMongoTemplate.find(query, AppReleaseMongodb.class, MongodbConstants.Collection.APP_RELEASE);

		return getAppReleaseList(appReleaseMongodbList);
	}

	/**
	 * 获取应用发行列表
	 *
	 * @param appId         应用ID
	 * @param endpointId 终端ID
	 * @param args          参数
	 * @return 应用发行分页列表
	 */
	@NewSpan
	@BizLog(
		bizId = "app_release:get_app_release_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<MetadataAppRelease> getAppReleasePageList(String appId, String endpointId, GetAppReleaseArgs args) {
		Criteria criteria = buildCriteria(appId, endpointId, args);
		Query query = Query.query(criteria);
		//
		long total = readMongoTemplate.count(query, MongodbConstants.Collection.APP_RELEASE);

		// 排序
		query.with(Sort.by(Sort.Order.desc(AppReleaseMongodb.FIELD.METADATA.UPDATE_TIME))).with(args.pageable());

		//
		List<AppReleaseMongodb> appReleaseMongodbList = readMongoTemplate.find(query, AppReleaseMongodb.class, MongodbConstants.Collection.APP_RELEASE);

		List<MetadataAppRelease> appReleaseList = getAppReleaseList(appReleaseMongodbList);
		return new Page<>(args, appReleaseList, total);
	}

	public Criteria buildCriteria(String appId, String endpointId, GetAppReleaseArgs args) {
		Criteria criteria = new Criteria();
		Optional.ofNullable(appId).filter(x -> !x.isBlank()).ifPresent(x -> criteria.and(AppReleaseMongodb.FIELD.APP_ID).is(x));
		Optional.ofNullable(endpointId).filter(x -> !x.isBlank()).ifPresent(x -> criteria.and(AppReleaseMongodb.FIELD.ENDPOINT_ID).is(x));
		Optional.ofNullable(args.getType()).filter(x -> !x.isBlank()).ifPresent(x -> criteria.and(AppReleaseMongodb.FIELD.TYPE).is(x));
		Optional.ofNullable(args.getForce()).ifPresent(x -> criteria.and(AppReleaseMongodb.FIELD.FORCE).is(x));
		Optional.ofNullable(args.getLatestVersion()).ifPresent(x -> criteria.and(AppReleaseMongodb.FIELD.LATEST_VERSION).is(x));
		Optional.ofNullable(args.getReleaseVersion()).ifPresent(x -> criteria.and(AppReleaseMongodb.FIELD.RELEASE_VERSION).is(x));
		return criteria;
	}

	List<MetadataAppRelease> getAppReleaseList(List<AppReleaseMongodb> list) {
		Set<String> userIds = list.stream().map(AppReleaseMongodb::getMetadata).toList().stream().flatMap(x -> Stream.of(x.getCreateUserId(), x.getUpdateUserId())).filter(Objects::nonNull).collect(Collectors.toSet());

		// 应用
		Map<String, App> appMap;
		List<String> appIds = list.stream().map(AppReleaseMongodb::getAppId).distinct().collect(Collectors.toList());
		List<App> appList = appClientApiService.getAppList(GetAppArgs.builder()
			.appIds(appIds)
			.build());
		if (!appIds.isEmpty()) {
			appMap = Optional.ofNullable(appList).orElse(Collections.emptyList()).stream().collect(Collectors.toMap(App::getAppId, g -> g));
		} else {
			appMap = Collections.emptyMap();
		}

		// 终端
		Map<String, Endpoint> appEndointMap;
		List<GetEndpointByAppClientArgs.EndpointInfo> endpointInfos = list.stream().map(x -> GetEndpointByAppClientArgs.EndpointInfo.builder().appId(x.getAppId()).endpointId(x.getEndpointId()).build()).distinct().collect(Collectors.toList());
		if (!endpointInfos.isEmpty()) {
			List<Endpoint> endpointByAppList = endpointClientApiService.getEndpointByAppList(GetEndpointByAppClientArgs.builder().EndpointInfos(endpointInfos).build());
			appEndointMap = Optional.ofNullable(endpointByAppList).stream().flatMap(Collection::stream).collect(Collectors.toMap(Endpoint::getEndpointId, x -> x));
		} else {
			appEndointMap = Collections.emptyMap();
		}

		Map<String, AppUser> metadataUserMap = appUserCommonService.getAppUserMapByAppUserIds(cairoSecurityProperties.getCairoAppId(), userIds);
		return list.stream().map(x -> AppReleaseConverter.convertMetadataAppReleaseVersion(x, appMap, appEndointMap, metadataUserMap)).collect(Collectors.toList());
	}

}

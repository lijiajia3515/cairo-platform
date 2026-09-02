package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.wxmp.provider;

import com.baomidou.lock.annotation.Lock4j;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user.GetAppUserClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.provider.CreateWxmpProviderArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.provider.DeleteWxmpProviderArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.provider.GetWxmpProviderArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.provider.ModifyWxmpProviderArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.wxmp.provider.ModifyWxmpProviderStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.wxmp.WxmpProviderMongodb;
import io.github.lijiajia3515.cairo.auth.framework.wx.mp.CairoWxMpService;
import io.github.lijiajia3515.cairo.auth.framework.wx.mp.WxMpProperties;
import io.github.lijiajia3515.cairo.auth.domain.dto.wxmp.provider.MetadataWxmpProvider;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.provider.WxmpProviderConverter;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
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

/**
 * [cairo_web_manage/api] wxmp provider  service
 */
@Slf4j
@Validated
@Component
public class WxmpProviderCairoWebManageApiService {
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final MongoTemplate readMongoTemplate;
	private final CairoWxMpService cairoWxMpService;
	private final WxMpService wxMpService;
	private final AppUserCommonService appUserCommonService;

	WxmpProviderCairoWebManageApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
											 TransactionTemplate transactionTemplate,
											 @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
											 CairoWxMpService cairoWxMpService,
											 WxMpService wxMpService,
											 AppUserCommonService appUserCommonService) {
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.cairoWxMpService = cairoWxMpService;
		this.wxMpService = wxMpService;
		this.appUserCommonService = appUserCommonService;
	}

	/**
	 * 创建微信公众号连接配置
	 *
	 * @param args args
	 */
	@NewSpan
	@BizLog(
		bizId = "wxmp_provider:create_wxmp_provider",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void createWxmpProvider(@Validated CreateWxmpProviderArgs args) {
		String currentUserId = CairoSecurityContextHolder.getAppUserId();

		try {
			//移除配置
			cairoWxMpService.removeConfig(args.getWxmpProviderId());
			//重新添加配置,与数据库保持一致
			WxMpProperties wxMpProperties = new WxMpProperties();
			wxMpProperties.setAppId(args.getWxmpAppId());
			wxMpProperties.setSecret(args.getWxmpSecret());
			wxMpProperties.setToken(args.getWxmpToken());
			wxMpProperties.setAesKey(args.getWxmpAesKey());
			cairoWxMpService.addConfig(args.getWxmpProviderId(), wxMpProperties);
			wxMpService.switchoverTo(args.getWxmpProviderId()).getAccessToken();
		} catch (WxErrorException e) {
			throw new ConflictBusinessException("微信公众号获取token失败,失败原因:".concat(e.getMessage()));
		}

		WxmpProviderMongodb wxmpTemplateMongodb = WxmpProviderMongodb.builder()
			.wxmpProviderId(args.getWxmpProviderId())
			.wxmpProviderName(args.getWxmpProviderName())
			.wxmpAppId(args.getWxmpAppId())
			.wxmpSecret(args.getWxmpSecret())
			.wxmpToken(args.getWxmpToken())
			.wxmpAesKey(args.getWxmpAesKey())
			.enabled(true)
			.metadata(AppUserMetadataMongodb.builder()
				.createUserId(currentUserId)
				.updateUserId(currentUserId)
				.build()
			)
			.build();
		transactionTemplate.executeWithoutResult(status -> {
			try {
				mongoTemplate.insert(wxmpTemplateMongodb, MongodbConstants.Collection.WXMP_PROVIDER);
			} catch (Exception e) {
				log.debug("createWxmpProvider", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("创建微信公众号连接配置异常");
			}
		});
	}

	/**
	 * 修改微信公众号连接配置
	 *
	 * @param args args
	 */
	@NewSpan
	@Lock4j(name = "modify_wxmp_provider", keys = {"#args.wxmpProviderId"})
	@BizLog(
		bizId = "wxmp_provider:modify_wxmp_provider",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifyWxmpProvider(ModifyWxmpProviderArgs args) {
		Criteria existsCriteria = Criteria
			.where(WxmpProviderMongodb.FIELD.WX_MP_PROVIDER_ID).is(args.getWxmpProviderId());
		Query existsQuery = Query.query(existsCriteria);
		WxmpProviderMongodb providerMongodb = mongoTemplate.findOne(existsQuery, WxmpProviderMongodb.class, MongodbConstants.Collection.WXMP_PROVIDER);

		if (providerMongodb == null){
			throw new ConflictBusinessException("微信公众号连接配置不存在");
		}

		if (providerMongodb.isEnabled()) {
			throw new ConflictBusinessException("请禁用后在进行编辑操作");
		}

		try {
			//移除配置
			cairoWxMpService.removeConfig(providerMongodb.getWxmpProviderId());
			//重新添加配置,与数据库保持一致
			WxMpProperties wxMpProperties = new WxMpProperties();
			wxMpProperties.setAppId(args.getWxmpAppId());
			wxMpProperties.setSecret(args.getWxmpSecret());
			wxMpProperties.setToken(args.getWxmpToken());
			wxMpProperties.setAesKey(args.getWxmpAesKey());
			cairoWxMpService.addConfig(providerMongodb.getWxmpProviderId(), wxMpProperties);
			wxMpService.switchoverTo(providerMongodb.getWxmpProviderId()).getAccessToken();
		} catch (WxErrorException e) {
			throw new ConflictBusinessException("微信公众号获取token失败,失败原因:".concat(e.getMessage()));
		}

		transactionTemplate.executeWithoutResult(transactionStatus -> {
			try {
				Criteria updateCriteria = Criteria
					.where(WxmpProviderMongodb.FIELD.WX_MP_PROVIDER_ID).is(args.getWxmpProviderId());
				Query updateQuery = Query.query(updateCriteria);
				Update update = new Update();

				if (args.getWxmpAppId() != null) {
					update.set(WxmpProviderMongodb.FIELD.WX_MP_APP_ID, args.getWxmpAppId());
				}

				if (args.getWxmpProviderName() != null) {
					update.set(WxmpProviderMongodb.FIELD.WX_MP_PROVIDER_NAME, args.getWxmpProviderName());
				}

				if (args.getWxmpSecret() != null) {
					update.set(WxmpProviderMongodb.FIELD.WX_MP_SECRET, args.getWxmpSecret());
				}

				if (args.getWxmpToken() != null) {
					update.set(WxmpProviderMongodb.FIELD.WX_MP_TOKEN, args.getWxmpToken());
				}

				if (args.getWxmpAesKey() != null) {
					update.set(WxmpProviderMongodb.FIELD.WX_MP_AES_KEY, args.getWxmpAesKey());
				}

				update.set(WxmpProviderMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
				update.currentDate(WxmpProviderMongodb.FIELD.METADATA.UPDATE_TIME);
				mongoTemplate.updateFirst(updateQuery, update, WxmpProviderMongodb.class, MongodbConstants.Collection.WXMP_PROVIDER);
			} catch (BusinessException e) {
				throw e;
			} catch (Exception e) {
				log.debug("modifyWxmpProvider", e);
				transactionStatus.setRollbackOnly();
				throw new ConflictBusinessException("修改公众号连接配置失败");
			}
		});

	}

	/**
	 * 修改微信公众号连接配置状态
	 *
	 * @param args args
	 */
	@NewSpan
	@Lock4j(name = "modify_wxmp_provider_status", keys = {"#args.wxmpProviderId"})
	@BizLog(
		bizId = "wxmp_provider:modify_wxmp_provider_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifyWxmpProviderStatus(ModifyWxmpProviderStatusArgs args) {
		Criteria criteria = Criteria
			.where(WxmpProviderMongodb.FIELD.WX_MP_PROVIDER_ID).is(args.getWxmpProviderId());
		Query query = Query.query(criteria);
		WxmpProviderMongodb node = mongoTemplate.findOne(query, WxmpProviderMongodb.class, MongodbConstants.Collection.WXMP_PROVIDER);

		if (node == null) {
			throw new ConflictBusinessException("更新微信模板状态失败，微信模板不存在");
		}

		transactionTemplate.executeWithoutResult(status -> {
			try {
				Update update = new Update();
				if (args.getEnabled() != null) {
					update.set(WxmpProviderMongodb.FIELD.ENABLED, args.getEnabled());
				}

				update.set(WxmpProviderMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());

				update.currentDate(WxmpProviderMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateFirst = mongoTemplate.updateFirst(query, update, WxmpProviderMongodb.class, MongodbConstants.Collection.WXMP_PROVIDER);

				if (updateFirst.getModifiedCount() <= 0) {
					throw new ConflictBusinessException("更新微信公众号配置状态失败");
				}
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyWxmpProviderStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("更新微信公众号配置状态失败");
			}
		});
	}


	/**
	 * 删除微信公众号连接配置
	 *
	 * @param args args
	 */
	@Lock4j(name = "delete_wxmp_provider", keys = {"#args.wxmpProviderId"})
	@BizLog(
		bizId = "wxmp_provider:delete_wxmp_provider",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@NewSpan
	@SneakyThrows
	public void deleteWxmpProvider(@Validated DeleteWxmpProviderArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Criteria stCriteria = Criteria
					.where(WxmpProviderMongodb.FIELD.WX_MP_PROVIDER_ID).is(args.getWxmpProviderId());
				Query stQuery = Query.query(stCriteria);

				WxmpProviderMongodb wxmpTemplateMongodb = mongoTemplate.findOne(stQuery, WxmpProviderMongodb.class, MongodbConstants.Collection.WXMP_PROVIDER);
				if (wxmpTemplateMongodb == null) {
					throw new ConflictBusinessException("删除微信公众号连接配置失败，公众号连接配置不存在");
				}
				if (wxmpTemplateMongodb.isEnabled()) {
					throw new ConflictBusinessException("删除微信公众号连接配置失败，请禁用后再删除");
				}

				Update stUpdate = Update.update(WxmpProviderMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
				stUpdate.currentDate(WxmpProviderMongodb.FIELD.METADATA.UPDATE_TIME);
				mongoTemplate.updateFirst(stQuery, stUpdate, MongodbConstants.Collection.WXMP_PROVIDER);

				WxmpProviderMongodb deletedWxmpProviderMongodb = mongoTemplate.findAndRemove(stQuery, WxmpProviderMongodb.class, MongodbConstants.Collection.WXMP_PROVIDER);
				if (deletedWxmpProviderMongodb != null) {
					mongoTemplate.insert(deletedWxmpProviderMongodb, MongodbConstants.DeletedCollection.WXMP_PROVIDER);
				}

			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("deleteWxmpProvider", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除微信公众号连接配置失败");
			}
		});
	}

	/**
	 * 查询微信公众号连接配置列表
	 *
	 * @param appId appId
	 * @param args  args
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "wxmp_provider:get_wxmp_provider_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<MetadataWxmpProvider> getWxmpProviderList(String appId, @Validated GetWxmpProviderArgs args) {
		Criteria criteria = new Criteria();

		if (args.getEnabled() != null) {
			criteria.and(WxmpProviderMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		if (args.getWxmpProviderIds() != null && !args.getWxmpProviderIds().isEmpty()) {
			criteria.and(WxmpProviderMongodb.FIELD.WX_MP_PROVIDER_ID).in(args.getWxmpProviderIds());
		}

		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.orOperator(
				Criteria.where(WxmpProviderMongodb.FIELD.WX_MP_APP_ID).regex(args.getKeyword()),
				Criteria.where(WxmpProviderMongodb.FIELD.WX_MP_AES_KEY).regex(args.getKeyword()),
				Criteria.where(WxmpProviderMongodb.FIELD.WX_MP_TOKEN).regex(args.getKeyword()),
				Criteria.where(WxmpProviderMongodb.FIELD.WX_MP_SECRET).regex(args.getKeyword())
			);
		}

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.desc(WxmpProviderMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<WxmpProviderMongodb> wxmsTemplateMongodbList = readMongoTemplate.find(query, WxmpProviderMongodb.class, MongodbConstants.Collection.WXMP_PROVIDER);
		return getMetadataWxmpProvider(appId, wxmsTemplateMongodbList);
	}

	/**
	 * 查询微信公众号连接配置分页列表
	 *
	 * @param args query args
	 * @return x
	 */
	@NewSpan
	@BizLog(
		bizId = "wxmp_provider:get_wxmp_provider_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<MetadataWxmpProvider> getWxmpProviderPageList(String appId, @Validated GetWxmpProviderArgs args) {
		Criteria criteria = new Criteria();

		if (args.getEnabled() != null) {
			criteria.and(WxmpProviderMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		if (args.getWxmpProviderIds() != null && !args.getWxmpProviderIds().isEmpty()) {
			criteria.and(WxmpProviderMongodb.FIELD.WX_MP_PROVIDER_ID).in(args.getWxmpProviderIds());
		}

		if (args.getKeyword() != null) {
			criteria.orOperator(
				Criteria.where(WxmpProviderMongodb.FIELD.WX_MP_APP_ID).regex(args.getKeyword()),
				Criteria.where(WxmpProviderMongodb.FIELD.WX_MP_AES_KEY).regex(args.getKeyword()),
				Criteria.where(WxmpProviderMongodb.FIELD.WX_MP_TOKEN).regex(args.getKeyword()),
				Criteria.where(WxmpProviderMongodb.FIELD.WX_MP_SECRET).regex(args.getKeyword())
			);
		}

		Query query = Query.query(criteria);
		long total = readMongoTemplate.count(query, WxmpProviderMongodb.class, MongodbConstants.Collection.WXMP_PROVIDER);

		query.with(args.pageable());
		query.with(Sort.by(Sort.Order.desc(WxmpProviderMongodb.FIELD.METADATA.UPDATE_TIME)));

		List<WxmpProviderMongodb> wxmsTemplateMongodbList = readMongoTemplate.find(query, WxmpProviderMongodb.class, MongodbConstants.Collection.WXMP_PROVIDER);
		List<MetadataWxmpProvider> contents = getMetadataWxmpProvider(appId, wxmsTemplateMongodbList);
		return new Page<>(args, contents, total);
	}


	private List<MetadataWxmpProvider> getMetadataWxmpProvider(String appId, List<WxmpProviderMongodb> mongodbList) {
		Set<String> userIds = mongodbList.stream().map(WxmpProviderMongodb::getMetadata).toList().stream().flatMap(x -> Stream.of(x.getCreateUserId(), x.getUpdateUserId())).filter(Objects::nonNull).collect(Collectors.toSet());
		Map<String, AppUser> metadataUserMap = appUserCommonService.getAppUserMapByAppUserIds(appId, userIds);
		return mongodbList.stream()
			.map(x -> WxmpProviderConverter.convertMetadataWxmpProvider(x, metadataUserMap))
			.collect(Collectors.toList());
	}

}

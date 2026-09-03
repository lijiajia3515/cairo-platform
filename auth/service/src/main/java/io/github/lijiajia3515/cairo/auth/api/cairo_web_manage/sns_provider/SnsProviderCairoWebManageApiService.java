package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.sns_provider;

import cn.binarywang.wx.miniapp.api.WxMaService;
import com.baomidou.lock.annotation.Lock4j;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SnsProviderMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.framework.sns.ProviderPartnerProperties;
import io.github.lijiajia3515.cairo.auth.framework.sns.ProviderTypeProperties;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsPartner;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsProviderProperties;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsType;
import io.github.lijiajia3515.cairo.auth.framework.wx.ma.CairoWxMaService;
import io.github.lijiajia3515.cairo.auth.framework.wx.ma.WxMaProperties;
import io.github.lijiajia3515.cairo.auth.framework.wx.mp.CairoWxMpService;
import io.github.lijiajia3515.cairo.auth.framework.wx.mp.WxMpProperties;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.modules.app.AppCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserTool;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sns_partner.GetProviderPartnerArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.sns_provider.MetadataSnsProvider;
import io.github.lijiajia3515.cairo.auth.domain.dto.sns_provider.ProviderPartner;
import io.github.lijiajia3515.cairo.auth.domain.dto.sns_provider.ProviderType;
import io.github.lijiajia3515.cairo.auth.modules.sns_provider.SnsProviderConverter;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sns_provider.CreateSnsProviderArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sns_provider.DeleteSnsProviderArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sns_provider.GetProviderTypeArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sns_provider.GetSnsProviderArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sns_provider.ModifySnsProviderInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.sns_provider.ModifySnsProviderStatusArgs;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.exception.ParamsErrorBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.micrometer.tracing.annotation.NewSpan;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * [cairo_web_manage/api] sns provider service
 */
@Slf4j
@Validated
@Component
public class SnsProviderCairoWebManageApiService {

	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final AppUserCommonService appUserCommonService;
	private final CairoSecurityProperties cairoSecurityProperties;
	private final WxMpService wxMpService;
	private final WxMaService wxMaService;
	private final CairoWxMpService cairoWxMpService;
	private final CairoWxMaService cairoWxMaService;
	private final SnsProviderProperties snsProviderProperties;
	private final AppCommonService appCommonService;

	public SnsProviderCairoWebManageApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
											   @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
											   TransactionTemplate transactionTemplate,
											   AppUserCommonService appUserCommonService,
											   CairoSecurityProperties cairoSecurityProperties,
											   WxMpService wxMpService,
											   WxMaService wxMaService,
											   CairoWxMpService cairoWxMpService,
											   CairoWxMaService cairoWxMaService,
											   SnsProviderProperties snsProviderProperties,
											   AppCommonService appCommonService) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.appUserCommonService = appUserCommonService;
		this.cairoSecurityProperties = cairoSecurityProperties;
		this.wxMpService = wxMpService;
		this.wxMaService = wxMaService;
		this.cairoWxMpService = cairoWxMpService;
		this.cairoWxMaService = cairoWxMaService;
		this.snsProviderProperties = snsProviderProperties;
		this.appCommonService = appCommonService;
	}

	/**
	 * 获取元素第三方认证提供方 集合模式
	 *
	 * @param args 参数
	 * @return 元素 list
	 */
	@NewSpan
	@BizLog(
		bizId = "sns_provider:get_sns_provider_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<MetadataSnsProvider> getSnsProviderList(@Validated GetSnsProviderArgs args) {
		Criteria criteria = buildCriteria(args);
		Query query = Query.query(criteria)
			.with(Sort.by(Sort.Order.desc(SnsProviderMongodb.FIELD.METADATA.UPDATE_TIME)));
		List<SnsProviderMongodb> list = readMongoTemplate.find(query, SnsProviderMongodb.class, MongodbConstants.Collection.SNS_PROVIDER);
		return getMetadataSnsProviderList(list);
	}

	/**
	 * 获取元素第三方认证提供方 分页模式
	 *
	 * @param args 参数
	 * @return 元素 page
	 */
	@NewSpan
	@BizLog(
		bizId = "sns_provider:get_sns_provider_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public Page<MetadataSnsProvider> getSnsProviderPageList(@Validated GetSnsProviderArgs args) {
		Criteria criteria = buildCriteria(args);

		Query query = Query.query(criteria);
		query.with(Sort.by(
			Sort.Order.desc(SnsProviderMongodb.FIELD.METADATA.UPDATE_TIME)
		));

		long total = readMongoTemplate.count(query, SnsProviderMongodb.class, MongodbConstants.Collection.SNS_PROVIDER);
		query.with(args.pageable());
		List<SnsProviderMongodb> ms = readMongoTemplate.find(query, SnsProviderMongodb.class, MongodbConstants.Collection.SNS_PROVIDER);
		List<MetadataSnsProvider> metadataSnsProviderList = getMetadataSnsProviderList(ms);
		return new Page<>(args, metadataSnsProviderList, total);
	}

	/**
	 * 获取第三方认证类型 集合模式
	 *
	 * @param args 参数
	 * @return 元素 list
	 */
	@NewSpan
	@BizLog(
		bizId = "provider_type:get_provider_type_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<ProviderType> getproviderTypeList(GetProviderTypeArgs args) {
		List<ProviderTypeProperties> properties = snsProviderProperties.getProviderTypes().stream()
			.filter(type -> args.getEnabled().equals(type.getEnabled()) && SnsType.typeValueOf(type.getId()).isPresent())
			.toList();
		return properties.stream().map(p -> ProviderType.builder()
			.providerTypeId(p.getId())
			.providerTypeName(p.getName())
			.enabled(p.getEnabled())
			.build()).collect(Collectors.toList());
	}

	/**
	 * 获取第三方认证厂商 集合模式
	 *
	 * @param args 参数
	 * @return 元素 list
	 */
	@NewSpan
	@BizLog(
		bizId = "sns_partner:get_sns_partner_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<ProviderPartner> getProviderPartnerList(GetProviderPartnerArgs args) {
		List<ProviderPartnerProperties> properties = snsProviderProperties.getProviderPartners().stream()
			.filter(type -> args.getEnabled().equals(type.getEnabled()) && SnsPartner.partnerIdOf(type.getId()).isPresent())
			.toList();
		return properties.stream().map(p -> ProviderPartner.builder()
			.providerPartnerId(p.getId())
			.providerPartnerName(p.getName())
			.providerPartnerIcon(p.getIcon())
			.enabled(p.getEnabled())
			.build()).collect(Collectors.toList());
	}

	/**
	 * 创建第三方认证提供方
	 *
	 * @param args args
	 */
	@NewSpan
	@BizLog(
		bizId = "sns_provider:create_sns_provider",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void createSnsProvider(@Validated CreateSnsProviderArgs args) {

		SnsType type = SnsType.typeValueOf(args.getSnsProviderType()).orElseThrow(() -> new ParamsErrorBusinessException(String.format("参数: 类型：%s 错误", args.getSnsProviderType())));

		SnsPartner partner = SnsPartner.partnerIdOf(args.getSnsProviderPartner()).orElseThrow(() -> new ParamsErrorBusinessException(String.format("参数: 厂商：%s 错误", args.getSnsProviderPartner())));

		if (SnsPartner.WX.equals(partner)) {
			if (SnsType.WX_MP.getTypeValue().equals(type.getTypeValue())) {
				try {
					wxMpService.removeConfigStorage(args.getSnsProviderId());
					WxMpProperties wxMpProperties = new WxMpProperties();
					wxMpProperties.setAppId(args.getClientId());
					wxMpProperties.setSecret(args.getClientSecret());
					cairoWxMpService.addConfig(args.getSnsProviderId(), wxMpProperties);
					wxMpService.switchoverTo(args.getSnsProviderId()).getAccessToken();
				} catch (WxErrorException e) {
					throw new ConflictBusinessException("微信公众号获取token失败,失败原因:".concat(e.getMessage()));
				}
			}

			if (SnsType.WX_WEB.getTypeValue().equals(type.getTypeValue())) {
				try {
					wxMpService.removeConfigStorage(args.getSnsProviderId());
					WxMpProperties wxMpProperties = new WxMpProperties();
					wxMpProperties.setAppId(args.getClientId());
					wxMpProperties.setSecret(args.getClientSecret());
					cairoWxMpService.addConfig(args.getSnsProviderId(), wxMpProperties);
					wxMpService.switchoverTo(args.getSnsProviderId()).getAccessToken();
				} catch (WxErrorException e) {
					throw new ConflictBusinessException("微信网页端获取token失败,失败原因:".concat(e.getMessage()));
				}
			}

			if (SnsType.WX_MA.getTypeValue().equals(type.getTypeValue())) {
				try {
					cairoWxMaService.removeConfig(args.getSnsProviderId());
					WxMaProperties wxMaProperties = new WxMaProperties();
					wxMaProperties.setAppId(args.getClientId());
					wxMaProperties.setSecret(args.getClientSecret());
					cairoWxMaService.addConfig(args.getSnsProviderId(), wxMaProperties);
					wxMaService.switchoverTo(args.getSnsProviderId()).getAccessToken();
				} catch (WxErrorException e) {
					throw new ConflictBusinessException("微信小程序获取token失败,失败原因:".concat(e.getMessage()));
				}
			}
		}
		Criteria typeCriteria = Criteria.where(SnsProviderMongodb.FIELD.APP_ID).is(args.getAppId())
			.and(SnsProviderMongodb.FIELD.SNS_PROVIDER_PARTNER).is(partner.getPartnerId())
			.and(SnsProviderMongodb.FIELD.SNS_PROVIDER_TYPE).is(type.getTypeValue());
		Query typeQuery = Query.query(typeCriteria);
		boolean typeExists = mongoTemplate.exists(typeQuery, SnsProviderMongodb.class, MongodbConstants.Collection.SNS_PROVIDER);

		if (typeExists) {
			throw new ConflictBusinessException("第三方认证提供方类型已存在");
		}

		Criteria nameCriteria = Criteria.where(SnsProviderMongodb.FIELD.SNS_PROVIDER_NAME).is(args.getSnsProviderName());
		Query nameQuery = Query.query(nameCriteria);
		boolean nameExists = mongoTemplate.exists(nameQuery, SnsProviderMongodb.class, MongodbConstants.Collection.SNS_PROVIDER);

		if (nameExists) {
			throw new ConflictBusinessException("第三方认证提供方名称已存在");
		}

		Criteria idCriteria = Criteria.where(SnsProviderMongodb.FIELD.SNS_PROVIDER_ID).is(args.getSnsProviderId());
		Query idQuery = Query.query(idCriteria);
		boolean idExists = mongoTemplate.exists(idQuery, SnsProviderMongodb.class, MongodbConstants.Collection.SNS_PROVIDER);

		if (idExists) {
			throw new ConflictBusinessException("第三方认证提供方id已存在");
		}

		transactionTemplate.executeWithoutResult(status -> {
			try {
				// 插入第三方认证提供方
				SnsProviderMongodb snsProvider = SnsProviderMongodb.builder()
					.appId(args.getAppId())
					.snsProviderId(Optional.ofNullable(args.getSnsProviderId()).orElse((CoreConstants.nextIdStr())))
					.snsProviderName(args.getSnsProviderName())
					.snsProviderPartner(partner.getPartnerId())
					.snsProviderType(type.getTypeValue())
					.clientSecret(args.getClientSecret())
					.clientId(args.getClientId())
					.isAutoRegister(args.getIsAutoRegister())
					.enabled(true)
					.metadata(AppUserMetadataMongodb.builder()
						.createUserId(CairoSecurityContextHolder.getSubappUserId())
						.updateUserId(CairoSecurityContextHolder.getSubappUserId())
						.build())
					.build();
				SnsProviderMongodb insert = mongoTemplate.insert(snsProvider, MongodbConstants.Collection.SNS_PROVIDER);
				log.info("insert snsProvider: {}", insert.getSnsProviderId());
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				status.setRollbackOnly();
				log.info("createSnsProvider", e);
				throw new ConflictBusinessException("第三方认证提供方创建失败");
			}
		});
	}

	/**
	 * 第三方认证提供方修改
	 *
	 * @param args args
	 */
	@NewSpan
	@Lock4j(name = "modify_sns_provider", keys = {"#args.snsProviderId"})
	@BizLog(
		bizId = "sns_provider:modify_sns_provider",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void modifySnsProvider(@Validated ModifySnsProviderInfoArgs args) {
		SnsType type;
		if (args.getSnsProviderType() != null) {
			type = SnsType.typeValueOf(args.getSnsProviderType()).orElseThrow(() -> new ParamsErrorBusinessException(String.format("参数: 类型：%s 错误", args.getSnsProviderType())));
		} else {
			type = null;
		}

		SnsPartner partner;
		if (args.getSnsProviderPartner() != null) {
			partner = SnsPartner.partnerIdOf(args.getSnsProviderPartner()).orElseThrow(() -> new ParamsErrorBusinessException(String.format("参数: 厂商：%s 错误", args.getSnsProviderPartner())));

		} else {
			partner = null;
		}

		Criteria nameCriteria = Criteria
			.where(SnsProviderMongodb.FIELD.SNS_PROVIDER_PARTNER).is(args.getSnsProviderPartner())
			.and(SnsProviderMongodb.FIELD.SNS_PROVIDER_TYPE).is(args.getSnsProviderType())
			.and(SnsProviderMongodb.FIELD.SNS_PROVIDER_NAME).is(args.getSnsProviderName())
			.and(SnsProviderMongodb.FIELD.SNS_PROVIDER_ID).ne(args.getSnsProviderId());
		Query nameQuery = Query.query(nameCriteria);
		boolean nameExists = mongoTemplate.exists(nameQuery, SnsProviderMongodb.class, MongodbConstants.Collection.SNS_PROVIDER);

		if (nameExists) {
			throw new ConflictBusinessException("第三方认证提供方名称已存在");
		}

		UpdateResult result = transactionTemplate.execute(status -> {
			try {
				Criteria criteria = Criteria
					.where(SnsProviderMongodb.FIELD.SNS_PROVIDER_ID).is(args.getSnsProviderId());
				Query query = Query.query(criteria);

				Update update = new Update();

				if (args.getSnsProviderName() != null) {
					update.set(SnsProviderMongodb.FIELD.SNS_PROVIDER_NAME, args.getSnsProviderName());
				}
				if (partner != null) {
					update.set(SnsProviderMongodb.FIELD.SNS_PROVIDER_PARTNER, partner.getPartnerId());
				}
				if (type != null) {
					update.set(SnsProviderMongodb.FIELD.SNS_PROVIDER_TYPE, type.getTypeValue());
				}
				if (args.getClientSecret() != null) {
					update.set(SnsProviderMongodb.FIELD.CLIENT_SECRET, args.getClientSecret());
				}
				if (args.getClientId() != null) {
					update.set(SnsProviderMongodb.FIELD.CLIENT_ID, args.getClientId());
				}
				if (args.getIsAutoRegister() != null) {
					update.set(SnsProviderMongodb.FIELD.IS_AUTO_REGISTER, args.getIsAutoRegister());
				}
				if (args.getAppId() != null) {
					update.set(SnsProviderMongodb.FIELD.APP_ID, args.getAppId());
				}


				update.set(SnsProviderMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());

				update.currentDate(SnsProviderMongodb.FIELD.METADATA.UPDATE_TIME);

				return mongoTemplate.updateFirst(query, update, SnsProviderMongodb.class, MongodbConstants.Collection.SNS_PROVIDER);
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifySnsProvider", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改第三方认证提供方失败");
			}
		});

		if (result == null || result.getModifiedCount() < 1) {
			throw new ConflictBusinessException("修改第三方认证提供方失败");
		}
	}

	/**
	 * 第三方认证提供方状态修改
	 *
	 * @param args args
	 */
	@NewSpan
	@Lock4j(name = "modify_sns_provider_status", keys = {"#args.snsProviderId"})
	@BizLog(
		bizId = "sns_provider:modify_sns_provider_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void modifySnsProviderStatus(ModifySnsProviderStatusArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Query query = Query.query(Criteria.where(SnsProviderMongodb.FIELD.SNS_PROVIDER_ID).is(args.getSnsProviderId()));

				Update update = Update.update(SnsProviderMongodb.FIELD.ENABLED, args.getEnabled());
				update.set(SnsProviderMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				update.currentDate(SnsProviderMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, SnsProviderMongodb.class, MongodbConstants.Collection.SNS_PROVIDER);
				if (updateResult.getModifiedCount() < 1) {
					throw new ConflictBusinessException("修改三方连接状态状态失败");
				}
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifySnsProviderStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改三方连接状态失败");
			}
		});
	}


	/**
	 * 删除第三方认证提供方
	 *
	 * @param args args
	 */

	@NewSpan
	@Lock4j(name = "delete_sns_provider", keys = {"#args.snsProviderId"})
	@BizLog(
		bizId = "sns_provider:delete_sns_provider",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void deleteSnsProvider(@Validated DeleteSnsProviderArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Query query = Query.query(Criteria.where(SnsProviderMongodb.FIELD.SNS_PROVIDER_ID).is(args.getSnsProviderId()));
				Update update = new Update();
				update.set(SnsProviderMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
				update.currentDate(SnsProviderMongodb.FIELD.METADATA.UPDATE_TIME);
				mongoTemplate.updateFirst(query, update, SnsProviderMongodb.class, MongodbConstants.Collection.SNS_PROVIDER);
				SnsProviderMongodb deleteSnsProviderMongodb = mongoTemplate.findAndRemove(query, SnsProviderMongodb.class, MongodbConstants.Collection.SNS_PROVIDER);
				if (deleteSnsProviderMongodb == null) {
					throw new ConflictBusinessException("删除第三方认证提供方失败");
				}
				mongoTemplate.insert(deleteSnsProviderMongodb, MongodbConstants.DeletedCollection.SNS_PROVIDER);

			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				status.setRollbackOnly();
				log.error("removeSnsProvider", e);
				throw new ConflictBusinessException("删除第三方认证提供方失败");
			}

		});
	}

	/**
	 * 构建查询条件
	 *
	 * @param args 查询参数
	 * @return criteria
	 */
	private Criteria buildCriteria(GetSnsProviderArgs args) {
		Criteria criteria = new Criteria();


		if (args.getSnsProviderIds() != null && !args.getSnsProviderIds().isEmpty()) {
			criteria.and(SnsProviderMongodb.FIELD.SNS_PROVIDER_ID).in(args.getSnsProviderIds());
		}

		if (args.getSnsTypes() != null && !args.getSnsTypes().isEmpty()) {
			criteria.and(SnsProviderMongodb.FIELD.SNS_PROVIDER_TYPE).in(args.getSnsTypes());
		}

		if (args.getSnsPartners() != null && !args.getSnsPartners().isEmpty()) {
			criteria.and(SnsProviderMongodb.FIELD.SNS_PROVIDER_PARTNER).in(args.getSnsPartners());
		}

		if (args.getAppId() != null && !args.getAppId().isBlank()) {
			criteria.and(SnsProviderMongodb.FIELD.APP_ID).is(args.getAppId());
		}

		if (args.getEnabled() != null) {
			criteria.and(SnsProviderMongodb.FIELD.ENABLED).is(args.getEnabled());
		}

		if (args.getKeyword() != null && !args.getKeyword().isBlank()) {
			criteria.and(SnsProviderMongodb.FIELD.SNS_PROVIDER_NAME).regex(args.getKeyword());
		}

		return criteria;
	}


	/**
	 * 包装数据
	 *
	 * @param ms ms
	 * @return cairo snsProvider list
	 */
	List<MetadataSnsProvider> getMetadataSnsProviderList(List<SnsProviderMongodb> ms) {

		Set<String> metadataUserIds = CairoAppUserTool.getAppUserMetadataUserIds(ms.stream().map(SnsProviderMongodb::getMetadata).collect(Collectors.toList()));
		Map<String, AppUser> metadataUserMap = Optional.of(metadataUserIds)
			.filter(userIds -> !userIds.isEmpty())
			.map(userIds -> appUserCommonService.getAppUserMapByAppUserIds(cairoSecurityProperties.getCairoAppId(), userIds))
			.orElse(Collections.emptyMap());

		List<String> appIds = ms.stream().map(SnsProviderMongodb::getAppId).distinct().collect(Collectors.toList());
		Map<String, App> appMap = Optional.of(appIds)
			.filter(innerAppIds -> !innerAppIds.isEmpty())
			.map(appCommonService::getAppMapByAppIds)
			.orElse(Collections.emptyMap());

		Map<String, ProviderTypeProperties> snsTypeMap = snsProviderProperties.getProviderTypes().stream()
			.collect(Collectors.toMap(ProviderTypeProperties::getId, x -> x, (x1, x2) -> x1));

		Map<String, ProviderPartnerProperties> snsPartnerMap = snsProviderProperties.getProviderPartners().stream()
			.collect(Collectors.toMap(ProviderPartnerProperties::getId, x -> x, (x1, x2) -> x1));

		return ms.stream().map(x -> SnsProviderConverter.convertMetadataSnsProvider(x, appMap, snsTypeMap, snsPartnerMap, metadataUserMap)).collect(Collectors.toList());
	}
}

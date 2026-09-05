package io.github.lijiajia3515.cairo.auth.api.tenant_app_user.wxmp.tenant_app_user;

import com.baomidou.lock.annotation.Lock4j;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.api.client.client.ClientClientApiService;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.wxmp.WxmpTenantAppUserMongodb;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.provider.WxmpProviderCommonService;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_app_user.wxmp.user.BindTenantAppUserArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.tenant_app_user.wxmp.user.UnBindTenantAppUserArgs;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

/**
 * [tenant_endpoint/api] wxmp service
 */
@Slf4j
@Validated
@Component
public class WxmpTenantAppUserTenantAppUserApiService {

	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final ClientClientApiService clientClientApiService;
	private final WxmpProviderCommonService wxmpProviderCommonService;

	public WxmpTenantAppUserTenantAppUserApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
														 @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
														 TransactionTemplate transactionTemplate,
														 ClientClientApiService clientClientApiService,
														 WxmpProviderCommonService wxmpProviderCommonService) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.clientClientApiService = clientClientApiService;
		this.wxmpProviderCommonService = wxmpProviderCommonService;
	}


/*	*//**
	 * 查询当前应用级用户公众号绑定列表
	 *//*
	@NewSpan
	@BizLog(
		bizId = "wxmp_tenant_app_user:get_my_wxmp_tenant_app_user_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "clientId", value = "#clientId"),
			@BizLog.Param(key = "userId", value = "#userId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<MyWxmpTenantAppUser> getMyWxmpTenantAppUser(String tenantId, String userId) {
		//查询客户端
		List<Client> clientList = clientClientApiService.getClientList(GetClientArgs.builder().clientIds(Collections.singletonList(clientId)).build());
		Client client = Optional.ofNullable(clientList).orElse(Collections.emptyList()).stream().findFirst().orElse(null);

		if (client == null || client.getAccountSnsProviderIds() == null || client.getAccountSnsProviderIds().isEmpty()) {
			return Collections.emptyList();
		}

		Criteria criteria = Criteria.where(WxmpTenantAppUserMongodb.FIELD.USER_ID).is(userId)
			.and(WxmpTenantAppUserMongodb.FIELD.WX_PROVIDER_ID).in(client.getAccountSnsProviderIds());

		Query query = Query.query(criteria);
		List<WxmpTenantAppUserMongodb> wxmpTenantAppUserMongodbs = readMongoTemplate.find(query, WxmpTenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_WXMP);


		Map<String, MetadataWxmpProvider> providerIdMap = wxmpProviderCommonService.getWxmpProviderList(client.getAppId(), GetWxmpProviderArgs.builder().wxmpProviderIds(client.getAccountSnsProviderIds()).build()).stream().collect(Collectors.toMap(MetadataWxmpProvider::getWxmpProviderId, x -> x));

		return client.getAccountSnsProviderIds().stream().map(accountSnsProviderId -> {
			//企业应用级用户三方绑定
			WxmpTenantAppUserMongodb wxmpTenantAppUserMongodb = wxmpTenantAppUserMongodbs.stream().filter(sns -> userId.equals(sns.getUserId()) && accountSnsProviderId.equals(sns.getWxProviderId())).findFirst().orElse(null);
			//三方认证
			MetadataWxmpProvider wxmpProvider = providerIdMap.getOrDefault(accountSnsProviderId, MetadataWxmpProvider.builder().build());

			if (wxmpTenantAppUserMongodb != null) {
				return MyWxmpTenantAppUser.builder()
					.wxProviderId(wxmpTenantAppUserMongodb.getWxProviderId())
					.openId(wxmpTenantAppUserMongodb.getOpenId())
					.bindTime(wxmpTenantAppUserMongodb.getBindTime())
					.isBind(true)
					.accessKey(wxmpProvider.getWxmpAesKey())
					.wxProviderName(wxmpProvider.getWxmpProviderName())
					.build();
			} else {
				return MyWxmpTenantAppUser.builder()
					.wxProviderName(accountSnsProviderId)
					.isBind(false)
					.accessKey(wxmpProvider.getWxmpAesKey())
					.wxProviderName(wxmpProvider.getWxmpProviderName())
					.build();
			}
		}).collect(Collectors.toList());


	}*/

	/**
	 * 绑定三方应用级用户
	 *
	 * @param args args
	 */
	@Lock4j(name = "bind_tenant_app_user", keys = {"#tenantId","#args.wxProviderId","#args.openId"})
	@NewSpan
	@BizLog(
		bizId = "tenant_app_user:bind_tenant_app_user",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)

	public void bindTenantAppUser(String tenantId, BindTenantAppUserArgs args) {
		String userId = CairoSecurityContextHolder.getTenantAppUserId();
		//openId绑定情况
		Criteria openCriteria = Criteria.where(WxmpTenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(WxmpTenantAppUserMongodb.FIELD.WX_PROVIDER_ID).is(args.getWxProviderId())
			.and(WxmpTenantAppUserMongodb.FIELD.OPEN_ID).is(args.getOpenId());
		Query openQuery = Query.query(openCriteria).limit(1);
		WxmpTenantAppUserMongodb opneWxmpTenantAppUserMongodb = mongoTemplate.findOne(openQuery, WxmpTenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_WXMP);


		if (opneWxmpTenantAppUserMongodb == null || opneWxmpTenantAppUserMongodb.getUserId() == null) {
			transactionTemplate.executeWithoutResult(status -> {
				try {
					Criteria userCriteria = Criteria.where(WxmpTenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
						.and(WxmpTenantAppUserMongodb.FIELD.WX_PROVIDER_ID).is(args.getWxProviderId())
						.and(WxmpTenantAppUserMongodb.FIELD.USER_ID).is(userId);
					Query userQuery = Query.query(userCriteria).limit(1);
					WxmpTenantAppUserMongodb wxmpTenantAppUserMongodb = mongoTemplate.findOne(userQuery, WxmpTenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_WXMP);
					if (wxmpTenantAppUserMongodb == null) {
						WxmpTenantAppUserMongodb insert = WxmpTenantAppUserMongodb.builder()
							.tenantId(tenantId)
							.userId(userId)
							.wxProviderId(args.getWxProviderId())
							.openId(args.getOpenId())
							.enabled(true)
							.bindTime(LocalDateTime.now())
							.metadata(TenantAppUserMetadataMongodb.builder().createUserId(userId).updateUserId(userId).build())
							.build();
						WxmpTenantAppUserMongodb tenantAppUserSns = mongoTemplate.insert(insert, MongodbConstants.Collection.TENANT_APP_USER_WXMP);
						log.info("bindTenantAppUserSns insert{}", tenantAppUserSns);
					}
				} catch (ConflictBusinessException e) {
					status.setRollbackOnly();
					throw e;
				} catch (Exception e) {
					log.error("bindTenantAppUserSns", e);
					status.setRollbackOnly();
					throw new ConflictBusinessException("绑定失败");
				}
			});
		}
	}

	/**
	 * 解绑三方应用级用户
	 *
	 * @param args args
	 */
	@NewSpan
	@Lock4j(name = "unbind_tenant_app_user", keys = {"#tenantId","#args.wxProviderId","#args.openId"})
	@BizLog(
		bizId = "tenant_app_user:unbind_tenant_app_user",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void unbindTenantAppUser(String tenantId, UnBindTenantAppUserArgs args) {
		Criteria openCriteria = Criteria.where(WxmpTenantAppUserMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(WxmpTenantAppUserMongodb.FIELD.WX_PROVIDER_ID).is(args.getWxProviderId())
			.and(WxmpTenantAppUserMongodb.FIELD.OPEN_ID).is(args.getOpenId());
		Query openQuery = Query.query(openCriteria).limit(1);
		WxmpTenantAppUserMongodb openAppUserWxmpSnsMongodb = mongoTemplate.findOne(openQuery, WxmpTenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_WXMP);
		if (openAppUserWxmpSnsMongodb == null) {
			throw new ConflictBusinessException("当前绑定信息不存在");
		}
		transactionTemplate.executeWithoutResult(status -> {
			try {
				//解绑
				Update update = new Update();
				update.set(WxmpTenantAppUserMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
				update.currentDate(WxmpTenantAppUserMongodb.FIELD.METADATA.UPDATE_TIME);
				mongoTemplate.updateFirst(openQuery, update, WxmpTenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_WXMP);
				WxmpTenantAppUserMongodb deletedAccountSns = mongoTemplate.findAndRemove(openQuery, WxmpTenantAppUserMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_WXMP);
				if (deletedAccountSns == null) {
					throw new ConflictBusinessException("解绑失败");
				}
				mongoTemplate.insert(deletedAccountSns, MongodbConstants.DeletedCollection.TENANT_APP_USER_WXMP);
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("unbindTenantAppUserSns", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("解绑失败");
			}
		});
	}
}

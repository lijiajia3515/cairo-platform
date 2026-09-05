package io.github.lijiajia3515.cairo.auth.api.app_user.wxmp.app_user;

import com.baomidou.lock.annotation.Lock4j;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.api.client.client.ClientClientApiService;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.wxmp.WxmpAppUserMongodb;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.wxmp.BindAppUserWxmpArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.app_user.wxmp.UnBindAppUserWxmpArgs;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.provider.WxmpProviderCommonService;
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
 * [endpoint/api] app_user wxmp service
 */
@Slf4j
@Validated
@Component
public class WxmpAppUserAppUserApiService {
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final ClientClientApiService clientClientApiService;
	private final WxmpProviderCommonService wxmpProviderCommonService;

	public WxmpAppUserAppUserApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
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
		bizId = "app_user_wxmp:get_my_app_user_wxmp_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "clientId", value = "#clientId"),
			@BizLog.Param(key = "userId", value = "#userId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<MyAppUserWxmp> getMyAppUserWxmp(String clientId, String userId) {
		//查询客户端
		List<Client> clientList = clientClientApiService.getClientList(GetClientArgs.builder().clientIds(Collections.singletonList(clientId)).build());
		Client client = Optional.ofNullable(clientList).orElse(Collections.emptyList()).stream().findFirst().orElse(null);

		if (client == null || client.getAccountSnsProviderIds() == null || client.getAccountSnsProviderIds().isEmpty()) {
			return Collections.emptyList();
		}

		Criteria criteria = Criteria.where(WxmpAppUserMongodb.FIELD.USER_ID).is(userId)
			.and(WxmpAppUserMongodb.FIELD.WX_PROVIDER_ID).in(client.getAccountSnsProviderIds());

		Query query = Query.query(criteria);
		List<WxmpAppUserMongodb> appUserWxmpSns = readMongoTemplate.find(query, WxmpAppUserMongodb.class, MongodbConstants.Collection.WXMP_APP_USER);


		Map<String, MetadataWxmpProvider> providerIdMap = wxmpProviderCommonService.getWxmpProviderList(client.getAppId(), GetWxmpProviderArgs.builder().wxmpProviderIds(client.getAccountSnsProviderIds()).build()).stream().collect(Collectors.toMap(MetadataWxmpProvider::getWxmpProviderId, x -> x));

		return client.getAccountSnsProviderIds().stream().map(accountSnsProviderId -> {
			//应用级用户三方绑定
			WxmpAppUserMongodb appUserWxmpSnsMongodb = appUserWxmpSns.stream().filter(sns -> userId.equals(sns.getUserId()) && accountSnsProviderId.equals(sns.getWxProviderId())).findFirst().orElse(null);
			//三方认证
			MetadataWxmpProvider wxmpProvider = providerIdMap.getOrDefault(accountSnsProviderId, MetadataWxmpProvider.builder().build());

			if (appUserWxmpSnsMongodb != null) {
				return MyAppUserWxmp.builder()
					.wxProviderId(appUserWxmpSnsMongodb.getWxProviderId())
					.openId(appUserWxmpSnsMongodb.getOpenId())
					.bindTime(appUserWxmpSnsMongodb.getBindTime())
					.isBind(true)
					.accessKey(wxmpProvider.getWxmpAesKey())
					.wxProviderName(wxmpProvider.getWxmpProviderName())
					.build();
			} else {
				return MyAppUserWxmp.builder()
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
	@NewSpan
	@BizLog(
		bizId = "app_user_wxmp:bind_app_user_wxmp",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)

	public void bindAppUserWxmp(BindAppUserWxmpArgs args) {
		String userId = CairoSecurityContextHolder.getAppUserId();
		//openId绑定情况
		Criteria openCriteria = Criteria.where(WxmpAppUserMongodb.FIELD.WX_PROVIDER_ID).is(args.getWxProviderId())
			.and(WxmpAppUserMongodb.FIELD.OPEN_ID).is(args.getOpenId());
		Query openQuery = Query.query(openCriteria).limit(1);
		WxmpAppUserMongodb openWxmpAppUserMongodb = mongoTemplate.findOne(openQuery, WxmpAppUserMongodb.class, MongodbConstants.Collection.WXMP_APP_USER);


		if (openWxmpAppUserMongodb == null || openWxmpAppUserMongodb.getUserId() == null) {
			transactionTemplate.executeWithoutResult(status -> {
				try {
					Criteria userCriteria = Criteria.where(WxmpAppUserMongodb.FIELD.WX_PROVIDER_ID).is(args.getWxProviderId())
						.and(WxmpAppUserMongodb.FIELD.USER_ID).is(userId);
					Query userQuery = Query.query(userCriteria).limit(1);
					WxmpAppUserMongodb appUserWxmpSnsMongodb = mongoTemplate.findOne(userQuery, WxmpAppUserMongodb.class, MongodbConstants.Collection.WXMP_APP_USER);
					if (appUserWxmpSnsMongodb == null) {
						WxmpAppUserMongodb insert = WxmpAppUserMongodb.builder()
							.userId(userId)
							.wxProviderId(args.getWxProviderId())
							.openId(args.getOpenId())
							.enabled(true)
							.bindTime(LocalDateTime.now())
							.metadata(AppUserMetadataMongodb.builder().createUserId(userId).updateUserId(userId).build())
							.build();
						WxmpAppUserMongodb accountSns = mongoTemplate.insert(insert, MongodbConstants.Collection.WXMP_APP_USER);
						log.info("bindAppUserSns insert{}", accountSns);
					}
				} catch (ConflictBusinessException e) {
					status.setRollbackOnly();
					throw e;
				} catch (Exception e) {
					log.error("bindAppUserSns", e);
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
	@Lock4j(name = "unbind_app_user_sns", keys = {"#args.wxProviderId","#args.openId"})
	@BizLog(
		bizId = "app_user_sns:unbind_app_user_sns",
		scope = "write",
		params = {
			@BizLog.Param(key = "appUserId", value = "#appUserId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void unbindAppUserWxmp(UnBindAppUserWxmpArgs args) {
		Criteria openCriteria = Criteria.where(WxmpAppUserMongodb.FIELD.WX_PROVIDER_ID).is(args.getWxProviderId())
			.and(WxmpAppUserMongodb.FIELD.OPEN_ID).is(args.getOpenId());
		Query openQuery = Query.query(openCriteria).limit(1);
		WxmpAppUserMongodb openWxmpAppUserMongodb = mongoTemplate.findOne(openQuery, WxmpAppUserMongodb.class, MongodbConstants.Collection.WXMP_APP_USER);
		if (openWxmpAppUserMongodb == null) {
			throw new ConflictBusinessException("当前绑定信息不存在");
		}
			transactionTemplate.executeWithoutResult(status -> {
				try {
					//解绑
					Update update = new Update();
					update.set(WxmpAppUserMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
					update.currentDate(WxmpAppUserMongodb.FIELD.METADATA.UPDATE_TIME);
					mongoTemplate.updateFirst(openQuery, update, WxmpAppUserMongodb.class, MongodbConstants.Collection.WXMP_APP_USER);
					WxmpAppUserMongodb deletedAccountSns = mongoTemplate.findAndRemove(openQuery, WxmpAppUserMongodb.class, MongodbConstants.Collection.WXMP_APP_USER);
					if (deletedAccountSns == null) {
						throw new ConflictBusinessException("解绑失败");
					}
					mongoTemplate.insert(deletedAccountSns, MongodbConstants.DeletedCollection.WXMP_APP_USER);
				} catch (ConflictBusinessException e) {
					status.setRollbackOnly();
					throw e;
				} catch (Exception e) {
					log.debug("unbindAppUserSns", e);
					status.setRollbackOnly();
					throw new ConflictBusinessException("解绑失败");
				}
			});
		}
}

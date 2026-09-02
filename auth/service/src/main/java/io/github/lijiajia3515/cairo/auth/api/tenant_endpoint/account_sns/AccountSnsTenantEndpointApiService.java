package io.github.lijiajia3515.cairo.auth.api.tenant_endpoint.account_sns;

import com.baomidou.lock.annotation.Lock4j;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountSnsMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.sns.ProviderPartnerProperties;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsProviderProperties;
import io.github.lijiajia3515.cairo.auth.domain.api.endpoint.account_sns.GetMyAccountSnsArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.endpoint.account_sns.MyAccountSns;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.modules.sns.SnsCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.sns.SnsToken;
import io.github.lijiajia3515.cairo.auth.domain.dto.sns_provider.SnsProvider;
import io.github.lijiajia3515.cairo.auth.modules.sns_provider.SnsProviderCommonService;
import io.github.lijiajia3515.cairo.auth.domain.api.client.sns_provider.GetSnsProviderArgs;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * [tenant_endpoint/api] account sns service
 */
@Slf4j
@Validated
@Component
public class AccountSnsTenantEndpointApiService {
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final SnsCommonService snsCommonService;
	private final SnsProviderProperties snsProviderProperties;
	private final SnsProviderCommonService snsProviderCommonService;

	public AccountSnsTenantEndpointApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
												 @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
												 TransactionTemplate transactionTemplate,
												 SnsCommonService snsCommonService,
												 SnsProviderProperties snsProviderProperties,
												 SnsProviderCommonService snsProviderCommonService) {
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.snsCommonService = snsCommonService;
		this.snsProviderProperties = snsProviderProperties;
		this.snsProviderCommonService = snsProviderCommonService;
	}


	/**
	 * 查询当前账号三方绑定列表
	 */
	@NewSpan
	@BizLog(
		bizId = "account_sns:get_my_account_sns_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "clientId", value = "#accountId"),
			@BizLog.Param(key = "accountId", value = "#accountId"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<MyAccountSns> getMyAccountSnsList(String appId, String accountId, GetMyAccountSnsArgs args) {
		//查询三方厂商
		List<ProviderPartnerProperties> partnerPropertiesList = snsProviderProperties.getProviderPartners().stream().filter(ProviderPartnerProperties::getEnabled).toList();

		// 查询第三方账号关系
		Criteria criteria = Criteria.where(AccountSnsMongodb.FIELD.ACCOUNT_ID).is(accountId)
			.and(AccountSnsMongodb.FIELD.ENABLED).is(true);
		Query query = Query.query(criteria);
		List<AccountSnsMongodb> accountSnsMongodbs = readMongoTemplate.find(query, AccountSnsMongodb.class, MongodbConstants.Collection.ACCOUNT_SNS);

		List<SnsProvider> snsProviderList = snsProviderCommonService.getSnsProviderList(GetSnsProviderArgs.builder()
			.snsTypes(args.getSnsTypes())
			.enabled(true)
			.snsPartners(partnerPropertiesList.stream().map(ProviderPartnerProperties::getId).toList())
			.appId(appId).build());

		return snsProviderList.stream().map(snsProvider -> {
			//账号三方绑定
			AccountSnsMongodb accountSnsMongodb = accountSnsMongodbs.stream().filter(asm -> snsProvider.getSnsProviderPartnerId().equals(asm.getSnsPartnerId())).findFirst().orElse(null);

			if (accountSnsMongodb != null) {
				return MyAccountSns.builder()
					.snsPartnerId(snsProvider.getSnsProviderPartnerId())
					.snsPartnerName(snsProvider.getSnsProviderPartnerName())
					.snsPartnerIcon(snsProvider.getSnsProviderPartnerIcon())
					.snsPartnerOpenId(accountSnsMongodb.getSnsPartnerOpenId())
					.bindTime(accountSnsMongodb.getBindTime())
					.isBind(true)
					.snsProviderId(snsProvider.getSnsProviderId())
					.nickname(accountSnsMongodb.getNickname())
					.avatarUrl(accountSnsMongodb.getAvatarUrl())
					.clientId(snsProvider.getClientId())
					.build();
			} else {
				return MyAccountSns.builder()
					.snsPartnerId(snsProvider.getSnsProviderPartnerId())
					.snsPartnerName(snsProvider.getSnsProviderPartnerName())
					.snsPartnerIcon(snsProvider.getSnsProviderPartnerIcon())
					.isBind(false)
					.snsProviderId(snsProvider.getSnsProviderId())
					.clientId(snsProvider.getClientId())
					.build();
			}
		}).collect(Collectors.toList());
	}

	/**
	 * 绑定三方账号
	 *
	 * @param args args
	 */
	@NewSpan
	@BizLog(
		bizId = "account_sns:bind_account_sns",
		scope = "read",
		params = {
			@BizLog.Param(key = "accountId", value = "#accountId"),
			@BizLog.Param(key = "snsToken", value = "#snsToken")
		}
	)
	@Lock4j(name = "bind_account_sns", keys = {"#accountId", "#snsToken"})
	public void bindAccountSns(@Valid @NotNull String accountId, @Valid @NotNull String snsToken) {
		//查询
		SnsToken snsTokenObj;
		try {
			snsTokenObj = snsCommonService.verifySnsToken(snsToken);
		} catch (Exception e) {
			throw new ConflictBusinessException(String.format("绑定失败（%s）", e.getMessage()));
		}


		//unionId绑定情况
		Criteria openCriteria = Criteria
			.where(AccountSnsMongodb.FIELD.SNS_PARTNER_ID).is(snsTokenObj.getPartnerId())
			.and(AccountSnsMongodb.FIELD.SNS_PARTNER_OPEN_ID).is(snsTokenObj.getPartnerOpenId());
		Query openQuery = Query.query(openCriteria).limit(1);
		AccountSnsMongodb openAccountSnsMongodb = mongoTemplate.findOne(openQuery, AccountSnsMongodb.class, MongodbConstants.Collection.ACCOUNT_SNS);


		if (openAccountSnsMongodb == null || openAccountSnsMongodb.getAccountId() == null) {
			transactionTemplate.executeWithoutResult(status -> {
				try {
					Criteria accountCriteria = Criteria
						.where(AccountSnsMongodb.FIELD.SNS_PARTNER_ID).is(snsTokenObj.getPartnerId())
						.and(AccountSnsMongodb.FIELD.ACCOUNT_ID).is(accountId);
					Query accountQuery = Query.query(accountCriteria).limit(1);
					AccountSnsMongodb accountSnsMongodb = mongoTemplate.findOne(accountQuery, AccountSnsMongodb.class, MongodbConstants.Collection.ACCOUNT_SNS);
					if (accountSnsMongodb == null) {
						AccountSnsMongodb insert = AccountSnsMongodb.builder()
							.recordId(CoreConstants.SNOWFLAKE.nextIdStr())
							.accountId(accountId)
							.snsPartnerId(snsTokenObj.getPartnerId())
							.snsPartnerOpenId(snsTokenObj.getPartnerOpenId())
							.enabled(true)
							.nickname(snsTokenObj.getNickName())
							.avatarUrl(snsTokenObj.getAvatarUrl())
							.bindTime(LocalDateTime.now())
							.metadata(AccountMetadataMongodb.builder().updateAccountId(accountId).updateAccountId(accountId).build())
							.build();
						AccountSnsMongodb accountSns = mongoTemplate.insert(insert, MongodbConstants.Collection.ACCOUNT_SNS);
						log.info("bindAccountSns insert{}", accountSns);
					} else {
						//修改
						Update update = Update.update(AccountSnsMongodb.FIELD.SNS_PARTNER_OPEN_ID, snsTokenObj.getPartnerOpenId());
						update.set(AccountSnsMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, accountId);
						update.currentDate(AccountSnsMongodb.FIELD.METADATA.UPDATE_TIME);
						UpdateResult updateResult = mongoTemplate.updateFirst(accountQuery, update, AccountSnsMongodb.class, MongodbConstants.Collection.ACCOUNT_SNS);
						log.info("bindAccountSns update{}", updateResult);
					}
				} catch (ConflictBusinessException e) {
					status.setRollbackOnly();
					throw e;
				} catch (Exception e) {
					log.error("bindAccountSns", e);
					status.setRollbackOnly();
					throw new ConflictBusinessException("绑定失败");
				}
			});
		}

		if (openAccountSnsMongodb != null && openAccountSnsMongodb.getAccountId() != null) {
			//账号是否存在多种第三方登录方式
			Criteria accountSnsCriteria = Criteria
				.where(AccountSnsMongodb.FIELD.ACCOUNT_ID).is(openAccountSnsMongodb.getAccountId())
				.and(AccountSnsMongodb.FIELD.SNS_PARTNER_OPEN_ID).ne(snsTokenObj.getPartnerOpenId());
			Query accountSnsQuery = Query.query(accountSnsCriteria);
			boolean exists = mongoTemplate.exists(accountSnsQuery, AccountSnsMongodb.class, MongodbConstants.Collection.ACCOUNT_SNS);
			if (!exists) {
				//账号
				Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(openAccountSnsMongodb.getAccountId());
				Query accountQuery = Query.query(accountCriteria).limit(1);
				AccountMongodb accountMongodb = mongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
				if (accountMongodb == null || (accountMongodb.getPhoneNumber() == null && accountMongodb.getUsername() == null)) {
					throw new ConflictBusinessException("绑定失败（绑定账号是当前唯一登录方式）");
				}
			}
			transactionTemplate.executeWithoutResult(status -> {
				try {
					//解绑原账号
					Update update = new Update();
					update.set(AccountSnsMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getAccountId());
					update.currentDate(AccountSnsMongodb.FIELD.METADATA.UPDATE_TIME);
					mongoTemplate.updateFirst(openQuery, update, AccountSnsMongodb.class, MongodbConstants.Collection.ACCOUNT_SNS);
					AccountSnsMongodb deletedAccountSns = mongoTemplate.findAndRemove(openQuery, AccountSnsMongodb.class, MongodbConstants.Collection.ACCOUNT_SNS);
					if (deletedAccountSns != null) {
						mongoTemplate.insert(deletedAccountSns, MongodbConstants.DeletedCollection.ACCOUNT_SNS);
					}

					//绑定新账号
					Criteria accountCriteria = Criteria.where(AccountSnsMongodb.FIELD.SNS_PARTNER_ID).is(snsTokenObj.getPartnerId())
						.and(AccountSnsMongodb.FIELD.ACCOUNT_ID).is(accountId);
					Query accountQuery = Query.query(accountCriteria).limit(1);
					AccountSnsMongodb accountSnsMongodb = mongoTemplate.findOne(accountQuery, AccountSnsMongodb.class, MongodbConstants.Collection.ACCOUNT_SNS);
					if (accountSnsMongodb == null) {
						AccountSnsMongodb insert = AccountSnsMongodb.builder()
							.recordId(CoreConstants.SNOWFLAKE.nextIdStr())
							.accountId(accountId)
							.snsPartnerId(snsTokenObj.getPartnerId())
							.snsPartnerOpenId(snsTokenObj.getPartnerOpenId())
							.enabled(true)
							.nickname(snsTokenObj.getNickName())
							.avatarUrl(snsTokenObj.getAvatarUrl())
							.bindTime(LocalDateTime.now())
							.metadata(AccountMetadataMongodb.builder().updateAccountId(accountId).updateAccountId(accountId).build())
							.build();
						AccountSnsMongodb accountSns = mongoTemplate.insert(insert, MongodbConstants.Collection.ACCOUNT_SNS);
						log.info("bindAccountSns insert{}", accountSns);
					} else {
						//修改
						Update openIdUpdate = Update.update(AccountSnsMongodb.FIELD.SNS_PARTNER_OPEN_ID, snsTokenObj.getPartnerOpenId());
						update.set(AccountSnsMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, accountId);
						update.currentDate(AccountSnsMongodb.FIELD.METADATA.UPDATE_TIME);
						UpdateResult updateResult = mongoTemplate.updateFirst(accountQuery, openIdUpdate, AccountSnsMongodb.class, MongodbConstants.Collection.ACCOUNT_SNS);
						log.info("bindAccountSns update{}", updateResult);
					}
				} catch (ConflictBusinessException e) {
					status.setRollbackOnly();
					throw e;
				} catch (Exception e) {
					log.debug("bindAccountSns", e);
					status.setRollbackOnly();
					throw new ConflictBusinessException("绑定失败");
				}
			});
		}
	}

	/**
	 * 解绑三方账号
	 *
	 * @param accountId    账号ID
	 * @param snsPartnerId 第三方账号厂商ID
	 */
	@NewSpan
	@BizLog(
		bizId = "account_sns:unbind_account_sns",
		scope = "read",
		params = {
			@BizLog.Param(key = "accountId", value = "#accountId"),
			@BizLog.Param(key = "snsPartnerId", value = "#snsPartnerId")
		}
	)
	@Lock4j(name = "unbind_account_sns", keys = {"#accountId", "#snsPartnerId"})
	public void unbindAccountSns(@Valid @NotNull String accountId, @Valid @NotNull String snsPartnerId) {
		Criteria openCriteria = Criteria.where(AccountSnsMongodb.FIELD.SNS_PARTNER_ID).is(snsPartnerId)
			.and(AccountSnsMongodb.FIELD.ACCOUNT_ID).is(accountId);
		Query openQuery = Query.query(openCriteria);
		AccountSnsMongodb accountSnsMongodb = mongoTemplate.findOne(openQuery, AccountSnsMongodb.class, MongodbConstants.Collection.ACCOUNT_SNS);
		if (accountSnsMongodb == null) {
			throw new ConflictBusinessException("解绑失败（当前绑定信息不存在）");
		}

		if (accountSnsMongodb.getAccountId() == null) {
			transactionTemplate.executeWithoutResult(status -> {
				try {
					//解绑
					Update update = new Update();
					update.set(AccountSnsMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getAccountId());
					update.currentDate(AccountSnsMongodb.FIELD.METADATA.UPDATE_TIME);
					mongoTemplate.updateFirst(openQuery, update, AccountSnsMongodb.class, MongodbConstants.Collection.ACCOUNT_SNS);
					AccountSnsMongodb deletedAccountSns = mongoTemplate.findAndRemove(openQuery, AccountSnsMongodb.class, MongodbConstants.Collection.ACCOUNT_SNS);
					if (deletedAccountSns != null) {
						mongoTemplate.insert(deletedAccountSns, MongodbConstants.DeletedCollection.ACCOUNT_SNS);
					}

				} catch (ConflictBusinessException e) {
					status.setRollbackOnly();
					throw e;
				} catch (Exception e) {
					log.debug("unbindAccountSns", e);
					status.setRollbackOnly();
					throw new ConflictBusinessException("解绑失败");
				}
			});
		}
		if (accountSnsMongodb.getAccountId() != null) {
			//账号是否存在多种第三方登录方式
			Criteria accountSnsCriteria = Criteria.where(AccountSnsMongodb.FIELD.ACCOUNT_ID).is(accountSnsMongodb.getAccountId())
				.and(AccountSnsMongodb.FIELD.SNS_PARTNER_OPEN_ID).ne(accountSnsMongodb.getSnsPartnerOpenId());
			Query accountSnsQuery = Query.query(accountSnsCriteria);
			boolean exists = mongoTemplate.exists(accountSnsQuery, AccountSnsMongodb.class, MongodbConstants.Collection.ACCOUNT_SNS);
			if (!exists) {
				//账号
				Criteria accountCriteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(accountSnsMongodb.getAccountId());
				Query accountQuery = Query.query(accountCriteria).limit(1);
				AccountMongodb accountMongodb = mongoTemplate.findOne(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
				if (accountMongodb == null || (accountMongodb.getPhoneNumber() == null && accountMongodb.getUsername() == null)) {
					throw new ConflictBusinessException("解绑失败(绑定账号是当前唯一登录方式)");
				}
			}
			transactionTemplate.executeWithoutResult(status -> {
				try {
					//解绑
					Update update = new Update();
					update.set(AccountSnsMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getAccountId());
					update.currentDate(AccountSnsMongodb.FIELD.METADATA.UPDATE_TIME);
					mongoTemplate.updateFirst(openQuery, update, AccountSnsMongodb.class, MongodbConstants.Collection.ACCOUNT_SNS);
					AccountSnsMongodb deletedAccountSns = mongoTemplate.findAndRemove(openQuery, AccountSnsMongodb.class, MongodbConstants.Collection.ACCOUNT_SNS);
					if (deletedAccountSns == null) {
						throw new ConflictBusinessException("解绑失败");
					}
					mongoTemplate.insert(deletedAccountSns, MongodbConstants.DeletedCollection.ACCOUNT_SNS);
				} catch (ConflictBusinessException e) {
					status.setRollbackOnly();
					throw e;
				} catch (Exception e) {
					log.debug("unbindAccountSns", e);
					status.setRollbackOnly();
					throw new ConflictBusinessException("解绑失败");
				}
			});
		}

	}

}

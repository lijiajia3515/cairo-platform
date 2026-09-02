package io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_authorization.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.AppUserAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.TenantAppUserAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.modules.app_user_authorization.AppUserAuthorizationStatus;
import io.github.lijiajia3515.cairo.auth.modules.tenant_app_user_authorization.TenantAppUserAuthorizationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 修改企业用户令牌过期状态
 */
@Slf4j
@Component
public class TenantAppUserAuthorizationStatusJob {
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;


	public TenantAppUserAuthorizationStatusJob(MongoTemplate mongoTemplate, TransactionTemplate transactionTemplate) {
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
	}

	@XxlJob("modifyTenantAppUserEndpointAuthorizationStatusJob")
	public void modifyTenantAppUserTokenStatusJob() {
		XxlJobHelper.log("job start");
		try {
			Criteria appUserAuthorizationCriteria = Criteria
				.where(TenantAppUserAuthorizationMongodb.FIELD.STATUS).is(AppUserAuthorizationStatus.OK.getStatusValue())
				.and(TenantAppUserAuthorizationMongodb.FIELD.REFRESH_TOKEN.EXPIRES_AT).lte(LocalDateTime.now());
			Query appUserauthorizationQuery = Query.query(appUserAuthorizationCriteria);

			List<TenantAppUserAuthorizationMongodb> appUserAuthorizationMongodbs = mongoTemplate.find(appUserauthorizationQuery, TenantAppUserAuthorizationMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_AUTHORIZATION);
			appUserAuthorizationMongodbs.forEach(appUserAuthorizationMongodb ->
				transactionTemplate.executeWithoutResult(status -> {
				try {
					//修改状态为已过期
					Query authorizationQuery = Query.query(Criteria
						.where(TenantAppUserAuthorizationMongodb.FIELD.TOKEN_ID).is(appUserAuthorizationMongodb.getTokenId()));

					Update update = new Update();
					update.set(TenantAppUserAuthorizationMongodb.FIELD.STATUS, AppUserAuthorizationStatus.EXPIRED.getStatusValue());
					update.currentDate(TenantAppUserAuthorizationMongodb.FIELD.LOGOUT_TIME);
					update.currentDate(TenantAppUserAuthorizationMongodb.FIELD.UPDATE_TIME);

					mongoTemplate.findAndModify(authorizationQuery, update, TenantAppUserAuthorizationMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_AUTHORIZATION);
				} catch (Exception e) {
					log.info("修改企业用户会话状态失败：", e);
				}
			}));
		} catch (Exception e) {
			log.error("error {}", e.getMessage());
			XxlJobHelper.log(e.getMessage());
			XxlJobHelper.handleFail("job fail");
			throw new RuntimeException(e);
		}
		XxlJobHelper.log("job end");
	}

	/**
	 * 删除过期会话任务
	 */
	@XxlJob("deleteTenantAppUserAuthorizationJob")
	public void deleteTenantAppUserAuthorizationJob() {
		XxlJobHelper.log("deleteTenantAppUserAuthorizationJob start");
		try {
			Criteria accountauthorizationCriteria = Criteria
				.where(TenantAppUserAuthorizationMongodb.FIELD.STATUS).ne(TenantAppUserAuthorizationStatus.OK.getStatusValue())
				.and(TenantAppUserAuthorizationMongodb.FIELD.REFRESH_TOKEN.EXPIRES_AT).lte(LocalDateTime.now().minusDays(7));
			Query accountauthorizationQuery = Query.query(accountauthorizationCriteria);

			List<TenantAppUserAuthorizationMongodb> removeList = mongoTemplate.findAllAndRemove(accountauthorizationQuery, TenantAppUserAuthorizationMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_AUTHORIZATION);
			XxlJobHelper.log("删除过期会话：{}条",removeList.size());
			if (!removeList.isEmpty()) {
				mongoTemplate.insert(removeList, MongodbConstants.DeletedCollection.TENANT_APP_USER_AUTHORIZATION);
			}
		} catch (Exception e) {
			log.error("deleteTenantAppUserAuthorizationJob error {}", e.getMessage());
			XxlJobHelper.log(e.getMessage());
			XxlJobHelper.handleFail("deleteTenantAppUserAuthorizationJob fail");
			throw new RuntimeException(e);
		}
		XxlJobHelper.log("deleteTenantAppUserAuthorizationJob end");
	}
}

package io.github.lijiajia3515.cairo.auth.modules.app_user_authorization.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.authorization.AppUserAuthorizationMongodb;
import io.github.lijiajia3515.cairo.auth.modules.app_user_authorization.AppUserAuthorizationCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_user_authorization.AppUserAuthorizationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 更新终端用户令牌过期状态
 */
@Slf4j
@Component
public class AppUserAuthorizationStatusJob {
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final AppUserAuthorizationCommonService appUserAuthorizationCommonService;


	public AppUserAuthorizationStatusJob(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
												 TransactionTemplate transactionTemplate,
												 AppUserAuthorizationCommonService appUserAuthorizationCommonService) {
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.appUserAuthorizationCommonService = appUserAuthorizationCommonService;
	}

	@XxlJob("modifyAppUserAuthorizationStatusJob")
	public void modifyAppUserTokenStatusJob() {
		XxlJobHelper.log("modifyAppUserAuthorizationStatusJob start");
		try {
			Criteria appUserAuthorizationCriteria = Criteria
				.where(AppUserAuthorizationMongodb.FIELD.STATUS).is(AppUserAuthorizationStatus.OK.getStatusValue())
				.and(AppUserAuthorizationMongodb.FIELD.REFRESH_TOKEN.EXPIRES_AT).lte(LocalDateTime.now());
			Query appUserauthorizationQuery = Query.query(appUserAuthorizationCriteria);

			List<AppUserAuthorizationMongodb> list = mongoTemplate.find(appUserauthorizationQuery, AppUserAuthorizationMongodb.class, MongodbConstants.Collection.APP_USER_AUTHORIZATION);
			list.forEach(appUserAuthorizationMongodb -> {
				try {
					transactionTemplate.executeWithoutResult(transactionStatus -> {
						try {
							//修改状态为已过期
							Query authorizationQuery = Query.query(Criteria
								.where(AppUserAuthorizationMongodb.FIELD.TOKEN_ID).is(appUserAuthorizationMongodb.getTokenId()));

							Update update = new Update();
							update.set(AppUserAuthorizationMongodb.FIELD.STATUS, AppUserAuthorizationStatus.EXPIRED.getStatusValue());
							update.currentDate(AppUserAuthorizationMongodb.FIELD.LOGOUT_TIME);
							update.currentDate(AppUserAuthorizationMongodb.FIELD.UPDATE_TIME);

							mongoTemplate.findAndModify(authorizationQuery, update, AppUserAuthorizationMongodb.class, MongodbConstants.Collection.APP_USER_AUTHORIZATION);
						} catch (Exception e) {
							log.info("修改账号会话状态失败：", e);
							transactionStatus.setRollbackOnly();
						}
					});

					appUserAuthorizationCommonService.removeAuthorizationCache(
						appUserAuthorizationMongodb.getAppId(),
						appUserAuthorizationMongodb.getEndpointId(),
						appUserAuthorizationMongodb.getAccessToken().getTokenValue()
					);
				} catch (Exception e) {
					log.info("修改终端用户会话状态失败：", e);
				}
			});
		} catch (Exception e) {
			log.error("error {}", e.getMessage());
			XxlJobHelper.log(e.getMessage());
			XxlJobHelper.handleFail("modifyAppUserAuthorizationStatusJob fail");
			throw new RuntimeException(e);
		}
		XxlJobHelper.log("modifyAppUserAuthorizationStatusJob end");
	}

	/**
	 * 删除过期会话任务
	 */
	@XxlJob("deleteAppUserAuthorizationJob")
	public void deleteAppUserAuthorizationJob() {
		XxlJobHelper.log("deleteAppUserAuthorizationJob start");
		try {
			Criteria accountauthorizationCriteria = Criteria
				.where(AppUserAuthorizationMongodb.FIELD.STATUS).ne(AppUserAuthorizationStatus.OK.getStatusValue())
				.and(AppUserAuthorizationMongodb.FIELD.REFRESH_TOKEN.EXPIRES_AT).lte(LocalDateTime.now().minusDays(7));
			Query accountauthorizationQuery = Query.query(accountauthorizationCriteria);

			List<AppUserAuthorizationMongodb> removeList = mongoTemplate.findAllAndRemove(accountauthorizationQuery, AppUserAuthorizationMongodb.class, MongodbConstants.Collection.APP_USER_AUTHORIZATION);
			XxlJobHelper.log("删除过期会话：{}条", removeList.size());
			if (!removeList.isEmpty()) {
				mongoTemplate.insert(removeList, MongodbConstants.DeletedCollection.APP_USER_AUTHORIZATION);
			}
		} catch (Exception e) {
			log.error("deleteAppUserAuthorizationJob error {}", e.getMessage());
			XxlJobHelper.log(e.getMessage());
			XxlJobHelper.handleFail("deleteAppUserAuthorizationJob fail");
			throw new RuntimeException(e);
		}
		XxlJobHelper.log("deleteAppUserAuthorizationJob end");
	}
}

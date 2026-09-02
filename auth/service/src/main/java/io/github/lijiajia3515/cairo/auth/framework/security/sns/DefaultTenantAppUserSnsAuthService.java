package io.github.lijiajia3515.cairo.auth.framework.security.sns;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserSnsMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.authentication.SnsCodeFailedException;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsInfo;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsService;
import io.github.lijiajia3515.cairo.auth.framework.sns.exception.SnsAuthenticationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DefaultTenantAppUserSnsAuthService implements TenantAppUserSnsAuthService {
	private final SnsService snsService;
	private final MongoTemplate readMongoTemplate;


	public DefaultTenantAppUserSnsAuthService(SnsService snsService, @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.snsService = snsService;
		this.readMongoTemplate = readMongoTemplate;
	}

	public String getTenantAppUserId(String tenantId, String appId, String snsType, String snsProviderId, String snsCode) {
		try {
			SnsInfo snsInfo;

			try {
				snsInfo = snsService.getSnsInfo(snsType, snsProviderId, snsCode);
			} catch (SnsAuthenticationException e) {
				// 转换为security认证异常
				throw new SnsCodeFailedException(e.getMessage());
			}

			Criteria criteria = Criteria
				.where(TenantAppUserSnsMongodb.FIELD.TENANT_ID).is(tenantId)
				.and(TenantAppUserSnsMongodb.FIELD.APP_ID).is(appId)
				.and(TenantAppUserSnsMongodb.FIELD.SNS_PROVIDER_ID).is(snsProviderId)
				.and(TenantAppUserSnsMongodb.FIELD.OPEN_ID).is(snsInfo.getPartnerOpenId());

			Query query = Query.query(criteria);
			query.fields().include(TenantAppUserSnsMongodb.FIELD.USER_ID);

			TenantAppUserSnsMongodb tenantAppUserSns = readMongoTemplate.findOne(query, TenantAppUserSnsMongodb.class, MongodbConstants.Collection.TENANT_APP_USER_SNS);
			return Optional.ofNullable(tenantAppUserSns).map(TenantAppUserSnsMongodb::getUserId).orElse(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

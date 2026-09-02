package io.github.lijiajia3515.cairo.auth.modules.sns;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.sns.SnsToken;
import io.github.lijiajia3515.cairo.auth.domain.dto.sns.SnsTokenStatus;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SnsTokenMongodb;
import io.github.lijiajia3515.cairo.auth.modules.sns.exception.SnsTokenException;
import io.github.lijiajia3515.cairo.auth.modules.sns.exception.SnsTokenExpiredException;
import io.github.lijiajia3515.cairo.auth.modules.sns.exception.SnsTokenNotFoundException;
import io.github.lijiajia3515.cairo.auth.modules.sns.exception.SnsTokenUsedException;
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
 * [open/api] sns service
 */
@Slf4j
@Validated
@Component
public class SnsCommonService {
	private final TransactionTemplate transactionTemplate;
	private final MongoTemplate writeMongoTemplate;
	private final MongoTemplate readMongoTemplate;


	public SnsCommonService(TransactionTemplate transactionTemplate,
							@Qualifier("mongoTemplate") MongoTemplate writeMongoTemplate,
							@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate) {
		this.transactionTemplate = transactionTemplate;
		this.writeMongoTemplate = writeMongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
	}


	/**
	 * 获取snsToken信息
	 *
	 * @param token snsToken信息
	 * @return SnsToken
	 */
	public SnsToken getSnsToken(String token) throws SnsTokenException {
		Criteria criteria = Criteria.where(SnsTokenMongodb.FIELD.TOKEN).is(token);
		Query query = Query.query(criteria);
		SnsTokenMongodb snsTokenMongodb = readMongoTemplate.findOne(query, SnsTokenMongodb.class, MongodbConstants.Collection.SNS_TOKEN);

		if (snsTokenMongodb == null) {
			return null;
		}

		SnsTokenStatus status = SnsTokenStatus.statusOf(snsTokenMongodb.getStatus()).orElse(SnsTokenStatus.OK);
		if (LocalDateTime.now().isBefore(snsTokenMongodb.getExpiredTime())) {
			status = SnsTokenStatus.EXPIRED;
		}

		return SnsToken.builder()
			.token(snsTokenMongodb.getToken())
			.status(status)
			.expireTime(snsTokenMongodb.getExpiredTime())
			.partnerId(snsTokenMongodb.getPartnerId())
			.providerId(snsTokenMongodb.getProviderId())
			.partnerOpenId(snsTokenMongodb.getPartnerOpenId())
			.providerOpenId(snsTokenMongodb.getProviderOpenId())
			.nickName(snsTokenMongodb.getNickname())
			.avatarUrl(snsTokenMongodb.getAvatarUrl())
			.sex(snsTokenMongodb.getSex())
			.build();
	}


	/**
	 * 验证并返回SnsToken信息
	 *
	 * @param token snsToken信息
	 * @return SnsToken
	 */
	public SnsToken verifySnsToken(String token) throws SnsTokenException {
		Criteria criteria = Criteria.where(SnsTokenMongodb.FIELD.TOKEN).is(token);
		Query query = Query.query(criteria);
		SnsToken snsToken = transactionTemplate.execute(transactionStatus -> {
			SnsTokenMongodb snsTokenMongodb = writeMongoTemplate.findOne(query, SnsTokenMongodb.class, MongodbConstants.Collection.SNS_TOKEN);

			if (snsTokenMongodb == null) {
				return null;
			}

			SnsTokenStatus status = SnsTokenStatus.statusOf(snsTokenMongodb.getStatus()).orElse(SnsTokenStatus.OK);

			if (snsTokenMongodb.getStatus().equals(SnsTokenStatus.OK.getStatus())) {
				Update update = new Update();
				update.currentDate(SnsTokenMongodb.FIELD.METADATA.UPDATE_TIME);
				if (snsTokenMongodb.getExpiredTime() == null || LocalDateTime.now().isBefore(snsTokenMongodb.getExpiredTime())) {
					update.set(SnsTokenMongodb.FIELD.STATUS, SnsTokenStatus.USED.getStatus());
				} else {
					status = SnsTokenStatus.EXPIRED;
					update.set(SnsTokenMongodb.FIELD.STATUS, SnsTokenStatus.EXPIRED.getStatus());
				}
				writeMongoTemplate.updateFirst(query, update, SnsTokenMongodb.class, MongodbConstants.Collection.SNS_TOKEN);
			}
			return SnsToken.builder()
				.token(snsTokenMongodb.getToken())
				.status(status)
				.expireTime(snsTokenMongodb.getExpiredTime())
				.partnerId(snsTokenMongodb.getPartnerId())
				.providerId(snsTokenMongodb.getProviderId())
				.partnerOpenId(snsTokenMongodb.getPartnerOpenId())
				.providerOpenId(snsTokenMongodb.getProviderOpenId())
				.nickName(snsTokenMongodb.getNickname())
				.avatarUrl(snsTokenMongodb.getAvatarUrl())
				.sex(snsTokenMongodb.getSex())
				.build();
		});

		if (snsToken == null) {
			throw new SnsTokenNotFoundException(token);
		}
		if (snsToken.getStatus().equals(SnsTokenStatus.USED)) {
			throw new SnsTokenUsedException(token);
		}
		if (snsToken.getStatus().equals(SnsTokenStatus.EXPIRED)) {
			throw new SnsTokenExpiredException(token, snsToken.getExpireTime());
		}
		return snsToken;
	}
}

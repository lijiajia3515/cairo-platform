package io.github.lijiajia3515.cairo.auth.api.open.sns;

import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SnsTokenMongodb;
import io.github.lijiajia3515.cairo.auth.framework.phone_number_sns.PhoneNumberSnsInfo;
import io.github.lijiajia3515.cairo.auth.framework.phone_number_sns.PhoneNumberSnsService;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsInfo;
import io.github.lijiajia3515.cairo.auth.framework.sns.SnsService;
import io.github.lijiajia3515.cairo.auth.framework.sns.exception.SnsAuthenticationException;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.modules.sns.SnsBusiness;
import io.github.lijiajia3515.cairo.auth.domain.dto.sns.SnsToken;
import io.github.lijiajia3515.cairo.auth.domain.dto.sns.SnsTokenStatus;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
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
public class SnsOpenApiService {

	private final SnsService snsService;
	private final PhoneNumberSnsService phoneNumberSnsService;
	private final MongoTemplate writeMongoTemplate;
	private final TransactionTemplate transactionTemplate;

	public SnsOpenApiService(SnsService snsService, PhoneNumberSnsService phoneNumberSnsService,
							 @Qualifier("mongoTemplate") MongoTemplate writeMongoTemplate,
							 TransactionTemplate transactionTemplate) {
		this.snsService = snsService;
		this.phoneNumberSnsService = phoneNumberSnsService;
		this.writeMongoTemplate = writeMongoTemplate;
		this.transactionTemplate = transactionTemplate;
	}


	/**
	 * 获取联接信息
	 *
	 * @param snsType       联接类型
	 * @param snsProviderId 联接ID
	 * @param snsCode       授权码
	 * @return 联接信息
	 */
	@NewSpan
	@BizLog(
		bizId = "sns:get_sns_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "snsType", value = "#snsType"),
			@BizLog.Param(key = "snsProviderId", value = "#snsProviderId"),
			@BizLog.Param(key = "snsCode", value = "#snsCode")
		}
	)
	public SnsInfo getSnsInfo(@Valid @NotNull String snsType, @Valid @NotNull String snsProviderId, @Valid @NotNull String snsCode) {
		try {
			return snsService.getSnsInfo(snsType, snsProviderId, snsCode);
		} catch (SnsAuthenticationException e) {
			log.warn("获取SNS错误", e);
			throw new ConflictBusinessException(e.getMessage(), SnsBusiness.BAD);
		}
	}

	/**
	 * 获取联接信息
	 *
	 * @param snsType       联接类型
	 * @param snsProviderId 联接ID
	 * @param snsCode       授权码
	 * @return 联接信息
	 */
	@NewSpan
	@BizLog(
		bizId = "sns:get_sns_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "snsType", value = "#snsType"),
			@BizLog.Param(key = "snsProviderId", value = "#snsProviderId"),
			@BizLog.Param(key = "snsCode", value = "#snsCode")
		}
	)
	public SnsToken getSnsToken(@Valid @NotNull String snsType, @Valid @NotNull String snsProviderId, @Valid @NotNull String snsCode) {
		try {
			SnsInfo snsInfo = snsService.getSnsInfo(snsType, snsProviderId, snsCode);
			SnsTokenMongodb tokenMongodb = SnsTokenMongodb.builder()
				.token(CoreConstants.SNOWFLAKE.nextIdStr())
				.status(SnsTokenStatus.OK.getStatus())
				.expiredTime(LocalDateTime.now().plusMinutes(5))
				.partnerId(snsInfo.getPartnerId())
				.providerId(snsInfo.getProviderId())
				.partnerOpenId(snsInfo.getPartnerOpenId())
				.providerOpenId(snsInfo.getProviderOpenId())
				.nickname(snsInfo.getNickName())
				.avatarUrl(snsInfo.getAvatarUrl())
				.sex(snsInfo.getSex())
				.build();
			writeMongoTemplate.insert(tokenMongodb, MongodbConstants.Collection.SNS_TOKEN);

			return SnsToken.builder()
				.token(tokenMongodb.getToken())
				.expireTime(tokenMongodb.getExpiredTime())
				.partnerId(tokenMongodb.getPartnerId())
				.providerId(tokenMongodb.getProviderId())
				.partnerOpenId(tokenMongodb.getPartnerOpenId())
				.providerOpenId(tokenMongodb.getProviderOpenId())
				.nickName(tokenMongodb.getNickname())
				.avatarUrl(snsInfo.getAvatarUrl())
				.sex(snsInfo.getSex())
				.build();
		} catch (Exception e) {
			log.warn("获取snsToken错误", e);
			throw new ConflictBusinessException(e.getMessage(), SnsBusiness.BAD);
		}
	}

	/**
	 * 获取手机号
	 *
	 * @param snsProviderId 联接ID
	 * @param snsCode       授权码
	 * @return 手机号信息
	 */
	@NewSpan
	@BizLog(
		bizId = "sns:get_phone_number",
		scope = "read",
		params = {
			@BizLog.Param(key = "snsProviderId", value = "#snsProviderId"),
			@BizLog.Param(key = "snsCode", value = "#snsCode")
		}
	)
	public PhoneNumberSnsInfo getPhoneNumber(@Valid @NotNull String snsProviderId, @Valid @NotNull String snsCode) {
		try {
			return phoneNumberSnsService.getPhoneNumberInfo(snsProviderId, snsCode);
		} catch (Exception e) {
			log.warn("获取手机号错误", e);
			throw new ConflictBusinessException(e.getMessage(), SnsBusiness.BAD);
		}
	}
}

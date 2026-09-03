package io.github.lijiajia3515.cairo.auth.api.endpoint.account;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.HexUtil;
import com.baomidou.lock.annotation.Lock4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.CairoAuthVerifyCodeConstants;
import io.github.lijiajia3515.cairo.auth.constants.FileKeyPrefixConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountPasswordMongodb;
import io.github.lijiajia3515.cairo.auth.framework.lock4j.AppUserAuthKeyBuilder;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAuthAccountService;
import io.github.lijiajia3515.cairo.auth.framework.security.core.PasswordType;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountCommonService;
import io.github.lijiajia3515.cairo.auth.domain.api.endpoint.account.ModifyMyAccountPasswordArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.endpoint.account.ModifyMyAccountPhoneNumberArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.endpoint.account.ModifyMyAccountUsernameArgs;
import io.github.lijiajia3515.cairo.auth.domain.message.account.ModifiedAccountPasswordMessage;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.modules.captcha.CairoMultipartFile;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.VerifyCodeService;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.VerifyCodeStat;
import io.github.lijiajia3515.cairo.auth.modules.verify_code.VerifyVerifyCodeArgs;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.github.lijiajia3515.cairo.auth.api.client.file.public_file.PublicFileClientApiService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;

import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * [endpoint/api] account service
 */
@Slf4j
@Validated
@Component
public class AccountEndpointApiService {
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final AccountCommonService accountCommonService;
	private final CairoAuthAccountService cairoAuthAccountService;

	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final ObjectMapper objectMapper;

	private final VerifyCodeService verifyCodeService;
	private final PublicFileClientApiService publicFileClientApiService;

	public AccountEndpointApiService(
		@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
		@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
		AccountCommonService accountCommonService,
		TransactionTemplate transactionTemplate, CairoAuthAccountService cairoAuthAccountService,
		RabbitTemplate rabbitTemplate,
		CairoRabbitmqTool cairoRabbitmqTool,
		ObjectMapper objectMapper,
		VerifyCodeService verifyCodeService,
		PublicFileClientApiService publicFileClientApiService) {
		this.cairoAuthAccountService = cairoAuthAccountService;
		this.accountCommonService = accountCommonService;
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.objectMapper = objectMapper;
		this.verifyCodeService = verifyCodeService;
		this.publicFileClientApiService = publicFileClientApiService;
	}

	/**
	 * 修改当前账号用户名
	 *
	 * @param accountId 账号ID
	 * @param args      参数
	 */
	@NewSpan
	@BizLog(
		bizId = "account:modify_my_account_username",
		scope = "write",
		params = {
			@BizLog.Param(key = "accountId", value = "#accountId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@SneakyThrows
	@Lock4j(name = "modify_my_account_username", keyBuilderStrategy = AppUserAuthKeyBuilder.class)
	public void modifyMyAccountUsername(@Valid @NotNull String accountId, @Validated ModifyMyAccountUsernameArgs args) {
		// 验证用户名格式
		if (!AccountCommonService.validUsername(args.getUsername())) {
			throw new ConflictBusinessException("用户名格式错误");
		}

		transactionTemplate.executeWithoutResult(transactionStatus -> {
			try {
				Criteria criteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(accountId);
				Query query = Query.query(criteria);

				Update update = Update.update(AccountMongodb.FIELD.USERNAME, args.getUsername());
				update.set(AccountPasswordMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, accountId);
				update.currentDate(AccountPasswordMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.upsert(query, update, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);

				if (updateResult.getModifiedCount() < 1L) {
					throw new ConflictBusinessException("修改用户名失败");
				}
			} catch (ConflictBusinessException e) {
				transactionStatus.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.info("修改用户名失败", e);
				transactionStatus.setRollbackOnly();
				throw new ConflictBusinessException("修改用户名失败");
			}
		});

		cairoAuthAccountService.removeAccountCache(accountId);
	}

	/**
	 * 修改当前账号手机号
	 *
	 * @param accountId 账号ID
	 * @param args      参数
	 */
	@NewSpan
	@BizLog(
		bizId = "account:modify_my_account_phone_number",
		scope = "write",
		params = {
			@BizLog.Param(key = "accountId", value = "#accountId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@SneakyThrows
	@Lock4j(name = "modify_my_account_phone_number", keyBuilderStrategy = AppUserAuthKeyBuilder.class)
	public void modifyMyAccountPhoneNumber(@Valid @NotNull String accountId, @Validated ModifyMyAccountPhoneNumberArgs args) {
		// 验证手机号格式
		if (!AccountCommonService.validPhoneNumber(args.getPhoneNumber())) {
			throw new ConflictBusinessException("手机号格式错误");
		}

		transactionTemplate.executeWithoutResult(transactionStatus -> {
			try {
				Query query = Query.query(Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(accountId));
				query.fields().include(AccountMongodb.FIELD.PHONE_NUMBER);
				AccountMongodb accountMongodb = mongoTemplate.findOne(query, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
				if (accountMongodb == null) {
					throw new ConflictBusinessException("修改失败");
				}

				// 验证旧手机号
				if (accountMongodb.getPhoneNumber() != null) {
					if (accountMongodb.getPhoneNumber().equals(args.getPhoneNumber())) {
						throw new ConflictBusinessException("手机号相同，无需重复修改");
					}

					VerifyCodeStat oldVerifyCodeStat = verifyCodeService.verify(
						VerifyVerifyCodeArgs.builder()
							.bizCode(CairoAuthVerifyCodeConstants.AUTH)
							.target(accountMongodb.getPhoneNumber())
							.maxFailCount(3)
							.verifyCode(args.getSourceVerifyCode())
							.build()
					);

					if (!VerifyCodeStat.SUCCESS.equals(oldVerifyCodeStat)) {
						throw new ConflictBusinessException("手机号验证码错误");
					}
				}

				Query accountQuery = Query.query(Criteria.where(AccountMongodb.FIELD.PHONE_NUMBER).is(args.getPhoneNumber()));
				boolean exists = mongoTemplate.exists(accountQuery, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
				if (exists) {
					throw new ConflictBusinessException("修改失败(手机号已绑定其他账号)");
				}

				VerifyCodeStat oldVerifyCodeStat = verifyCodeService.verify(
					VerifyVerifyCodeArgs.builder()
						.bizCode(CairoAuthVerifyCodeConstants.AUTH)
						.target(args.getPhoneNumber())
						.maxFailCount(3)
						.verifyCode(args.getVerifyCode())
						.build()
				);

				if (!VerifyCodeStat.SUCCESS.equals(oldVerifyCodeStat)) {
					throw new ConflictBusinessException("新手机号验证码错误");
				}

				Update update = Update.update(AccountMongodb.FIELD.PHONE_NUMBER, args.getPhoneNumber());
				update.set(AccountMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, accountId);
				update.currentDate(AccountMongodb.FIELD.METADATA.UPDATE_TIME);
				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
				log.debug("updateResult: {}", updateResult);
			} catch (BusinessException e) {
				transactionStatus.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyAccountPhoneNumber", e);
				throw new ConflictBusinessException("修改账号手机号失败");
			}
		});


		cairoAuthAccountService.removeAccountCache(accountId);
	}

	/**
	 * 获取帐号是否设置密码
	 *
	 * @param accountId 账号ID
	 */
	@NewSpan
	@BizLog(
		bizId = "account:get_my_account_password_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "accountId", value = "#accountId")
		}
	)
	public boolean getMyAccountPasswordStatus(@Valid @NotNull String accountId) {
		Criteria accountPasswordCriteria = Criteria
			.where(AccountPasswordMongodb.FIELD.ACCOUNT_ID).is(accountId)
			.and(AccountPasswordMongodb.FIELD.TYPE).is(PasswordType.PASSWORD.getType());
		Query accountPasswordQuery = Query.query(accountPasswordCriteria);
		return readMongoTemplate.exists(accountPasswordQuery, AccountPasswordMongodb.class, MongodbConstants.Collection.ACCOUNT_PASSWORD);
	}

	/**
	 * 修改当前用户密码
	 *
	 * @param accountId 账号ID
	 * @param args      args
	 */
	@NewSpan
	@BizLog(
		bizId = "account:modify_my_account_password",
		scope = "write",
		params = {
			@BizLog.Param(key = "accountId", value = "#accountId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	@SneakyThrows
	@Lock4j(name = "modify_my_account_password", keyBuilderStrategy = AppUserAuthKeyBuilder.class)
	public void modifyMyAccountPassword(@Valid @NotNull String accountId, @Validated ModifyMyAccountPasswordArgs args) {
		String encodeNewPassword = accountCommonService.getPasswordEncoder().encode(args.getNewPassword());
		transactionTemplate.executeWithoutResult(transactionStatus -> {
			try {
				Criteria criteria = Criteria
					.where(AccountPasswordMongodb.FIELD.ACCOUNT_ID).is(accountId)
					.and(AccountPasswordMongodb.FIELD.TYPE).is(PasswordType.PASSWORD.getType());
				Query query = Query.query(criteria);
				String oldEncoderPassword = Optional.ofNullable(mongoTemplate.findOne(query, AccountPasswordMongodb.class, MongodbConstants.Collection.ACCOUNT_PASSWORD))
					.map(AccountPasswordMongodb::getPassword).orElse(null);
				boolean match = false;
				if (oldEncoderPassword == null) {
					match = true;
				} else {
					if (args.getPassword() != null) {
						match = accountCommonService.getPasswordEncoder().matches(args.getPassword(), oldEncoderPassword);
					}
				}

				if (!match) {
					throw new ConflictBusinessException("密码错误");
				}

				Update update = Update.update(AccountPasswordMongodb.FIELD.PASSWORD, encodeNewPassword);
				update.set(AccountPasswordMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getAccountId());
				update.currentDate(AccountPasswordMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.upsert(query, update, AccountPasswordMongodb.class, MongodbConstants.Collection.ACCOUNT_PASSWORD);

				if (updateResult.getModifiedCount() < 1L && updateResult.getUpsertedId() == null) {
					throw new ConflictBusinessException("修改密码失败");
				}

			} catch (ConflictBusinessException e) {
				transactionStatus.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.info("密码修改失败", e);
				transactionStatus.setRollbackOnly();
				throw new ConflictBusinessException("修改密码失败");
			}
		});

		// 发送密码变更通知
		rabbitTemplate.convertAndSend(
			cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
			cairoRabbitmqTool.getRouteKey().getKey(CairoAuthRabbitmqRouteKey.MODIFIED_ACCOUNT_PASSWORD),
			objectMapper.writeValueAsString(ModifiedAccountPasswordMessage.builder()
				.accountId(accountId)
				.eventAccountId(accountId)
				.eventTime(LocalDateTime.now())
				.build()
			),
			new CorrelationData(CoreConstants.nextIdStr())
		);
	}


	/**
	 * 修改当前账号头像(直接修改模式)
	 *
	 * @param accountId         账号ID
	 * @param avatarInputStream 头像二进制流
	 */
	@NewSpan
	@BizLog(
		bizId = "account:modify_my_account_avatar",
		scope = "write",
		params = {
			@BizLog.Param(key = "accountId", value = "#accountId"),
			@BizLog.Param(key = "appId", value = "#appId"),
		}
	)
	@SneakyThrows
	@Lock4j(name = "modify_my_account_avatar", keyBuilderStrategy = AppUserAuthKeyBuilder.class)
	public void modifyMyAccountAvatar(@Valid @NotNull String accountId, InputStream avatarInputStream, String contentType, Long contentLength) {
		DataSize dataSize = DataSize.ofBytes(contentLength);
//		// 头像小于10KB
//		if (dataSize.compareTo(DataSize.ofKilobytes(10)) < 0) {
//			throw new ConflictBusinessException("头像过小请重新上传");
//		}

		// 头像大于2MB
		if (dataSize.compareTo(DataSize.ofMegabytes(2)) > 0) {
			throw new ConflictBusinessException("头像过大请重新上传");
		}

		byte[] bytes = IoUtil.readBytes(avatarInputStream);
		// 获取文件头部二进制
		byte[] headBytes = ArrayUtil.sub(bytes, 0, 28);
		// 文件头转16二进制
		String hexHead = HexUtil.encodeHexStr(headBytes, false);
		// 文件类型
		String fileType = FileTypeUtil.getType(hexHead);
		String filename = accountId + "." + fileType;
		String path = FileKeyPrefixConstants.AVATAR_PREFIX + filename;
		CairoMultipartFile avatarMultipartFile = new CairoMultipartFile(filename, contentType, bytes);

		List<String> avatarUrls = publicFileClientApiService.uploadFile(path, avatarMultipartFile);
		String avatarUrl = Optional.ofNullable(avatarUrls).filter(z -> z.size() == 3).map(x -> x.get(2)).orElse(null);

		Criteria criteria = Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(accountId);
		Query query = Query.query(criteria);
		Update update = new Update();
		Optional.ofNullable(avatarUrl).ifPresent(x -> update.set(AccountMongodb.FIELD.AVATAR_URL, x));

		update.set(AccountMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, accountId);
		update.currentDate(AccountMongodb.FIELD.METADATA.UPDATE_TIME);

		transactionTemplate.execute(status -> {
			try {
				AccountMongodb account = mongoTemplate.findAndModify(query, update, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
				if (account == null) {
					throw new ConflictBusinessException("账号不存在");
				}
				return account;
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyMyAccountAvatarUrl", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改头像失败");
			}
		});

		cairoAuthAccountService.removeAccountCache(accountId);
	}

}

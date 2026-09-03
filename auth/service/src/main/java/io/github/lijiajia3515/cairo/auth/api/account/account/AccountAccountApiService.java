package io.github.lijiajia3515.cairo.auth.api.account.account;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.HexUtil;
import com.baomidou.lock.annotation.Lock4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqExchange;
import io.github.lijiajia3515.cairo.auth.CairoAuthRabbitmqRouteKey;
import io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants;
import io.github.lijiajia3515.cairo.auth.constants.FileKeyPrefixConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.account.account.ModifyAccountPasswordArgs;
import io.github.lijiajia3515.cairo.auth.domain.message.account.LogoffAccountMessage;
import io.github.lijiajia3515.cairo.auth.domain.message.account.ModifiedAccountPasswordMessage;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountPasswordMongodb;
import io.github.lijiajia3515.cairo.auth.framework.lock4j.AccountAuthKeyBuilder;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.account.CairoAuthAccountService;
import io.github.lijiajia3515.cairo.auth.framework.security.core.PasswordType;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountCommonService;
import io.github.lijiajia3515.cairo.auth.modules.account.AccountLogoffStatus;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.modules.captcha.CairoMultipartFile;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.rabbitmq.CairoRabbitmqTool;
import io.github.lijiajia3515.cairo.auth.api.client.file.public_file.PublicFileClientApiService;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * [account/api] account service
 */
@Slf4j
@Validated
@Component
public class AccountAccountApiService {

	/**
	 * 默认密码
	 */
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final RedisTemplate<String, Object> redisTemplate;
	private final RabbitTemplate rabbitTemplate;
	private final CairoRabbitmqTool cairoRabbitmqTool;
	private final ObjectMapper objectMapper;
	private final CairoAuthAccountService cairoAuthAccountService;
	private final AccountCommonService accountCommonService;

	private final PublicFileClientApiService publicFileClientApiService;

	public AccountAccountApiService(
		@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
		RedisTemplate<String, Object> redisTemplate,
		AccountCommonService accountCommonService,
		TransactionTemplate transactionTemplate,
		RabbitTemplate rabbitTemplate,
		CairoRabbitmqTool cairoRabbitmqTool,
		ObjectMapper objectMapper, CairoAuthAccountService cairoAuthAccountService,
		PublicFileClientApiService publicFileClientApiService) {
		this.redisTemplate = redisTemplate;
		this.accountCommonService = accountCommonService;
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.rabbitTemplate = rabbitTemplate;
		this.cairoRabbitmqTool = cairoRabbitmqTool;
		this.objectMapper = objectMapper;
		this.cairoAuthAccountService = cairoAuthAccountService;
		this.publicFileClientApiService = publicFileClientApiService;
	}


	/**
	 * 修改密码
	 *
	 * @param accountId 账号ID
	 * @param args      参数
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
	@Lock4j(name = "modify_my_account_password", keyBuilderStrategy = AccountAuthKeyBuilder.class)
	public void modifyMyAccountPassword(@Valid @NotNull String accountId, @Validated ModifyAccountPasswordArgs args) {
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
				log.info("修改密码失败", e);
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
	 * 修改我的账户头像
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
			@BizLog.Param(key = "avatarInputStream", value = "'****'"),
			@BizLog.Param(key = "contentType", value = "#contentType"),
			@BizLog.Param(key = "contentLength", value = "#contentLength"),
		}
	)
	@SneakyThrows
	@Lock4j(name = "modify_my_account_avatar", keyBuilderStrategy = AccountAuthKeyBuilder.class)
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
		update.set(AccountPasswordMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, accountId);
		update.currentDate(AccountPasswordMongodb.FIELD.METADATA.UPDATE_TIME);

		transactionTemplate.executeWithoutResult(status -> {
			try {
				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
				if (updateResult.getModifiedCount() < 1) {
					throw new ConflictBusinessException("头像修改失败");
				}
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyAccountAvatarUrl", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("头像修改失败");
			}
		});
		cairoAuthAccountService.removeAccountCache(accountId);
	}

	/**
	 * 注销我的账号
	 *
	 * @param accountId 账号ID
	 */
	@NewSpan
	@BizLog(
		bizId = "account:logoff_my_account",
		scope = "write",
		params = {
			@BizLog.Param(key = "accountId", value = "#accountId")
		}
	)
	@SneakyThrows
	@Lock4j(name = "logoff_my_account", keyBuilderStrategy = AccountAuthKeyBuilder.class)
	public void logoffMyAccount(@Valid @NotNull String accountId) {
		AccountMongodb logoffAccountMongodb = transactionTemplate.execute(transactionStatus -> {
			try {
				Query accountQuery = Query.query(Criteria.where(AccountMongodb.FIELD.ACCOUNT_ID).is(accountId));

				Update accountUpdate = Update.update(AccountMongodb.FIELD.LOGOFF_STATUS, AccountLogoffStatus.PENDING.getLogoffStatusValue());
				accountUpdate.set(AccountMongodb.FIELD.LOGOFF_PENDING_TIME, LocalDateTime.now().plus(CairoAuthConstants.ACCOUNT_LOGOFF_PENDING_TIME));
				accountUpdate.set(AccountMongodb.FIELD.METADATA.UPDATE_ACCOUNT_ID, CairoSecurityContextHolder.getAccountId());
				accountUpdate.currentDate(AccountMongodb.FIELD.METADATA.UPDATE_TIME);
				FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
				AccountMongodb modifiedAccountMongodb = mongoTemplate.findAndModify(accountQuery, accountUpdate, options, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
				if (modifiedAccountMongodb == null) {
					throw new ConflictBusinessException("注销账号失败，账号不存在");
				}
				return modifiedAccountMongodb;
			} catch (ConflictBusinessException e) {
				transactionStatus.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("logoffMyAccount", e);
				transactionStatus.setRollbackOnly();
				throw new ConflictBusinessException("注销账号失败");
			}
		});

		if (logoffAccountMongodb != null) {
			// remove cache
			cairoAuthAccountService.removeAccountCache(accountId);
			// 发送注销账号消息
			rabbitTemplate.convertAndSend(
				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
				cairoRabbitmqTool.getRouteKey().getKey(CairoAuthRabbitmqRouteKey.LOGOFF_ACCOUNT),
				objectMapper.writeValueAsString(LogoffAccountMessage.builder()
					.accountId(logoffAccountMongodb.getAccountId())
					.nickname(logoffAccountMongodb.getNickname())
					.avatarUrl(logoffAccountMongodb.getAvatarUrl())
					.phoneNumber(logoffAccountMongodb.getPhoneNumber())
					.email(logoffAccountMongodb.getEmail())
					.username(logoffAccountMongodb.getUsername())
					.joinTime(logoffAccountMongodb.getJoinTime())
					.eventAccountId(CairoSecurityContextHolder.getAccountId())
					.eventTime(LocalDateTime.now())
					.build()
				),
				new CorrelationData(CoreConstants.nextIdStr())
			);
		}
	}
}

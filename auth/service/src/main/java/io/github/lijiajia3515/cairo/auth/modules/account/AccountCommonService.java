package io.github.lijiajia3515.cairo.auth.modules.account;

import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpUtil;
import io.github.lijiajia3515.cairo.auth.constants.FileKeyPrefixConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.account.Account;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AccountMongodb;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.modules.captcha.CairoMultipartFile;
import io.github.lijiajia3515.cairo.mongodb.serial.SerialService;
import io.github.lijiajia3515.cairo.auth.api.client.file.public_file.PublicFileClientApiService;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static cn.hutool.core.util.RandomUtil.BASE_CHAR_NUMBER;

/**
 * account common service
 */
@Slf4j
@Validated
@Component
public class AccountCommonService {
	public static final String USERNAME_REGEX_STR = "^[a-zA-Z\\u4e00-\\u9fa5][a-zA-Z0-9_\\u4e00-\\u9fa5]{4,25}$";
	public static final String PHONE_NUMBER_REGEX_STR = "^1[3456789]\\d{9}$";

	private static final String SERIAL_NAMESPACE = "default";
	private static final String SERIAL_ACCOUNT = "account";
	private static final String ACCOUNT = "A";

	private final SerialService serialService;

	private final PasswordEncoder passwordEncoder;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final MongoTemplate writeMongoTemplate;
	private final PublicFileClientApiService publicFileClientApiService;

	/**
	 * 密码生成器
	 */
	RandomGenerator passwordRandomGenerator = new RandomGenerator(BASE_CHAR_NUMBER, 8);

	public AccountCommonService(SerialService serialService,
								PasswordEncoder passwordEncoder,
								@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
								TransactionTemplate transactionTemplate,
								@Qualifier("mongoTemplate") MongoTemplate writeMongoTemplate,
								PublicFileClientApiService publicFileClientApiService) {
		this.serialService = serialService;
		this.passwordEncoder = passwordEncoder;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.writeMongoTemplate = writeMongoTemplate;
		this.publicFileClientApiService = publicFileClientApiService;
	}

	/**
	 * get account list by account ids
	 *
	 * @param accountIds accountIds
	 * @return user list
	 */
	@NewSpan
	public List<Account> getAccountListByAccountIds(Collection<String> accountIds) {
		if (accountIds == null || accountIds.isEmpty()) {
			return Collections.emptyList();
		}

		Criteria criteria = Criteria
			.where(AccountMongodb.FIELD.ACCOUNT_ID).in(accountIds);

		Query query = Query.query(criteria);
		query.with(Sort.by(Sort.Order.asc(AccountMongodb.FIELD.NICKNAME)));
		List<AccountMongodb> users = readMongoTemplate.find(query, AccountMongodb.class, MongodbConstants.Collection.ACCOUNT);
		return users.stream().map(this::convert).collect(Collectors.toList());
	}


	@NewSpan
	public Account getAccount(String accountId) {
		return getAccountMapByAccountIds(Collections.singleton(accountId)).get(accountId);
	}

	/**
	 * get account map by accountIds
	 *
	 * @param accountIds accountIds
	 * @return user map
	 */
	@NewSpan
	public Map<String, Account> getAccountMapByAccountIds(Collection<String> accountIds) {
		return getAccountListByAccountIds(accountIds).stream()
			.collect(Collectors.toMap(Account::getAccountId, x -> x, (x1, x2) -> x1));
	}

	public Account convert(AccountMongodb accountMongodb) {
		return Account.builder()
			.accountId(accountMongodb.getAccountId())
			.avatarUrl(accountMongodb.getAvatarUrl())
			.nickname(accountMongodb.getNickname())
			.joinTime(accountMongodb.getMetadata().getCreateTime())
			.build();
	}

	/**
	 * 验证用户名格式
	 *
	 * @param username username
	 * @return 格式是否正确
	 */
	public static boolean validUsername(String username) {
		return ReUtil.isMatch(USERNAME_REGEX_STR, username);
	}

	/**
	 * 手机号格式是否正确
	 *
	 * @param phoneNumber phoneNumber
	 * @return 格式是否正确
	 */
	public static boolean validPhoneNumber(String phoneNumber) {
		return ReUtil.isMatch(PHONE_NUMBER_REGEX_STR, phoneNumber);
	}

	@NewSpan
	public String getNewAccountId() {
		return ACCOUNT + serialService.next(SERIAL_NAMESPACE, SERIAL_ACCOUNT);
	}


	/**
	 * 生成新密码
	 *
	 * @return 密码
	 */
	@NewSpan
	public String getNewPassword() {
		return passwordRandomGenerator.generate();
	}

	/**
	 * 生成新密码和密文
	 *
	 * @return 密码
	 */
	@NewSpan
	public List<String> getNewPasswordPackage() {
		String rawPassword = passwordRandomGenerator.generate();
		String encodePassword = passwordEncoder.encode(rawPassword);
		return List.of(rawPassword, encodePassword);
	}

	/**
	 * 上传用户头像根据网络地址
	 *
	 * @param accountId 账号ID
	 * @param avatarUrl 网络地址
	 * @return 返回格式化的账号头像地址
	 */
	@NewSpan
	@BizLog(
		bizId = "account:modify_my_account_avatar_url",
		scope = "write",
		params = {
			@BizLog.Param(key = "accountId", value = "#accountId"),
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public String uploadAccountAvatar(@Valid @NotNull String accountId, String avatarUrl) {
		try {
			byte[] bytes = HttpUtil.createGet(avatarUrl).execute().bodyBytes();
			byte[] headBytes = ArrayUtil.sub(bytes, 0, 28);
			// 文件头转16二进制
			String hexHead = HexUtil.encodeHexStr(headBytes, false);
			// 文件类型
			String fileType = FileTypeUtil.getType(hexHead);
			String filename = accountId + "." + fileType;
			String path = FileKeyPrefixConstants.AVATAR_PREFIX + filename;
			CairoMultipartFile avatarMultipartFile = new CairoMultipartFile(filename, bytes);

			List<String> avatarUrls = publicFileClientApiService.uploadFile(path, avatarMultipartFile);
			return Optional.ofNullable(avatarUrls).filter(z -> z.size() == 3).map(x -> x.get(2)).orElse(null);
		} catch (Exception e) {
			log.warn("头像上传失败", e);
			return null;
		}
	}


	/**
	 * 获取密码编译器
	 *
	 * @return 密码编译器
	 */
	public PasswordEncoder getPasswordEncoder() {
		return passwordEncoder;
	}

}

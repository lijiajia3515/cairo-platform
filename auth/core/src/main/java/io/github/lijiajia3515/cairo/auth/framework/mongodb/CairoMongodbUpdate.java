package io.github.lijiajia3515.cairo.auth.framework.mongodb;


import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractAccountMetadataMongodbField;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractAppUserMetadataMongodbField;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.field.AbstractTenantAppUserMetadataMongodbField;
import io.github.lijiajia3515.cairo.mongodb.domain.field.AbstractNoneMetadataMongodbField;
import org.springframework.data.mongodb.core.query.Update;

import java.time.LocalDateTime;

public class CairoMongodbUpdate {

	public static Update update(AbstractNoneMetadataMongodbField mongodbField, Update update) {
		return update.set(mongodbField.METADATA.UPDATE_TIME, LocalDateTime.now());
	}


	/**
	 * 账号 metadata 更新
	 *
	 * @param mongodbField    mongodbField
	 * @param updateAccountId updateAccountId
	 * @param update          mongodbUpdate
	 * @return mongoUpdate
	 */
	public static Update accountUpdate(AbstractAccountMetadataMongodbField mongodbField, String updateAccountId, Update update) {
		return update.set(mongodbField.METADATA.UPDATE_ACCOUNT_ID, updateAccountId)
			.set(mongodbField.METADATA.UPDATE_TIME, LocalDateTime.now());
	}

	/**
	 * 用户 metadata 更新
	 *
	 * @param mongodbField mongodbField
	 * @param updateUserId updateUserId
	 * @param update       mongodbUpdate
	 * @return mongoUpdate
	 */
	public static Update userUpdate(AbstractAppUserMetadataMongodbField mongodbField, String updateUserId, Update update) {
		return update.set(mongodbField.METADATA.UPDATE_USER_ID, updateUserId)
			.set(mongodbField.METADATA.UPDATE_TIME, LocalDateTime.now());
	}

	/**
	 * 企业 metadata 更新
	 *
	 * @param mongodbField mongodbField
	 * @param updateUserId updateUserId
	 * @param update       mongodbUpdate
	 * @return mongoUpdate
	 */
	public static Update tenantUpdate(AbstractTenantAppUserMetadataMongodbField mongodbField, String updateUserId, Update update) {
		return update.set(mongodbField.METADATA.UPDATE_USER_ID, updateUserId)
			.set(mongodbField.METADATA.UPDATE_TIME, LocalDateTime.now());
	}

}

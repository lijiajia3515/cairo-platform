package io.github.lijiajia3515.cairo.auth.domain.api.client.account;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * 查询账号（单个）
 */
@Data
@EqualsAndHashCode(callSuper = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchAccountArgs extends AbstractPage<SearchAccountArgs> {
	/**
	 * 类型
	 */
	private String type;
	/**
	 * 账号ID
	 */
	private String accountId;
	/**
	 * 手机号
	 */
	private String phoneNumber;

	/**
	 * 用户名
	 */
	private String username;

	/**
	 * 邮箱
	 */
	private String email;

	public enum Type {
		ACCOUNT_ID("account_id"),
		USERNAME("username"),
		PHONE_NUMBER("phone_number"),
		EMAIL("email"),
		;
		/**
		 * 类型值
		 */
		@Getter
		private final String typeValue;

		Type(String typeValue) {
			this.typeValue = typeValue;
		}
		public static Optional<Type> ofTypeValue(String typeValue){
			return Arrays.stream(Type.values()).filter(x->x.typeValue.equals(typeValue)).findFirst();
		}
	}
}

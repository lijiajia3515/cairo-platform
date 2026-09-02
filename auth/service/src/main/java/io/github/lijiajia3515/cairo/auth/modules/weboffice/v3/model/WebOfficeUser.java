package io.github.lijiajia3515.cairo.auth.modules.weboffice.v3.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebOfficeUser implements Serializable {

	/**
	 * 当前用户 ID
	 */
	@JsonProperty("id")
	private String id;

	/**
	 * 用户昵称
	 */
	@JsonProperty("name")
	private String name;

	/**
	 * 用户头像 URL
	 */
	@JsonProperty("avatar_url")
	private String avatarUrl;
}

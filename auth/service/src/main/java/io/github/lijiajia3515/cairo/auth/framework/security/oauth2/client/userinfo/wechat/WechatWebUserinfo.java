package io.github.lijiajia3515.cairo.auth.framework.security.oauth2.client.userinfo.wechat;

import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.client.userinfo.OAuthUserinfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WechatWebUserinfo implements OAuthUserinfo, Serializable {
	private String openid;
	private String unionid;
	private String nickname;
	private String sex;
	private String language;
	private String city;
	private String province;
	private String country;
	private String headimgurl;
	private List<Object> privilege;

	@Override
	public String subject() {
		return unionid;
	}
}

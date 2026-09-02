package io.github.lijiajia3515.cairo.auth.api.client.wxmp.provider;

import io.github.lijiajia3515.cairo.auth.framework.sns.exception.SnsAuthenticationCodeFailedException;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.client.provider.WxmpOpenIdInfo;
import io.github.lijiajia3515.cairo.auth.domain.api.client.provider.WxmpProviderInfo;
import io.github.lijiajia3515.cairo.auth.domain.api.client.provider.WxmpJsApiTicket;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.wxmp.WxmpProviderMongodb;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.provider.GetWxmpJsApiTicketArgs;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.provider.GetWxmpProviderAuthArgs;
import io.github.lijiajia3515.cairo.auth.modules.wxmp.provider.GetWxmpProviderInfoArgs;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.enums.TicketType;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;


/**
 * [client/api] WxmpProvider service
 */
@Slf4j
@Validated
@Component
public class WxmpProviderClientApiService {

	private final WxMpService wxMpService;
	private final MongoTemplate readMongoTemplate;



	public WxmpProviderClientApiService(WxMpService wxMpService,
										MongoTemplate readMongoTemplate) {
		this.wxMpService = wxMpService;
		this.readMongoTemplate = readMongoTemplate;
	}

	@NewSpan
	@BizLog(
		bizId = "wxmp_provider:get_provider_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public WxmpProviderInfo getProviderInfo(GetWxmpProviderInfoArgs args) {
		try {
			Criteria criteria = Criteria.where(WxmpProviderMongodb.FIELD.WX_MP_PROVIDER_ID).is(args.getWxmpProviderId());
			WxmpProviderMongodb wxmpProviderMongodb = readMongoTemplate.findOne(Query.query(criteria), WxmpProviderMongodb.class, MongodbConstants.Collection.WXMP_PROVIDER);

			if (wxmpProviderMongodb == null) {
				log.error("wxmpProviderMongodb不存在{}", wxmpProviderMongodb);
				throw new ConflictBusinessException("微信公众号providerId不存在");
			}

			return WxmpProviderInfo.builder()
				.providerId(wxmpProviderMongodb.getWxmpProviderId())
				.providerName(wxmpProviderMongodb.getWxmpProviderName())
				.wxmpAppId(wxmpProviderMongodb.getWxmpAppId())
				.wxmpSecret(wxmpProviderMongodb.getWxmpSecret())
				.wxmpToken(wxmpProviderMongodb.getWxmpToken())
				.wxmpAesKey(wxmpProviderMongodb.getWxmpAesKey())
				.enabled(wxmpProviderMongodb.isEnabled())
				.build();
		} catch (Exception e) {
			log.warn("微信公众号providerId错误", e);
			throw new ConflictBusinessException("微信公众号providerId错误");
		}
	}

	@NewSpan
	@BizLog(
		bizId = "wxmp_provider:get_wxmp_openid",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public WxmpOpenIdInfo getWxmpOpenid(GetWxmpProviderAuthArgs args) {
		try {
			WxOAuth2AccessToken accessToken = wxMpService.switchoverTo(args.getWxmpProviderId()).getOAuth2Service().getAccessToken(args.getCode());

			if (accessToken == null || accessToken.getOpenId() == null) {
				throw new SnsAuthenticationCodeFailedException("微信公众号读取用户信息失败");
			}


			return WxmpOpenIdInfo.builder()
				.providerId(args.getWxmpProviderId())
				.appId("wxmp")
				.openId(accessToken.getOpenId())
				.build();
		} catch (Exception e) {
			log.warn("微信异常", e);
			throw new SnsAuthenticationCodeFailedException("微信公众号授权码错误");
		}
	}

	@NewSpan
	@BizLog(
		bizId = "wxmp_provider:js_api_ticket",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public WxmpJsApiTicket jsApiTicket(GetWxmpJsApiTicketArgs args) {
		WxmpJsApiTicket.WxmpJsApiTicketBuilder builder = WxmpJsApiTicket.builder();

		Criteria criteria = Criteria.where(WxmpProviderMongodb.FIELD.WX_MP_PROVIDER_ID).is(args.getWxmpProviderId());
		WxmpProviderMongodb wxmpProviderMongodb = readMongoTemplate.findOne(Query.query(criteria), WxmpProviderMongodb.class, MongodbConstants.Collection.WXMP_PROVIDER);
		if (wxmpProviderMongodb == null || wxmpProviderMongodb.getWxmpAppId() == null) {
			log.error("wxmpAppId不存在{}", wxmpProviderMongodb);
			throw new ConflictBusinessException("微信公众号appId不存在");
		}
		builder.appId(wxmpProviderMongodb.getWxmpAppId());
		try {
			String ticket = wxMpService.switchoverTo(args.getWxmpProviderId()).getTicket(TicketType.JSAPI);
			builder.jsapiTicket(ticket);
		} catch (WxErrorException e) {
			log.warn("微信公众号获取jsApiTicket失败", e);
			throw new ConflictBusinessException("微信公众号获取jsApiTicket失败");
		}
		return builder.build();
	}
}

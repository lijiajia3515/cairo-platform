package io.github.lijiajia3515.cairo.auth.api.client.wxmp.mass_msg.client;

import cn.hutool.http.HttpUtil;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.auth.modules.utils.FilesUtil;
import io.github.lijiajia3515.cairo.auth.domain.api.client.mass.msg.DeleteWxmpMassMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.mass.msg.SendWxmpMassMsgArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.mass.msg.WxmpMassMsgResult;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.WxMpMassTagMessage;
import me.chanjar.weixin.mp.bean.draft.WxMpAddDraft;
import me.chanjar.weixin.mp.bean.draft.WxMpDraftArticles;
import me.chanjar.weixin.mp.bean.material.WxMpMaterial;
import me.chanjar.weixin.mp.bean.material.WxMpMaterialUploadResult;
import me.chanjar.weixin.mp.bean.result.WxMpMassSendResult;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * [client/api] wxmpMassMsg service
 * 微信推文群发
 */

@Slf4j
@Validated
@Component
public class WxmpMassMsgClientApiService {

	private final WxMpService wxMpService;
	private final String TEMPORARY_PATH = "/temporary";

	public WxmpMassMsgClientApiService(WxMpService wxMpService) {
		this.wxMpService = wxMpService;
	}

	/**
	 * 公众号群发 发送
	 *
	 * @param args 参数
	 * @return WxmpMassMsgResult
	 */
	@NewSpan
	@BizLog(
		bizId = "wxmp_mass_msg:send_wxmp_mass_msg",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public WxmpMassMsgResult sendWxmpMassMsg(SendWxmpMassMsgArgs args) {
		//获取草稿
		WxmpMassMsgResult result = draft(args);
		/*
		 * 群发草稿消息
		 * */
		WxMpMassTagMessage wxMpMassTagMessage = new WxMpMassTagMessage();
		wxMpMassTagMessage.setSendAll(true);
		wxMpMassTagMessage.setSendIgnoreReprint(true);
		wxMpMassTagMessage.setMediaId(result.getDraftMediaId());
		wxMpMassTagMessage.setMsgType(WxConsts.MassMsgType.MPNEWS);
		try {
			WxMpMassSendResult massSendResult = wxMpService.getMassMessageService().massGroupMessageSend(wxMpMassTagMessage);
			result.setMsgId(massSendResult.getMsgId());
		} catch (WxErrorException e) {
			log.info("massGroupMessageSend error {}", e.getMessage());
		}
		return result;
	}

	@NewSpan
	@BizLog(
		bizId = "wxmp_mass_msg:upload_wx_material",
		scope = "write",
		params = {
			@BizLog.Param(key = "url", value = "#url")
		}
	)
	//上传永久素材（占用容量）
	private WxMpMaterialUploadResult wxMpMaterialUpload(String url) {
		String fileName =  TEMPORARY_PATH+"/"+ CoreConstants.nextIdStr() + FilesUtil.getType(url);
		File file = HttpUtil.downloadFileFromUrl(url, fileName);
		//永久图片 ID
		WxMpMaterial wxMpMaterial = new WxMpMaterial("img", file, "图片", "图片");
		WxMpMaterialUploadResult result = null;
		try {
			result = wxMpService.getMaterialService().materialFileUpload(WxConsts.MediaFileType.IMAGE, wxMpMaterial);
		} catch (WxErrorException e) {
			log.info("materialFileUpload error {}", e.getMessage());
		}
		boolean delete = file.delete();
		if (!delete) {
			log.info("file {} not delete", file.getAbsolutePath());
		}
		return result;
	}

	private WxmpMassMsgResult draft(SendWxmpMassMsgArgs args) {
		//封面图片转换
		WxMpMaterialUploadResult mpMaterialUploadResult = wxMpMaterialUpload(args.getCoverUrl());
		String coverMediaId = mpMaterialUploadResult.getMediaId();

		String content = args.getContent();
		String img;
		Pattern p_image;
		Matcher m_image;
		String regEx_img = "<img.*src\\s*=\\s*(.*?)[^>]*?>";
		p_image = Pattern.compile
			(regEx_img, Pattern.CASE_INSENSITIVE);
		m_image = p_image.matcher(content);
		/*
		 * 替换url
		 * */
		List<String> contentMediaIds = new ArrayList<>();
		while (m_image.find()) {
			// 得到<img />数据
			img = m_image.group();
			// 匹配<img>中的src数据
			Matcher matcher = Pattern.compile("src\\s*=\\s*\"?(.*?)(\"|>|\\s+)").matcher(img);
			while (matcher.find()) {
				String substring = matcher.group().substring(5, matcher.group().length() - 1);
				WxMpMaterialUploadResult result = wxMpMaterialUpload(substring);
				content = content.replaceAll(substring, result.getUrl());
				contentMediaIds.add(result.getMediaId());
			}
		}
		WxmpMassMsgResult.WxmpMassMsgResultBuilder builder = WxmpMassMsgResult.builder()
			.coverMediaId(coverMediaId);

		WxMpDraftArticles draftArticles = WxMpDraftArticles.builder()
			.content(content)
			.thumbMediaId(coverMediaId)
			.title(args.getTitle())
			.build();
		WxMpAddDraft addDraft = WxMpAddDraft.fromJson(WxMpAddDraft.builder().articles(Collections.singletonList(draftArticles)).build().toJson());

		String draft = null;
		try {
			draft = wxMpService.getDraftService().addDraft(addDraft);
		} catch (WxErrorException e) {
			log.info("addDraft error {}", e.getMessage());
		}
		builder.draftMediaId(draft)
			.contentMediaId(contentMediaIds);
		return builder.build();
	}

	/**
	 * 公众号群发 删除
	 *
	 * @param args 参数
	 */

	@NewSpan
	@BizLog(
		bizId = "wxmp_mass_msg:delete_wxmp_mass_msg",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void deleteWxmpMassMsg(DeleteWxmpMassMsgArgs args) {
		//删除群发
		try {
			wxMpService.getMassMessageService().delete(Long.valueOf(args.getMsgId()), 0);
		} catch (WxErrorException e) {
			log.info("Mass delete  error {}", e.getMessage());
		}
		//删除封面素材
		try {
			wxMpService.getMaterialService().materialDelete(args.getCoverMediaId());
		} catch (WxErrorException e) {
			log.info("material delete cover error {}", e.getMessage());
		}
		//删除内容素材
		args.getContentMediaId().forEach(x -> {
			try {
				wxMpService.getMaterialService().materialDelete(x);
			} catch (WxErrorException e) {
				log.info("material delete content  error {}", e.getMessage());
			}
		});
	}
}


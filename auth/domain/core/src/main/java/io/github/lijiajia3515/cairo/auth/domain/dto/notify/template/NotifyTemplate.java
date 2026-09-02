package io.github.lijiajia3515.cairo.auth.domain.dto.notify.template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通知消息模板
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotifyTemplate {

	/**
	 * 模板ID
	 */
	private String msgId;

	/**
	 * 业务ID
	 */
	private String templateId;

	/**
	 * 模板名称
	 */
	private String templateName;

	/**
	 * 分类ID
	 */
	private String categoryId;

	/**
	 * 分类名称
	 */
	private String categoryName;

	/**
	 * 分类图标
	 */
	private String categoryIcon;

	/**
	 * 消息编码
	 */
	private String messageCode;


	/**
	 * 消息类型（0-提醒消息，1-内容消息，2-模板消息）
	 */
	private String messageType;

	/**
	 * 消息标题
	 */
	private String messageTitle;

	/**
	 * 消息图标
	 */
	private String messageIcon;

	/**
	 * 消息提示
	 */
	private String messageAlert;

	/**
	 * 消息内容
	 */
	private String messageContent;

	/**
	 * 链接方式（0-不跳转，1-页面，2-内部链接地址，3-外部链接地址）
	 */
	private String linkType;

	/**
	 * 页面URL
	 */
	private String pageUrl;

	/**
	 * 网站URL
	 */
	private String linkUrl;

	/**
	 * 提示参数
	 */
	private List<NotifyTemplateArgs> alertArgs;

	/**
	 * 内容参数
	 */
	private List<NotifyTemplateArgs> contentArgs;

	/**
	 * 模板参数
	 */
	private List<NotifyTemplateArgs> templateArgs;

	/**
	 * 是否启用
	 */
	private Boolean enabled;

}

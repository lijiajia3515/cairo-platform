package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.template;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 修改通知消息模板信息 参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ModifyNotificationTemplateInfoArgs implements Serializable {

	/**
	 * 模板ID
	 */
	@NotNull
	private String templateId;

	/**
	 * 模板名称
	 */
	@NotNull
	private String templateName;

	/**
	 * 消息编码
	 */
	@NotNull
	private String messageCode;

	/**
	 * 消息分类
	 */
	@NotNull
	private String categoryId;

	/**
	 * 消息图标
	 */
	private String messageIcon;

	/**
	 * 消息类型（0-提醒消息，1-内容消息，2-模板消息）
	 */
	@NotNull
	private String messageType;

	/**
	 * 消息标题
	 */
	@NotNull
	private String messageTitle;

	/**
	 * 消息提醒
	 */
	@NotNull
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
	 * 页面地址
	 */
	private String pageUrl;

	/**
	 * 链接地址
	 */
	private String linkUrl;

	/**
	 * 提醒参数值
	 */
	@Builder.Default
	private List<NotifyTemplateArgs> alertArgs = new ArrayList<>();

	/**
	 * 消息内容参数值
	 */
	@Builder.Default
	private List<NotifyTemplateArgs> contentArgs = new ArrayList<>();

	/**
	 * 扩展参数
	 */
	@Builder.Default
	private List<NotifyTemplateArgs> templateArgs = new ArrayList<>();
}

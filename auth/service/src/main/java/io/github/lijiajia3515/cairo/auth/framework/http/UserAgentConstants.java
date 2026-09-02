package io.github.lijiajia3515.cairo.auth.framework.http;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.useragent.Browser;
import cn.hutool.http.useragent.Engine;

import java.util.List;

import static cn.hutool.http.useragent.Browser.Other_Version;

public class UserAgentConstants {
	/**
	 * 支持的引擎类型
	 */
	public static final List<Engine> ENGINES = CollUtil.newArrayList(
		new Engine("Trident", "trident"),
		new Engine("Webkit", "webkit"),
		new Engine("Chrome", "chrome"),
		new Engine("Opera", "opera"),
		new Engine("Presto", "presto"),
		new Engine("Gecko", "gecko"),
		new Engine("KHTML", "khtml"),
		new Engine("Konqueror", "konqueror"),
		new Engine("MIDP", "MIDP"),
		new Engine("Dart", "Dart")
	);

	public static final List<Browser> BROWERS = CollUtil.newArrayList(

		// 部分特殊浏览器是基于安卓、Iphone等的，需要优先判断
		// 企业微信 企业微信使用微信浏览器内核,会包含 MicroMessenger 所以要放在前面
		new Browser("wxwork", "wxwork", "wxwork\\/([\\d\\w\\.\\-]+)"),
		// 微信
		new Browser("MicroMessenger", "MicroMessenger", Other_Version),
		// 微信小程序
		new Browser("miniProgram", "miniProgram", Other_Version),
		// QQ浏览器
		new Browser("QQBrowser", "MQQBrowser", "MQQBrowser\\/([\\d\\w\\.\\-]+)"),
		// 钉钉PC端浏览器
		new Browser("DingTalk-win", "dingtalk-win", "DingTalk\\(([\\d\\w\\.\\-]+)\\)"),
		// 钉钉内置浏览器
		new Browser("DingTalk", "DingTalk", "AliApp\\(DingTalk\\/([\\d\\w\\.\\-]+)\\)"),
		// 支付宝内置浏览器
		new Browser("Alipay", "AlipayClient", "AliApp\\(AP\\/([\\d\\w\\.\\-]+)\\)"),
		// 淘宝内置浏览器
		new Browser("Taobao", "taobao", "AliApp\\(TB\\/([\\d\\w\\.\\-]+)\\)"),
		// UC浏览器
		new Browser("UCBrowser", "UC?Browser", "UC?Browser\\/([\\d\\w\\.\\-]+)"),
		// XiaoMi 浏览器
		new Browser("MiuiBrowser", "MiuiBrowser|mibrowser", "MiuiBrowser\\/([\\d\\w\\.\\-]+)"),
		// 夸克浏览器
		new Browser("Quark", "Quark", Other_Version),
		// 联想浏览器
		new Browser("Lenovo", "SLBrowser", "SLBrowser/([\\d\\w\\.\\-]+)"),
		new Browser("MSEdge", "Edge|Edg", "(?:edge|Edg|EdgA)\\/([\\d\\w\\.\\-]+)"),
		new Browser("Chrome", "chrome", Other_Version),
		new Browser("Firefox", "firefox", Other_Version),
		new Browser("IEMobile", "iemobile", Other_Version),
		new Browser("Android Browser", "android", "version\\/([\\d\\w\\.\\-]+)"),
		new Browser("Safari", "safari", "version\\/([\\d\\w\\.\\-]+)"),
		new Browser("Opera", "opera", Other_Version),
		new Browser("Konqueror", "konqueror", Other_Version),
		new Browser("PS3", "playstation 3", "([\\d\\w\\.\\-]+)\\)\\s*$"),
		new Browser("PSP", "playstation portable", "([\\d\\w\\.\\-]+)\\)?\\s*$"),
		new Browser("Lotus", "lotus.notes", "Lotus-Notes\\/([\\w.]+)"),
		new Browser("Thunderbird", "thunderbird", Other_Version),
		new Browser("Netscape", "netscape", Other_Version),
		new Browser("Seamonkey", "seamonkey", Other_Version),
		new Browser("Outlook", "microsoft.outlook", Other_Version),
		new Browser("Evolution", "evolution", Other_Version),
		new Browser("MSIE", "msie", "msie ([\\d\\w\\.\\-]+)"),
		new Browser("MSIE11", "rv:11", "rv:([\\d\\w\\.\\-]+)"),
		new Browser("Gabble", "Gabble", Other_Version),
		new Browser("Yammer Desktop", "AdobeAir", "([\\d\\w\\.\\-]+)\\/Yammer"),
		new Browser("Yammer Mobile", "Yammer[\\s]+([\\d\\w\\.\\-]+)", "Yammer[\\s]+([\\d\\w\\.\\-]+)"),
		new Browser("Apache HTTP Client", "Apache\\\\-HttpClient", "Apache\\-HttpClient\\/([\\d\\w\\.\\-]+)"),
		new Browser("BlackBerry", "BlackBerry", "BlackBerry[\\d]+\\/([\\d\\w\\.\\-]+)")
	);
}

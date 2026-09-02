package io.github.lijiajia3515.cairo.auth.modules.weboffice.v3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
public class WebOfficeService {

	public static final String DEFAULT_AVATAR_URL = "https://example.com/public/avatar/default.png";
	public static final String DOC_BUCKET = "doc";
	public static final Set<String> W_EXT_NAMES = Set.of("doc", "dot", "wps", "wpt", "docx", "dotx", "docm", "dotm", "rtf");
	public static final Set<String> S_EXT_NAMES = Set.of("xls", "xlt", "et", "xlsx", "xltx", "csv", "xlsm", "xltm");
	public static final Set<String> P_EXT_NAMES = Set.of("ppt", "pptx", "pptm", "ppsx", "ppsm", "pps", "potx", "potm", "dpt", "dps");
	public static final Set<String> F_EXT_NAMES = Set.of("pdf");

	public WebOfficeService() {
	}

	public String getType(String extName) {
		String realExt = extName.toLowerCase();
		if (W_EXT_NAMES.contains(realExt)) {
			return "w";
		}
		if (S_EXT_NAMES.contains(realExt)) {
			return "s";
		}
		if (P_EXT_NAMES.contains(realExt)) {
			return "p";
		}
		if (F_EXT_NAMES.contains(realExt)) {
			return "f";
		}
		return "w";
	}
}

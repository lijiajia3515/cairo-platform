package io.github.lijiajia3515.cairo.auth.framework.imgproxy;

import lombok.SneakyThrows;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ImgProxy {
	static final List<String> FORMAT_KEY = Arrays.asList("format", "f", "ext");
	final static String HMACSHA256 = "HmacSHA256";

//	public static void testWithJavaHmacApacheBase64ImgProxyTest() throws Exception {
//		byte[] key = hexStringToByteArray("your-hex");
//		byte[] salt = hexStringToByteArray("your-hex");
//		String url = "s3://your-bucket/1630465317359/1630488672828.png";
//
//		String resize = "fill";
//		int width = 300;
//		int height = 300;
//		String gravity = "no";
//		int enlarge = 1;
//		String extension = "jpg";
//		String urlWithHash = generateSignedUrl(key, salt, url, new HashMap<>() {{
//			put("resize", resize);
//			put("width", width + "");
//			put("height", height + "");
//			put("gravity", gravity + "");
//			put("enlarge", enlarge + "");
//			put("ext", extension);
//		}});
//		System.out.println(urlWithHash);
//
//		// assertEquals("/_PQ4ytCQMMp-1w1m_vP6g8Qb-Q7yF9mwghf6PddqxLw/fill/300/300/no/1/aHR0cDovL2ltZy5leGFtcGxlLmNvbS9wcmV0dHkvaW1hZ2UuanBn.png", urlWithHash);
//	}

	@SneakyThrows
	public static String generateSignedUrl(byte[] key, byte[] salt, String url, Map<String, String> params) {
		String encodedUrl = Base64.getUrlEncoder().withoutPadding().encodeToString(url.getBytes());
		String processOptions = params.isEmpty() ? "" : "/" + params.keySet().stream().map(d -> String.join(":", d, params.getOrDefault(d, ""))).collect(Collectors.joining("/"));
		String path = processOptions + "/" + encodedUrl + "." + extension(url, params);

		Mac sha256HMAC = Mac.getInstance(HMACSHA256);
		SecretKeySpec secretKey = new SecretKeySpec(key, HMACSHA256);
		sha256HMAC.init(secretKey);
		sha256HMAC.update(salt);

		String hash = Base64.getUrlEncoder().withoutPadding().encodeToString(sha256HMAC.doFinal(path.getBytes()));

		return "/" + hash + path;
	}

	static String extension(String url, Map<String, String> params) {
		return FORMAT_KEY.stream().filter(params::containsKey).map(params::get).findFirst().orElse(url.substring(url.lastIndexOf(".") + 1));
	}

	public static byte[] hexStringToByteArray(String hex) {
		if (hex.length() % 2 != 0)
			throw new IllegalArgumentException("Even-length string required");
		byte[] res = new byte[hex.length() / 2];
		for (int i = 0; i < res.length; i++) {
			res[i] = (byte) ((Character.digit(hex.charAt(i * 2), 16) << 4) | (Character.digit(hex.charAt(i * 2 + 1), 16)));
		}
		return res;
	}
}

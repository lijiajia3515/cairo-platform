package io.github.lijiajia3515.cairo.jackson.test;

import io.github.lijiajia3515.cairo.jackson.desensitize.Desensitize;
import io.github.lijiajia3515.cairo.jackson.desensitize.DesensitizeType;
import lombok.Data;

@Data
public class TestData {


	@Desensitize(type = DesensitizeType.USER_ID)
	private String userId = "afsfsfsdfsadf";

	@Desensitize(type = DesensitizeType.ID_CARD)
	private String idCard = "360121122809083515";


	@Desensitize(type = DesensitizeType.CHINESE_NAME)
	private String name = "lijiajia3515@aliyun.com";

	@Desensitize(type = DesensitizeType.FIXED_PHONE)
	private String fixedPhone = "13072745282";

	@Desensitize(type = DesensitizeType.MOBILE_PHONE)
	private String phone = "13072745282";

	@Desensitize(type = DesensitizeType.ADDRESS)
	private String address = "湖北省武汉市武昌区和平街道保利城1期12-1-603";

	@Desensitize(type = DesensitizeType.EMAIL)
	private String email = "lijiajia3515@aliyun.com";

	@Desensitize(type = DesensitizeType.PASSWORD)
	private String password = "sdfasdfsadfsdf";


	@Desensitize(type = DesensitizeType.CAR_LICENSE)
	private String carLicense = "鄂A123123";

	@Desensitize(type = DesensitizeType.BANK_CARD)
	private String bankCard = "62122830020015462523";

	@Desensitize(type = DesensitizeType.IPV4)
	private String ipv4 = "192.168.0.1";

	@Desensitize(type = DesensitizeType.IPV6)
	private String ipv6 = "a:a:b:d:e:f:a:f";
}

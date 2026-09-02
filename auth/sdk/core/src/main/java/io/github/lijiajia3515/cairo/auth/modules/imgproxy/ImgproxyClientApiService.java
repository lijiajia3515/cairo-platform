package io.github.lijiajia3515.cairo.auth.modules.imgproxy;

import io.github.lijiajia3515.cairo.auth.domain.api.client.imgproxy.GetImgUrlArgs;

import java.util.List;

public interface ImgproxyClientApiService {

	List<String> getProxyUrl(List<GetImgUrlArgs> params);
}

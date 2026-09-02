package io.github.lijiajia3515.cairo.auth.framework.aliyunsms;

import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient;
import org.springframework.beans.factory.DisposableBean;

public class AliyunDysmsDestroy implements DisposableBean {
	private final AsyncClient asyncClient;

	public AliyunDysmsDestroy(AsyncClient asyncClient) {
		this.asyncClient = asyncClient;
	}

	@Override
	public void destroy() throws Exception {
		asyncClient.close();
	}
}

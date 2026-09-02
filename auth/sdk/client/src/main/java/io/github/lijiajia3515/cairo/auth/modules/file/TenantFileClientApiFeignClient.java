package io.github.lijiajia3515.cairo.auth.modules.file;

import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkClientFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_file.GetFileStatArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.FileStat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 企业应用文件子应用-FeignClient
 */
@FeignClient(
	contextId = "tenantFileClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/tenant_file",
	fallbackFactory = TenantFileClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkClientFeignClientConfiguration.class
)
public interface TenantFileClientApiFeignClient {
	/**
	 * 访问文件
	 * 需要权限： tenant_file:all｜tenant_file:access_file
	 *
	 * @param args 参数
	 * @return 带签名的文件地址
	 */
	@PostMapping("/access_file")
	ResponseEntity<BusinessResult<List<String>>> accessFile(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody AccessFileArgs args);

	/**
	 * 获取访问文件Map
	 * 需要权限： tenant_file:all｜tenant_file:access_file
	 * @param tenantId 企业id
	 * @param s3Urls s3协议文件地址
	 * @return s3转http 文件map
	 */
	default Map<String, String> getAccessFileMap(String tenantId,List<String> s3Urls) {
		return Optional.ofNullable(s3Urls)
			.filter(x -> !x.isEmpty())
			.map(os -> {
				ResponseEntity<BusinessResult<List<String>>> accessSignUrlResp = accessFile("",AccessFileArgs.builder()
					.tenantId(tenantId)
					.s3Urls(os).build());
				List<String> httpUrls = Optional.ofNullable(accessSignUrlResp.getBody()).map(BusinessResult::getData).orElseThrow();
				return IntStream.range(0, os.size())
					.mapToObj(x -> Arrays.asList(s3Urls.get(x), httpUrls.get(x)))
					.collect(Collectors.toMap(x -> x.get(0), x -> x.get(1), (x1, x2) -> x1));
			})
			.orElse(Collections.emptyMap());
	}

	/**
	 * 获取文件状态
	 * 需要权限： tenant_file:all｜tenant_file:get_file_stat
	 *
	 * @param args 参数
	 * @return 文件状态列表
	 */
	@PostMapping("/get_file_stat")
	ResponseEntity<BusinessResult<List<FileStat>>> getFileStat(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody GetFileStatArgs args);

	/**
	 * 上传文件
	 * 需要权限： tenant_file:all｜tenant_file:upload_file
	 *
	 * @param path 上传路径
	 * @param file 文件
	 * @return 0-上传路径,1-s3格式地址,2-http格式地址
	 */
	@PostMapping(value = "/upload_file", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	ResponseEntity<BusinessResult<List<String>>> uploadFile(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
															@RequestParam("tenant_id") String tenantId,
															@RequestParam("path") String path,
															@RequestPart("file") MultipartFile file);

	/**
	 * 删除文件
	 * 需要权限： tenant_file:all｜tenant_file:delete_file
	 *
	 * @param args 参数
	 */
	@PostMapping("/delete_file")
	ResponseEntity<BusinessResult<List<FileStat>>> deleteFile(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
															  @RequestBody DeleteFileArgs args);

}

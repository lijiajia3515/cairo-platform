package io.github.lijiajia3515.cairo.auth.modules.file.temporary_file;

import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.auth.CairoAuthClientSdkCoreFeignClientConfiguration;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.FileStat;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.temporary_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.temporary_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.temporary_file.GetFileStatArgs;
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
 * 临时文件子应用-FeignClient
 */
@FeignClient(
	contextId = "temporaryFileClientApiFeignClient",
	name = "${cairo.feign.client.cairo-auth-service:cairo-auth-service}",
	path = "/client_api/temporary_file",
	fallbackFactory = TemporaryFileClientApiFeignClientFallbackFactory.class,
	configuration = CairoAuthClientSdkCoreFeignClientConfiguration.class
)
public interface TemporaryFileClientApiFeignClient {
	/**
	 * 访问文件
	 * 需要权限： temporary_file:all｜temporary_file:access_file
	 *
	 * @param args 参数
	 * @return 带签名的文件地址
	 */
	@PostMapping("/access_file")
	ResponseEntity<BusinessResult<List<String>>> accessFile(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody AccessFileArgs args);

	/**
	 * 获取访问文件Map
	 * 需要权限： temporary_file:all｜temporary_file:access_file
	 *
	 * @param s3Urls s3协议文件地址
	 * @return s3转http 文件map
	 */
	default Map<String, String> getAccessFileMap(List<String> s3Urls) {
		return Optional.ofNullable(s3Urls)
			.filter(x -> !x.isEmpty())
			.map(os -> {
				ResponseEntity<BusinessResult<List<String>>> accessSignUrlResp = accessFile("",AccessFileArgs.builder().s3Urls(os).build());
				List<String> httpUrls = Optional.ofNullable(accessSignUrlResp.getBody()).map(BusinessResult::getData).orElseThrow();
				return IntStream.range(0, os.size())
					.mapToObj(x -> Arrays.asList(s3Urls.get(x), httpUrls.get(x)))
					.collect(Collectors.toMap(x -> x.get(0), x -> x.get(1), (x1, x2) -> x1));
			})
			.orElse(Collections.emptyMap());
	}

	/**
	 * 获取文件状态
	 * 需要权限： temporary_file:all｜temporary_file:get_file_stat
	 *
	 * @param args 参数
	 * @return 文件状态列表
	 */
	@PostMapping("/get_file_stat")
	ResponseEntity<BusinessResult<List<FileStat>>> getFileStat(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,@RequestBody GetFileStatArgs args);


	/**
	 * 上传文件
	 * 需要权限： temporary_file:all｜temporary_file:upload_file
	 *
	 * @param path 上传路径
	 * @param file 文件
	 * @return 0-上传路径,1-s3格式地址,2-http格式地址
	 */
	@PostMapping(value = "/upload_file", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	ResponseEntity<BusinessResult<List<String>>> uploadFile(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
															@RequestParam("path") String path,
															@RequestPart("file") MultipartFile file);

	/**
	 * 上传多个文件
	 * 需要权限： temporary_file:all｜temporary_file:upload_file
	 *
	 * @param prefix 文件前缀
	 * @param files   文件
	 * @return 0-上传路径,1-s3格式地址,2-http格式地址
	 */
	@PostMapping(value = "/upload_files", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	ResponseEntity<BusinessResult<List<List<String>>>> uploadFiles(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
																   @RequestParam("prefix") String prefix,
																   @RequestPart("files")  List<MultipartFile> files);

	/**
	 * 获取上传文件签名url
	 * 需要权限： temporary_file:all｜temporary_file:upload_file
	 *
	 * @param prefix 文件前缀
	 * @param size   文件个数
	 * @return 0-上传路径,1-s3格式地址,2-http格式地址
	 */
	@PostMapping(value = "/get_upload_file_sign_url", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	ResponseEntity<BusinessResult<List<List<String>>>> getUploadFileSignUrl(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,@RequestParam("prefix") String prefix, @RequestParam(name = "size", defaultValue = "1") Integer size);

	/**
	 * 删除文件
	 * 需要权限： temporary_file:all｜temporary_file:delete_file
	 *
	 * @param args 参数
	 */
	@PostMapping("/delete_file")
	ResponseEntity<BusinessResult<List<FileStat>>> deleteFile(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
															  @RequestBody DeleteFileArgs args);

}

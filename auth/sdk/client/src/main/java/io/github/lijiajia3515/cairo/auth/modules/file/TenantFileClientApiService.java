package io.github.lijiajia3515.cairo.auth.modules.file;

import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_file.GetFileStatArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.FileStat;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface TenantFileClientApiService {

	/**
	 * 访问文件
	 * 需要权限： tenant_file:all｜tenant_file:access_file
	 *
	 * @param args 参数
	 * @return 带签名的文件地址
	 */
	List<String> accessFile(@RequestBody AccessFileArgs args);

	/**
	 * 访问文件Map
	 * 需要权限： tenant_file:all｜tenant_file:access_file
	 *
	 * @param args s3协议文件地址
	 * @return s3转http 文件map
	 */
	default Map<String, String> getAccessFileMap(AccessFileArgs args) {
		if (args == null || args.getS3Urls() == null || args.getS3Urls().isEmpty()) return Collections.emptyMap();
		List<String> httpUrls = accessFile(args);
		return IntStream.range(0, args.getS3Urls().size())
			.mapToObj(x -> Arrays.asList(args.getS3Urls().get(x), httpUrls.get(x)))
			.collect(Collectors.toMap(x -> x.get(0), x -> x.get(1), (x1, x2) -> x1));
	}


	/**
	 * 获取文件状态
	 * 需要权限： tenant_file:all｜tenant_file:get_file_stat
	 *
	 * @param args 参数
	 * @return 文件状态列表
	 */
	List<FileStat> getFileStat(GetFileStatArgs args);

	/**
	 * 上传文件
	 * 需要权限： tenant_file:all｜tenant_file:upload_file
	 *
	 * @param path 上传路径
	 * @param file 文件
	 * @return 0-上传路径,1-s3格式地址,2-http格式地址
	 */
	List<String> uploadFile(String tenantId,String path,MultipartFile file);

	/**
	 * 删除文件
	 * 需要权限： tenant_file:all｜tenant_file:delete_file
	 *
	 * @param args 参数
	 */
	List<FileStat> deleteFile(DeleteFileArgs args);

	/**
	 * 访问文件Map
	 * 需要权限： tenant_file:all｜tenant_file:access_file
	 *
	 * @param tenantId tenantId
	 * @param s3Urls s3Urls
	 * @return 带签名的文件地址 map
	 */
	Map<String, String> getAccessFileMap(String tenantId,List<String> s3Urls);
}

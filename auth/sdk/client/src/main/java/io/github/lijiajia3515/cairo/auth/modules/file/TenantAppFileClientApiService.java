package io.github.lijiajia3515.cairo.auth.modules.file;

import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_app_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_app_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_app_file.GetFileStatArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_app_file.GetFolderArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_app_file.ListFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_app_file.MkdirArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_app_file.MoveFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.tenant_app_file.UploadFileSignArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.CairoFileItem;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.FileStat;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.Folder;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.UploadSignArgs;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface TenantAppFileClientApiService {


	/**
	 * 读取文件列表
	 * 需要权限： tenant_app_file:all｜tenant_app_file:list_file
	 *
	 * @param args 参数
	 * @return 带签名的文件地址
	 */
	List<CairoFileItem> listFile(ListFileArgs args);

	/**
	 * 读取文件夹列表
	 * 需要权限： tenant_app_file:all｜tenant_app_file:get_folder
	 *
	 * @param args 参数
	 * @return 带签名的文件地址
	 */
	List<Folder> getFolderList(GetFolderArgs args);

	/**
	 * 读取文件夹列表
	 * 需要权限： tenant_app_file:all｜tenant_app_file:get_folder
	 *
	 * @param args 参数
	 * @return 带签名的文件地址
	 */
	List<Folder> getFolderTreeList(GetFolderArgs args);

	/**
	 * 创建文件夹
	 * 需要权限： tenant_app_file:all｜tenant_app_file:mkdir
	 *
	 * @param args 参数
	 * @return size
	 */
	Integer mkdir(MkdirArgs args);

	/**
	 * 访问文件
	 * 需要权限： tenant_app_file:all｜tenant_app_file:access_file
	 *
	 * @param args 参数
	 * @return 带签名的文件地址
	 */
	List<String> accessFile(AccessFileArgs args);

	/**
	 * 访问文件Map
	 * 需要权限： tenant_app_file:all｜tenant_app_file:access_file
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
	 * 需要权限： tenant_app_file:all｜tenant_app_file:get_file_stat
	 *
	 * @param args 参数
	 * @return 文件状态列表
	 */
	List<FileStat> getFileStat(GetFileStatArgs args);

	/**
	 * 上传文件
	 * 需要权限： tenant_app_file:all｜tenant_app_file:upload_file
	 *
	 * @param tenantId 企业ID
	 * @param path     上传路径
	 * @param file     文件
	 * @return 0-上传路径,1-s3格式地址,2-http格式地址
	 */
	List<String> uploadFile(String tenantId, String path, MultipartFile file);

	/**
	 * 上传文件
	 * 需要权限： tenant_app_file:all｜tenant_app_file:upload_file
	 *
	 * @param tenantId 企业ID
	 * @param path     上传路径
	 * @param file     文件
	 * @param metadata 文件属性
	 * @return 0-上传路径,1-s3格式地址,2-http格式地址
	 */
	List<String> uploadFile(String tenantId, String path, MultipartFile file, Map<String, String> metadata);

	/**
	 * 删除文件
	 * 需要权限： tenant_app_file:all｜tenant_app_file:delete_file
	 *
	 * @param args 参数
	 */
	Integer deleteFile(DeleteFileArgs args);

	/**
	 * 访问文件Map
	 * 需要权限： tenant_app_file:all｜tenant_app_file:access_file
	 *
	 * @param tenantId tenantId
	 * @param s3Urls   s3Urls
	 * @return 带签名的文件地址 map
	 */
	Map<String, String> getAccessFileMap(String tenantId, List<String> s3Urls);


	/**
	 * 获取上传文件签名参数
	 *
	 * @param args 参数
	 * @return 预上传签名参数值
	 */
	UploadSignArgs getUploadFileSign(UploadFileSignArgs args);

	/**
	 * 移动文件/重命名文件
	 * 需要权限： tenant_app_file:all｜tenant_app_file:move_file
	 *
	 * @return
	 */
	Integer moveFile(MoveFileArgs args);


}

package io.github.lijiajia3515.cairo.auth.modules.file.temporary_file;

import io.github.lijiajia3515.cairo.auth.domain.dto.file.FileStat;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.temporary_file.AccessFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.temporary_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.file.temporary_file.GetFileStatArgs;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TemporaryFileClientApiService {

	/**
	 * 访问文件
	 * 需要权限： common_file:all｜common_file:access_file
	 *
	 * @param args 参数
	 * @return 带签名的文件地址
	 */
	List<String> accessFile(AccessFileArgs args);

	/**
	 * 获取文件状态
	 * 需要权限： common_file:all｜common_file:get_file_stat
	 *
	 * @param args 参数
	 * @return 文件状态列表
	 */
	List<FileStat> getFileStat(GetFileStatArgs args);


	/**
	 * 上传文件
	 * 需要权限： common_file:all｜common_file:upload_file
	 *
	 * @param bucket 文件桶
	 * @param file   文件
	 * @return 0-上传路径,1-s3格式地址,2-http格式地址
	 */
	List<String> uploadFile(String bucket,MultipartFile file);

	/**
	 * 上传多个文件
	 * 需要权限： temporary_file:all｜temporary_file:upload_file
	 *
	 * @param prefix 文件前缀
	 * @param files   文件
	 * @return 0-上传路径,1-s3格式地址,2-http格式地址
	 */
	List<List<String>> uploadFiles(String prefix,List<MultipartFile> files);
	/**
	 * 获取上传文件签名url
	 * 需要权限： temporary_file:all｜temporary_file:upload_file
	 *
	 * @param prefix 文件前缀
	 * @param size   文件个数
	 * @return 0-上传路径,1-s3格式地址,2-http格式地址
	 */
	List<List<String>> getUploadFileSignUrl(String prefix,Integer size);
	/**
	 * 删除文件
	 * 需要权限： common_file:all｜common_file:delete_file
	 *
	 * @param args 参数
	 */
	List<FileStat> deleteFile(DeleteFileArgs args);
}

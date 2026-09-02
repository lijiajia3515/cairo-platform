package io.github.lijiajia3515.cairo.auth.modules.file;

import io.minio.Result;
import io.minio.messages.DeleteError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 异步文件服务
 */
@Slf4j
@Component
public class AsyncFileService {
	/**
	 * 异步从远端删除文件
	 *
	 * @param deleteResults 需要删除的文件
	 */
	@Async
	public void deleteFile(Iterable<Result<DeleteError>> deleteResults) {
		deleteResults.forEach(deleteErrorResult -> {
			try {
				DeleteError deleteError = deleteErrorResult.get();
				log.debug("文件删除失败：Code: {} Message: {} Bucket: {} Object: {} Resource: {}", deleteError.code(), deleteError.message(), deleteError.bucketName(), deleteError.objectName(), deleteError.resource());
			} catch (Exception e) {
				log.info("minio执行异常： {}", e.getMessage());
			}
		});
	}
}

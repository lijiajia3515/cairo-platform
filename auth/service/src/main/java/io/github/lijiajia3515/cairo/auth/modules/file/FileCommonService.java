package io.github.lijiajia3515.cairo.auth.modules.file;

import io.github.lijiajia3515.cairo.auth.domain.api.client.file.public_file.DeleteFileArgs;
import io.github.lijiajia3515.cairo.auth.api.client.file.public_file.PublicFileClientApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
@Component
public class FileCommonService {

	private final PublicFileClientApiService publicFileClientApiService;

	private static final String S3_PREFIX = "s3";
	private static final String HTTP_PREFIX = "http";

	public FileCommonService(PublicFileClientApiService publicFileClientApiService) {
		this.publicFileClientApiService = publicFileClientApiService;
	}


	@Async
	public void deletePublicFile(String keyPrefix, List<String> urls) {
		try {
			if (!urls.isEmpty())
				publicFileClientApiService.deleteFile(DeleteFileArgs.builder()
					.keyPrefix(keyPrefix)
					.s3Urls(urlConverter(urls).getS3Urls())
					.httpUrls(urlConverter(urls).getHttpUrls())
					.build());
		} catch (Exception e) {
			log.error("删除公开存储文件失败", e);
		}
	}

	UrlConverter urlConverter(List<String> urls) {
		List<String> s3Urls = urls.stream().filter(Objects::nonNull).filter(url -> url.startsWith(S3_PREFIX)).distinct().collect(Collectors.toList());
		List<String> httpUrls = urls.stream().filter(Objects::nonNull).filter(url -> url.startsWith(HTTP_PREFIX)).distinct().collect(Collectors.toList());
		return UrlConverter.builder().s3Urls(s3Urls).httpUrls(httpUrls).build();
	}

}



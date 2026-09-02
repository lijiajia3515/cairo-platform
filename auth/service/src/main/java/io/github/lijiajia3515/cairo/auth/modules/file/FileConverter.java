package io.github.lijiajia3515.cairo.auth.modules.file;

import io.github.lijiajia3515.cairo.auth.domain.dto.file.FileStat;
import io.minio.StatObjectResponse;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FileConverter {

	private static final String S3_PREFIX = "s3";
	private static final String HTTP_PREFIX = "http";

	public static FileStat convert(StatObjectResponse response) {
		return FileStat.builder()
			.exists(true)
			.region(response.region())
			.bucket(response.bucket())
			.version(response.versionId())
			.etag(response.etag())
			.lastModified(response.lastModified().toLocalDateTime())
			.size(response.size())
			.headers(response.headers().names().stream().map(x -> Tuples.of(x, response.headers().values(x))).collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2)))
			.userMetadata(response.userMetadata())
			.object(response.object())
			.build();
	}

	public  static UrlConverter urlConverter(List<String> urls) {
		List<String> s3Urls = urls.stream().filter(Objects::nonNull).filter(url -> url.startsWith(S3_PREFIX)).distinct().collect(Collectors.toList());
		List<String> httpUrls = urls.stream().filter(Objects::nonNull).filter(url -> url.startsWith(HTTP_PREFIX)).distinct().collect(Collectors.toList());
		return UrlConverter.builder().s3Urls(s3Urls).httpUrls(httpUrls).build();
	}
}

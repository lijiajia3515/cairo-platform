package io.github.lijiajia3515.cairo.auth.modules.captcha;

import cn.hutool.core.io.IoUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class CairoMultipartFile implements MultipartFile {

	private final String name;

	private String originalFilename;

	private String contentType;

	private final byte[] bytes;


	public CairoMultipartFile(String name, byte[] content) {
		this(name, "", null, content);
	}


	public CairoMultipartFile(String name, InputStream contentStream) throws IOException {
		this(name, "", null, FileCopyUtils.copyToByteArray(contentStream));
	}

	public CairoMultipartFile(String name, String contentType, InputStream contentStream) throws IOException {
		this(name, "", contentType, FileCopyUtils.copyToByteArray(contentStream));
	}

	public CairoMultipartFile(String name, String contentType, byte[] contentByes) {
		this(name, "", contentType, contentByes);
	}

	public CairoMultipartFile(String name, String originalFilename, String contentType, InputStream inputStream)
		throws IOException {
		this(name, originalFilename, contentType, IoUtil.readBytes(inputStream));
	}

	public CairoMultipartFile(String name, String originalFilename, String contentType, byte[] bytes) {
		this.name = name;
		this.originalFilename = (originalFilename != null ? originalFilename : "");
		this.contentType = contentType;
		this.bytes = (bytes != null ? bytes : new byte[0]);
	}



	@NotNull
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getOriginalFilename() {
		return this.originalFilename;
	}

	@Override
	public String getContentType() {
		return this.contentType;
	}

	@Override
	public boolean isEmpty() {
		return (this.bytes.length == 0);
	}

	@Override
	public long getSize() {
		return this.bytes.length;
	}

	@Override
	public byte[] getBytes() throws IOException {
		return this.bytes;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(this.bytes);
	}

	@Override
	public void transferTo(File dest) throws IOException, IllegalStateException {
		FileCopyUtils.copy(this.bytes, dest);
	}
}

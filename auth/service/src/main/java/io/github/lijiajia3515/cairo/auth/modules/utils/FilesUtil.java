package io.github.lijiajia3515.cairo.auth.modules.utils;

import cn.hutool.core.io.FileTypeUtil;
import io.github.lijiajia3515.cairo.auth.modules.captcha.CairoMultipartFile;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class FilesUtil {

	/**
	 * url转MultipartFile
	 */
	public static CairoMultipartFile urlConvertCairoMultipart(String url, String fileName) {
		try {
			byte[] bytes = HttpUtil.downloadBytes(url);
			// 获取文件头部二进制
			byte[] headBytes = ArrayUtil.sub(bytes, 0, 28);
			// 文件头转16二进制
			String hexHead = HexUtil.encodeHexStr(headBytes, false);
			// 文件类型
			String fileType = FileTypeUtil.getType(hexHead);
			return new CairoMultipartFile(fileName,fileName, getContentType(fileType), bytes);
		} catch (Exception e) {
			log.error("urlConvertCairoMultipart error", e);
		}
		return null;
	}

	public static String getContentType(String fileType) {
		Map<String, String> map = new HashMap<>();
		map.put("png", "image/png");
		map.put("jpg", "image/jpeg");
		map.put("gif", "image/gif");
		return map.getOrDefault(fileType, null);
	}

	/**
	 * stream转MultipartFile
	 */
	public static CairoMultipartFile inputConvertCairoMultipart(InputStream inputStream, String fileName) {
		try {
			byte[] bytes = streamToByteArray(inputStream);
			// 获取文件头部二进制
			byte[] headBytes = ArrayUtil.sub(bytes, 0, 28);
			// 文件头转16二进制
			String hexHead = HexUtil.encodeHexStr(headBytes, false);
			// 文件类型
			String fileType = FileTypeUtil.getType(hexHead);
			return new CairoMultipartFile(fileName, getContentType(fileType), bytes);
		} catch (Exception e) {
			log.error("inputConvertCairoMultipart error", e);
		}
		return null;
	}

	public static byte[] streamToByteArray(InputStream is) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();//创建输出流对象
			byte[] b = new byte[1024];
			int len;
			while ((len = is.read(b)) != -1) {
				bos.write(b, 0, len);
			}
			byte[] array = bos.toByteArray();
			bos.close();
			return array;
		} catch (IOException e) {
			log.error("streamToByteArray error", e);
		}
		return null;
	}

//	/**
//	 * MultipartFile格式
//	 */
//	public static MultipartFile getMulFileByPath(String path, String name, String contentType) {
//		FileItem fileItem = createFileItem(path, name, contentType);
//		return new CommonsMultipartFile(fileItem);
//	}

	private static FileItem createFileItem(String filePath, String name, String contentType) {
		FileItemFactory factory = new DiskFileItemFactory(16, null);
		String textFieldName = "textField";
		int num = filePath.lastIndexOf(".");
		String extFile = filePath.substring(num);
		FileItem item = factory.createItem(textFieldName, contentType, true,
			name + extFile);
		File enfile = new File(filePath);
		long fileSize = enfile.length();
		int bytesRead;
		byte[] buffer = new byte[(int) fileSize];
		try {
			FileInputStream fis = new FileInputStream(enfile);
			OutputStream os = item.getOutputStream();
			while ((bytesRead = fis.read(buffer, 0, buffer.length)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			os.close();
			fis.close();
		} catch (IOException e) {
			log.warn("createFileItem", e);
		}
		return item;
	}

	/**
	 * 创建文件
	 */
	public static void createFile(File file) {
		File fileParent = file.getParentFile();
		if (!fileParent.exists()) {
			boolean mkdirs = fileParent.mkdirs();
			log.info("mkdirs {}", mkdirs);
		}
		try {
			boolean newFile = file.createNewFile();
			log.info("createNewFile {}", newFile);
		} catch (IOException e) {
			log.error("createFile error", e);
		}
	}

	/**
	 * 创建文件夹
	 */
	public static void createFiles(File file) {
		File fileParent = file.getParentFile();
		if (!fileParent.exists()) {
			boolean mkdirs = fileParent.mkdirs();
			log.info("mkdirs {}", mkdirs);
		}
		boolean newFile = file.mkdirs();
		log.info("createNewFiles {}", newFile);

	}

	public static final Set<String> urlFormat = Stream.of
		("png", "jpeg", "jpg", "staticmap", "mov", "wmf", "pict", "png", "dib", "gif", "tiff", "eps", "bmp", "wpg", "webp",
			"PNG", "JPEG", "JPG", "STATICMAP", "MOV", "WMF", "PICT", "PNG", "DIB", "GIF", "TIFF", "EPS", "BMP", "WPG", "WEBP"
		).collect(Collectors.toSet());

	/**
	 * 删除文件夹
	 */
	public static boolean deleteAllFile(String dir) {
		File dirFile = new File(dir);
		// 如果dir对应的文件不存在，或者不是一个目录，则退出
		if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
			log.info("[deleteAllFile][error] result-> {}", "删除文件夹失败：" + dir + "不存在！");
			return false;
		}
		boolean flag = true;
		// 删除文件夹中的所有文件包括子文件夹
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			// 删除子文件
			if (files[i].isFile()) {
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag)
					break;
			}
			// 删除子文件夹
			else if (files[i].isDirectory()) {
				flag = deleteAllFile(files[i].getAbsolutePath());
				if (!flag)
					break;
			}
		}
		if (!flag) {
			log.info("[deleteAllFile][error] result-> {}", "删除文件夹失败");
			return false;
		}
		// 删除当前文件夹
		if (dirFile.delete()) {
			log.info("[deleteAllFile][success] result-> {}", "删除文件夹成功：" + dir);
			return true;
		} else {
			return false;
		}
	}

	public static String getFilename(String filename, int maxLength) {
		if (filename == null) return null;
		int a = filename.lastIndexOf(".");

		if (maxLength > a) return filename;

		return filename.substring(0, maxLength) + filename.substring(a);
	}

	/**
	 * 删除文件
	 */
	public static boolean deleteFile(String fileName) {
		File file = new File(fileName);
		// 如果文件路径只有单个文件
		if (file.exists() && file.isFile()) {
			if (file.delete()) {
				log.info("[deleteFile][success] result-> {}", "删除文件成功：" + fileName);
				return true;
			} else {
				log.info("[deleteFile][error] result-> {}", "删除文件失败：" + fileName);
				return false;
			}
		} else {
			log.info("[deleteFile][error] result-> {}", "文件不存在：" + fileName);
			return false;
		}
	}

	public static String getType(String imgFile) {
		if (imgFile != null) {
			String format;
			imgFile = imgFile.toLowerCase();
			if (imgFile.contains(".png")) format = ".png";
			else if (imgFile.contains(".jpeg") || imgFile.contains(".jpg") || imgFile.contains("staticmap") || imgFile.contains(".mov"))
				format = ".jpeg";
			else if (imgFile.endsWith(".wmf")) format = ".wmf";
			else if (imgFile.endsWith(".pict")) format = ".pict";
			else if (imgFile.endsWith(".png")) format = ".png";
			else if (imgFile.endsWith(".dib")) format = ".dib";
			else if (imgFile.endsWith(".gif")) format = ".gif";
			else if (imgFile.endsWith(".tiff")) format = ".tiff";
			else if (imgFile.endsWith(".eps")) format = ".eps";
			else if (imgFile.endsWith(".bmp")) format = ".bmp";
			else if (imgFile.endsWith(".wpg") || imgFile.contains(".wabp")) format = ".wpg";
			else {
				return imgFile;
			}
			return format;
		} else {
			return null;
		}
	}
}

package io.github.lijiajia3515.cairo.auth.modules.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileKey {
	/**
	 * key
	 */
	private String key;
	/**
	 * version
	 */
	private String version;

	public FileKey(String key) {
		this.key = key;
	}
}
